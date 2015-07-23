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
import com.jcatalog.grailsflow.model.process.BasicProcess
import com.jcatalog.grailsflow.model.process.FlowStatus
import com.jcatalog.grailsflow.model.process.ProcessNode
import com.jcatalog.grailsflow.engine.*
import org.springframework.core.io.Resource
import com.jcatalog.grailsflow.model.process.BasicProcess
import com.jcatalog.grailsflow.model.process.FlowStatus
import com.jcatalog.grailsflow.model.process.ProcessNode
import com.jcatalog.grailsflow.utils.ConstantUtils
import org.hibernate.FetchMode
import org.hibernate.SQLQuery
import com.jcatalog.grailsflow.status.NodeStatusEnum
import java.lang.management.ThreadInfo
import java.lang.management.ThreadMXBean
import java.lang.management.ManagementFactory

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
 * @author July Karpey
 * @author Ivan Baidakou
 */

class NodeActivatorJob {
    static triggers = {
        custom name: 'nodeActivator', triggerClass: com.jcatalog.grailsflow.scheduling.triggers.ConfigurableSimpleTrigger
    }
    def group = "GRAILSFLOW"
    def concurrent = false

    def processManagerService
    def threadRuntimeInfoService
    def appExternalID
    def sessionFactory
    
    def execute(){
        try{
            FlowStatus activeStatus = FlowStatus.findByStatusID(NodeStatusEnum.ACTIVATED.value())
            FlowStatus runningStatus = FlowStatus.findByStatusID(NodeStatusEnum.RUNNING.value())

            SQLQuery activeNodesQuery = sessionFactory.currentSession.createSQLQuery("""
                    SELECT n.id FROM process_node n
                    INNER JOIN basic_process p ON n.process_id = p.id
                    WHERE p.app_groupid = '$appExternalID'
                      AND n.status_id IN ('$activeStatus.id', '$runningStatus.id')
                      AND n.type      IN ('$ConstantUtils.NODE_TYPE_ACTIVITY ', '$ConstantUtils.NODE_TYPE_FORK',
                                          '$ConstantUtils.NODE_TYPE_ORJOIN', '$ConstantUtils.NODE_TYPE_ANDJOIN')
                    ORDER BY n.id ASC""")
            List activeNodesKeys = activeNodesQuery.list()

            log.info "*** Amount of Nodes to execute ${activeNodesKeys.size()} ***"

            activeNodesKeys.each { nodeKey ->
                ProcessNode node =  ProcessNode.get(nodeKey)
                processManagerService.sendEvent(node.process, node, null, node.caller)
            }

            // restart the execution of manual nodes if they were interrupted by Server
            SQLQuery waitNodesQuery = sessionFactory.currentSession.createSQLQuery("""
                    SELECT n.id FROM process_node n
                    INNER JOIN basic_process p ON n.process_id = p.id
                    WHERE p.app_groupid = '$appExternalID'
                      AND n.status_id   = '$runningStatus.id'
                      AND n.type        = '$ConstantUtils.NODE_TYPE_WAIT'
                    ORDER BY n.id ASC""")
            List waitNodesKeys = waitNodesQuery.list()


            if (waitNodesKeys) {
                log.info "*** Amount of Manual Nodes that are in running state: ${waitNodesKeys.size()} ***"
                waitNodesKeys.each { nodeKey ->
                    ProcessNode waitNode =  ProcessNode.get(nodeKey)
                    String namePrefix = "#${waitNode.process?.id}(${waitNode.process?.type})-${waitNode.nodeID}"
                    // check if the node is executed in separate thread or recently finished. if no - execute it
                    if(!threadRuntimeInfoService.isExecutingOrRecentlyFinished(waitNode.process.id)){
                        log.info "*** No Thread Info for thread [${namePrefix}]: sending event [${waitNode.event}] to node [${waitNode.nodeID}] ***"
                        processManagerService.sendEvent(waitNode.process, waitNode, waitNode.event, waitNode.caller)
                    }
                }
            }
            threadRuntimeInfoService.clear()
        } catch (Throwable ex){
            log.error("Unexpected Problems appear during NodeActivatorJob execution.",ex)
            sessionFactory.currentSession.clear()
        }
    }
}