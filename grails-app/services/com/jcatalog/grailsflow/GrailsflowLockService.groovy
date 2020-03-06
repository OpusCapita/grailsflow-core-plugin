package com.jcatalog.grailsflow

import com.jcatalog.grailsflow.model.process.ProcessNode
import com.jcatalog.grailsflow.cluster.GrailsflowLock
import com.jcatalog.grailsflow.utils.ConstantUtils
import org.hibernate.FetchMode
import com.jcatalog.grailsflow.status.ProcessStatusEnum
import com.jcatalog.grailsflow.status.NodeStatusEnum
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import groovy.transform.Synchronized
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.Trigger
import org.quartz.JobDetail

import static org.quartz.JobKey.jobKey
import com.jcatalog.grailsflow.cluster.ClusterInfo
import com.jcatalog.grailsflow.scheduling.triggers.ConfigurableSimpleTrigger

/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


/**
 *  GrailsflowLockService class is used to organize locks for process execution
 *
 * @author July Antonicheva
 */
class GrailsflowLockService {

    static transactional = false // manage transactions manually

    def processManagerService
    def grailsApplication

    @SuppressWarnings('UnusedPrivateField')
    private final Object lock = new Object()

    @Synchronized("lock")
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean lockProcessExecution(ProcessNode node) {
        try {
            if (!node) {
                log.error("Cannot lock node: node cannot be nullable")
                return Boolean.FALSE
            }
            // check if the process is runnable by this NodeActivator
            // try to save Grailsflow instance
            // if everything is okay - then send event
            GrailsflowLock processLock = GrailsflowLock.findByProcess(node.process)
            if (processLock){
                return Boolean.FALSE
            }

            String currentThreadName = (grailsApplication.config.grailsflow.clusterName instanceof Closure) ?
                grailsApplication.config.grailsflow.clusterName()?.toString() :
                grailsApplication.config.grailsflow.clusterName?.toString()

            GrailsflowLock savedLock = new GrailsflowLock(process: node.process, nodeID:  node.nodeID,
                    clusterName: currentThreadName, lockedOn: new Date()).save(flush:true)
            if (!savedLock) {
                return Boolean.FALSE
            }

            return Boolean.TRUE
        } catch (Exception ex) {
            ProcessNode.withSession {
                // clear the session, because failed object during saving should be removed from session
                it.clear()
            }
            log.warn "Cannot create lock for process node [$node.id] execution: probably it was created by another process"

            return Boolean.FALSE
        }
    }

    @Synchronized("lock")
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean unlockProcessExecution(ProcessNode node)  {
        boolean isLockRemoved = GrailsflowLock.executeUpdate('DELETE GrailsflowLock WHERE process = ? AND nodeID = ?', [node.process, node.nodeID])

        if (!isLockRemoved) {
            log.debug("No Grailsflow lock for process ${node.process.id} and node #${node.nodeID} was found!")
        }

        return isLockRemoved
    }

    @Synchronized("lock")
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean removeLocksForStoppedClusters()  {
        log.debug("Starting check for stopped clusters")
        try {
            Date now = new Date()
            List<ClusterInfo> clusters = ClusterInfo.list()
            List<ClusterInfo> stoppedList = new ArrayList()
            clusters?.each() { clusterInfo ->
                // check when the cluster is last fired
                log.debug("Checking cluster [${clusterInfo.clusterName}] with repeat interval value [${clusterInfo.repeatInterval}]")
                Date clusterExpiredTime = new Date(now.time-clusterInfo.repeatInterval*2)
                if (clusterInfo.lastCheckedOn < clusterExpiredTime) {
                    GrailsflowLock.findAllByClusterName(clusterInfo.clusterName)*.delete()
                    stoppedList << clusterInfo
                }
            }
            log.debug("Removing locks for stopped clusters: "+stoppedList*.clusterName)
            if (!stoppedList.isEmpty()) {
                stoppedList*.delete(flush: true)
            }

        } catch(Exception ex) {
            log.error(ex)
            return Boolean.FALSE
        }
        return Boolean.TRUE
    }

    @Synchronized("lock")
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean removeExpiredLocks(Long expiredInterval)  {
        log.debug("Starting removing expired locks for interval [${expiredInterval}]")
        try {
            List<GrailsflowLock> expiredLocks = getOverdueLocks(expiredInterval)
            log.debug("Delete expired locks [${expiredLocks}]")
            expiredLocks*.delete()
        } catch (Exception exception) {
            log.error("Exception during expired locks deletion", exception)
            return Boolean.FALSE
        }
        return Boolean.TRUE
    }

    @Synchronized("lock")
    @Transactional(propagation = Propagation.REQUIRED)
    public List<GrailsflowLock> getExpiredLocks(Long expiredInterval)  {
        try {
            List<GrailsflowLock> expiredLocks = getOverdueLocks(expiredInterval)
            log.debug("Getting expired locks: [${expiredLocks}]")
            return expiredLocks
        } catch (Exception exception) {
            log.error("Exception during expired locks deletion", exception)
            return null
        }
    }


    @Synchronized("lock")
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateClusterInfo()  {
        Date now = new Date()
        String currentClusterName = (grailsApplication.config.grailsflow.clusterName instanceof Closure) ?
            grailsApplication.config.grailsflow.clusterName()?.toString() :
            grailsApplication.config.grailsflow.clusterName?.toString()
        log.debug("Updating cluster information for cluster [${currentClusterName}] ")
        ClusterInfo clusterInfo = ClusterInfo.findByClusterName(currentClusterName)
        if (!clusterInfo) {
            def clusterRepeatIntervalValue = (grailsApplication.config.grailsflow.scheduler.clusterChecker.repeatInterval instanceof Closure) ?
                grailsApplication.config.grailsflow.scheduler.clusterChecker.repeatInterval() :
                grailsApplication.config.grailsflow.scheduler.clusterChecker.repeatInterval

            Long clusterRepeatInterval = ConfigurableSimpleTrigger.DEFAULT_REPEAT_INTERVAL

            if (clusterRepeatIntervalValue) {
                try {
                    clusterRepeatInterval = Long.valueOf(clusterRepeatIntervalValue.toString())
                } catch (NumberFormatException nfe) {
                    log.error("Exception during converting ReleatInterval to Long value", nfe)
                    clusterRepeatInterval = ConfigurableSimpleTrigger.DEFAULT_REPEAT_INTERVAL
                }
            }

            clusterInfo = new ClusterInfo(clusterName:  currentClusterName, lastCheckedOn: now, repeatInterval: clusterRepeatInterval)
        } else {
            clusterInfo.lastCheckedOn = now
        }
        if (!clusterInfo.save(flush: true)) {
            log.debug("Cluster Information was not updated: ${clusterInfo.errors}")
            return Boolean.FALSE
        }
        return Boolean.TRUE
    }

    private List<GrailsflowLock> getOverdueLocks(Long expiredInterval) {
        Date now = new Date()
        if (now.time <= expiredInterval) {
            return null
        }
        List<GrailsflowLock> locks = GrailsflowLock
            .findAllByLockedOnLessThan(new Date(now.time-expiredInterval))
        log.debug("Overdue locks for expiredInterval [${expiredInterval}] are [${locks}]")
        return locks
    }
}
