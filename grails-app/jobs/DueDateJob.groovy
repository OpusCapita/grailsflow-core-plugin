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
import com.jcatalog.grailsflow.model.process.ProcessNode

/**
 * DueDate Job class is used for checking due dates or timeouts of
 * processes nodes that are in ACTIVATED or AWAIT_CALLBACK states.
 *
 * If node is expired, the job sends event 'overDue' (for nodes with
 * AWAIT_CALLBACK status) and 'timeout' (for nodes with ACTIVATED status). 
 *
 * It is possible that several external applications work together
 * and have Grailsflow installed. So, the job need to send events
 * only for nodes of processes that are started in its external application.
 * For this aim, it is important to specify appExternalID bean in Spring configuration.
 *
 * Job is running according to 'timeout' value.
 *
 * @author Stephan Albers
 * @author July Karpey
 *
 */
import com.jcatalog.grailsflow.utils.ConstantUtils

import com.jcatalog.grailsflow.model.process.ProcessNode
import com.jcatalog.grailsflow.model.process.FlowStatus
import org.hibernate.FetchMode
import com.jcatalog.grailsflow.status.NodeStatusEnum

class DueDateJob {
    static triggers = {
        custom name: 'nodeDueDate', triggerClass: com.jcatalog.grailsflow.scheduling.triggers.ConfigurableSimpleTrigger
    }
    def concurrent = false
    def group = "GRAILSFLOW"

    def processManagerService
    def appExternalID
    def sessionFactory
    
    def execute(){
        try{
            def activatedStatus = FlowStatus.findByStatusID(NodeStatusEnum.ACTIVATED.value())
            def awaitCallbackStatus = FlowStatus.findByStatusID(NodeStatusEnum.AWAIT_CALLBACK.value())
            def now = new Date()

            log.debug("Searching nodes that overdue at ${now}")

            // select overdue nodes form active "Wait" nodes and nodes that await for callback
            ProcessNode.withCriteria {
              and {
                createAlias("process", "p")
                eq("p.appGroupID", appExternalID)
                or {
                   eq("status", awaitCallbackStatus)
                   and {
                     eq("type", ConstantUtils.NODE_TYPE_WAIT)
                     eq("status", activatedStatus)
                   }
                }
                isNotNull("dueOn")
                lt("dueOn", now)
                fetchMode('p', FetchMode.JOIN)
              }
            }.each {
                log.debug("Node '${it.nodeID}' of process #${it.process.id} overdue at ${it.dueOn}")
                def event
                if (it.status == awaitCallbackStatus) {
                    event = "timeout"
                } else {
                     event = "overdue"
                }
                log.debug("Sending event '${event}' to overdue node '${it.nodeID}' of process #${it.process.id}")
                processManagerService.sendEvent(it.process.id,
                                                it.nodeID,
                                                event,
                                                it.caller)
            } // each
        } catch (Throwable ex){
            log.error("Unexpected Problems appear during DueDateJob execution.",ex)
            //discard any changes of persistence objects
            sessionFactory.currentSession.clear()
        }

    } // execute closure

} // class
