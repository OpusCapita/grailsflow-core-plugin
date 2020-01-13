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
import com.jcatalog.grailsflow.model.process.FlowStatus
import com.jcatalog.grailsflow.model.process.ProcessNode
import com.jcatalog.grailsflow.utils.ConstantUtils
import com.jcatalog.grailsflow.status.NodeStatusEnum
import org.hibernate.FetchMode
import org.hibernate.SQLQuery
import grails.util.Holders
import com.jcatalog.grailsflow.scheduling.triggers.ConfigurableSimpleTrigger
import org.springframework.util.StopWatch

/**
 * NodeActivatorJob class is used for activating automatic process nodes
 * by sending empty events.
 *
 * Checks all active nodes and invokes their execution.
 *
 * It is possible that several external applications work together 
 * and have Grailsflow installed. If several applications share the same DB the job
 * running in one application must never invoke events for processes started from another
 * applications. 
 * Therefore, it is important to specify 'appExternalID' bean in Spring configuration.
 * The 'appExternalID' bean is a String Object that gives an additional restriction
 * for NodeActivatorJob class. This restriction gives possibility to execute nodes that
 * where activated in a defined 'external' application.
 *
 *
 * @author Stephan Albers
 * @author July Antonicheva
 * @author Ivan Baidakou
 */

class NodeActivatorJob {
    static triggers = {
        def nodeActivator = Holders.config.grailsflow.scheduler.nodeActivator
        if (nodeActivator && nodeActivator.containsKey(ConfigurableSimpleTrigger.AUTO_START)) {
            if (nodeActivator.get(ConfigurableSimpleTrigger.AUTO_START)) {
                custom name: 'nodeActivator', triggerClass: ConfigurableSimpleTrigger
            }
        } else {
            custom name: 'nodeActivator', triggerClass: ConfigurableSimpleTrigger
        }
    }

    def group = "GRAILSFLOW"
    def concurrent = false

    def processManagerService
    def threadRuntimeInfoService
    def grailsflowLockService
    def appExternalID
    def sessionFactory
    def grailsApplication

    def execute(){
        StopWatch sw
        if (log.debugEnabled) {
            sw = new StopWatch('NodeActivatorJob')
        }
        try{
            if (log.debugEnabled) {
                sw?.start('Finding active and running FlowStatuses')
            }
            Long activatedStatusKey = FlowStatus.findByStatusID(NodeStatusEnum.ACTIVATED.value())?.id
            Long runningStatusKey = FlowStatus.findByStatusID(NodeStatusEnum.RUNNING.value())?.id
            if (log.debugEnabled) {
                sw?.stop()
                sw?.start('Finding active node keys')
            }
            SQLQuery activeNodesQuery = sessionFactory.currentSession.createSQLQuery("""
                    SELECT n.id FROM process_node n
                    INNER JOIN basic_process p ON n.process_id = p.id
                    WHERE p.app_groupid = :appExternalID
                      AND n.status_id IN ($activatedStatusKey, $runningStatusKey)
                      AND n.type      IN (:types)
                    ORDER BY n.started_on ASC""")
            activeNodesQuery.setParameter('appExternalID', appExternalID)
            activeNodesQuery.setParameterList('types', [ConstantUtils.NODE_TYPE_ACTIVITY, ConstantUtils.NODE_TYPE_FORK,
                                                        ConstantUtils.NODE_TYPE_ORJOIN, ConstantUtils.NODE_TYPE_ANDJOIN])
            List activeNodesKeys = activeNodesQuery.list()

            log.info "*** Amount of Nodes to execute ${activeNodesKeys.size()} ***"

            if (log.debugEnabled) {
                sw?.stop()
            }
            def nodesComparator

            if (activeNodesKeys) {
                if (log.debugEnabled) {
                    sw?.start('Loading ProcessNodes by keys')
                }

                List<ProcessNode> activeNodes = []
                activeNodesKeys.each {
                    activeNodes << ProcessNode.get(it)
                }

                if (log.debugEnabled) {
                    sw?.stop()
                    sw?.start('Sorting ProcessNodes')
                }
                // compare nodes according to configured nodes comparator
                nodesComparator = grailsApplication.config.grailsflow.nodeActivator.comparator
                if (nodesComparator) {
                    activeNodes.sort(nodesComparator)
                }

                if (log.debugEnabled) {
                    sw?.stop()
                    sw?.start('Sending events for ProcessNodes')
                }
                activeNodes.each { ProcessNode node ->
                    sendEvent(node)
                }
                if (log.debugEnabled) {
                    sw?.stop()
                }
            }

            if (log.debugEnabled) {
                sw?.start('Finding manual nodes with running state')
            }

            // restart the execution of manual nodes if they were interrupted by Server
            SQLQuery waitNodesQuery = sessionFactory.currentSession.createSQLQuery("""
                    SELECT n.id FROM process_node n
                    INNER JOIN basic_process p ON n.process_id = p.id
                    WHERE p.app_groupid = :appExternalID
                      AND n.status_id   = $runningStatusKey
                      AND n.type        = :type
                    ORDER BY n.id ASC""")
            waitNodesQuery.setParameter('appExternalID', appExternalID)
            waitNodesQuery.setParameter('type', ConstantUtils.NODE_TYPE_WAIT)
            List waitNodesKeys = waitNodesQuery.list()

            if (log.debugEnabled) {
                sw?.stop()
                sw?.start('Processing manual nodes in running state')
            }
            if (waitNodesKeys) {
                log.info "*** Amount of manual nodes that are in running state: ${waitNodesKeys.size()} ***"

                List<ProcessNode> waitNodes = waitNodesKeys.collect { ProcessNode.get(it) }
                nodesComparator = activeNodesKeys ? nodesComparator : grailsApplication.config.grailsflow.nodeActivator.comparator
                if (nodesComparator) {
                    waitNodes = waitNodes.sort(nodesComparator)
                }
                waitNodes.each { ProcessNode node ->
                    if (!threadRuntimeInfoService.isExecutingOrRecentlyFinished(node.process.id)) {
                        sendEvent(node)
                    }
                }
            }
            if (log.debugEnabled) {
                sw?.stop()
            }
            threadRuntimeInfoService.clear()
        } catch (Throwable ex){
            log.error("Unexpected Problems appear during NodeActivatorJob execution.",ex)
            sessionFactory.currentSession.clear()
        }
        if (log.debugEnabled && sw) {
            log.debug sw.prettyPrint()
        }
    }

    private void sendEvent(ProcessNode node) {
        // check if the node is executed in separate thread or recently finished. if no - execute it
        if (grailsflowLockService.lockProcessExecution(node)) {
            String namePrefix = "#${node.process?.id}(${node.process?.type})-${node.nodeID}"

            log.info "*** No Thread Info for thread [${namePrefix}]: sending event [${node.event}] to node [${node.nodeID}] ***"
            processManagerService.sendEvent(node.process, node, node.event, node.caller)
            grailsflowLockService.unlockProcessExecution(node)
        }
    }
}
