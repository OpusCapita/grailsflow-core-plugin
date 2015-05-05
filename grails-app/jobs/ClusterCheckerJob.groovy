import com.jcatalog.grailsflow.cluster.GrailsflowLock
import grails.util.Holders
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
 * ClusterCheckerJob class checks for expired locks and delete them
 *
 *
 * @author Stephan Albers
 * @author July Antonicheva
 */

class ClusterCheckerJob {
    static triggers = {
        def clusterChecker = Holders.config.grailsflow.scheduler.clusterChecker
        if (clusterChecker && clusterChecker.containsKey(ConfigurableSimpleTrigger.AUTO_START)) {
            if (clusterChecker.get(ConfigurableSimpleTrigger.AUTO_START)) {
                custom name: 'clusterChecker', triggerClass: ConfigurableSimpleTrigger
            }
        } else {
            custom name: 'clusterChecker', triggerClass: ConfigurableSimpleTrigger
        }
    }

    def group = "GRAILSFLOW"
    def concurrent = false

    def processManagerService
    def sessionFactory
    def grailsApplication
    def grailsflowLockService

    def execute(){
        try{
            log.info "Running ClusterCheckerJob"

            if (!grailsflowLockService.updateClusterInfo()) {
                log.error("Cluster Information was not updated due to some problems")
            }

            if (!grailsflowLockService.removeLocksForStoppedClusters()) {
                log.error("Removing locks for stopped clusters failed")
            }

            // check expired nodes -> delete locks for nodes with expired lock time
            def lockExpiredIntervalValue = (grailsApplication.config.grailsflow.clusterChecker.lockExpiredInterval instanceof Closure) ?
                grailsApplication.config.grailsflow.clusterChecker.lockExpiredInterval() :
                grailsApplication.config.grailsflow.clusterChecker.lockExpiredInterval
            long lockExpiredInterval = ConfigurableSimpleTrigger.DEFAULT_REPEAT_INTERVAL*100

            if (lockExpiredIntervalValue) {
                try {
                    lockExpiredInterval = Long.valueOf(lockExpiredIntervalValue.toString())
                } catch (NumberFormatException nfe) {
                    log.error("Exception during converting lockExpiredInterval to Long value: ${nfe.message}")
                    lockExpiredInterval = ConfigurableSimpleTrigger.DEFAULT_REPEAT_INTERVAL*100
                }

                grailsflowLockService.getExpiredLocks(lockExpiredInterval).each() { GrailsflowLock lock->
                    processManagerService.killProcess(lock.process.id, ClusterCheckerJob.getCanonicalName())
                }
                if (!grailsflowLockService.removeExpiredLocks(lockExpiredInterval)) {
                    log.error("Removing expired locks failed due to some reasons.")
                }
            }

        } catch (Throwable throwable){
            log.error("Unexpected Problems appear during ClusterCheckerJob execution.", throwable)
        }
    }

}