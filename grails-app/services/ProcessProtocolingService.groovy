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
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import com.jcatalog.grailsflow.model.process.BasicProcess
import com.jcatalog.grailsflow.model.definition.ProcessDef
import com.jcatalog.grailsflow.engine.concurrent.ProcessTypeLock
import org.apache.commons.lang.StringUtils

/**
 * ProcessProtocolingService class is used for preparing process execution
 * information
 *
 * @author Stephan Albers
 * @author July Karpey
 * 
 */

class ProcessProtocolingService {
    def processManagerService
    def generateProcessService

    /**
     * return result is a complex map of values.
     * 
     * Result has the following structure:
     *  [ processId1: protocolGroups1,
     *    processId2: protocolGroups2, ...]
     *
     *  where protocolGroups is a map of entries
     *  [protocolGroupName: nodes]
     *
     *  where nodes is a list of the following entries:
     *  ["nodeKey": node.id, "nodeID": node.nodeID, "type": node.type,
     *   "startedOn": node.startedOn, "finishedOn": node.finishedOn, 
     *   "caller": node.caller, "executionTime"]
     *
     */
    def getProcessExecutionProtocol(String processType) {
        log.debug("Starting preparing execution protocol for processes with type '${processType}'")
        if (!processType) return null

        def startedProcesses = BasicProcess.findAllByType(processType)
        if (!startedProcesses || startedProcesses.isEmpty()) {
            log.debug("There are no started processes with type '${processType}'")
            return null
        }

        def result = []
        startedProcesses.each() { process ->
            def nodes = process.nodes.findAll(){ it.protocolGroup != null}
            if (nodes){
                def protocolGroups = [:]
                nodes.each() { node ->
                    if (!protocolGroups[node.protocolGroup]) protocolGroups[node.protocolGroup] = []
                    protocolGroups[node.protocolGroup] << ["nodeKey": node.id, "nodeID": node.nodeID,
                            "type": node.type,"startedOn": node.startedOn,
                            "finishedOn": node.finishedOn, "caller": node.caller,
                            "executionTime": node.finishedOn ? node.finishedOn.time - node.startedOn.time : 0]
                    
                }

                result << ["processId": process.id, "protocolGroups": protocolGroups]

            } else {
                log.debug("There are no protocoled nodes for started process '${process?.id}' with type '${processType}'")
            }
        }

        return result
    }

    /**
     * Returns map of protocol groups:
     *  [protocolGroupName: nodes]
     *
     *  where nodes is a list of the following entries:
     *  ["nodeKey": node.id, "nodeID": node.nodeID, "type": node.type,
     *   "startedOn": node.startedOn, "finishedOn": node.finishedOn,
     *   "caller": node.caller, "executionTime"]
     */
    def getProcessExecutionProtocol(BasicProcess process) {
        def protocolGroups = [:]

        def nodes = process.nodes.findAll() {it.protocolGroup != null}
        if (nodes) {
            nodes.each() { node ->
                if (!protocolGroups[node.protocolGroup]) {
                    protocolGroups[node.protocolGroup] = []
                }
                protocolGroups[node.protocolGroup] << ["nodeKey": node.id, "nodeID": node.nodeID,
                        "type": node.type, "startedOn": node.startedOn,
                        "finishedOn": node.finishedOn, "caller": node.caller,
                        "executionTime": node.finishedOn ?
                            node.finishedOn.time - node.startedOn.time :
                            System.currentTimeMillis() - node.startedOn.time]
            }
        } else {
            log.debug("There are no protocoled nodes for started process '${process?.id}' with type '${process.type}'")
        }

        return protocolGroups
    }

    /**
     * Find all protocol groups defined for all nodes of all processTypes. Collect all unique not empty protocol groups
     * per each processType.
     * @return map of processType to unique list of protocolGroups for the processType.
     */
    def getProtocolGroups(){
        def processTypeToProtocolGroups = [:]
        processManagerService.getSupportedProcessClasses()?.each{ processClass ->
            def processDef
            def protocolGroups = []
            synchronized(ProcessTypeLock.getLock(processClass.processType)){
                processDef = ProcessDef.findWhere(processID: processClass.processType)
                if (!processDef) {
                    processDef = generateProcessService.buildProcessDefinition(processClass)
                }
            }
            processDef.nodes.each{ node ->
                if (node.protocolGroup){
                    protocolGroups << node.protocolGroup
                }
            }
            protocolGroups.unique()
            processTypeToProtocolGroups.put(processClass.processType, protocolGroups)
        }
        return processTypeToProtocolGroups
    }
}