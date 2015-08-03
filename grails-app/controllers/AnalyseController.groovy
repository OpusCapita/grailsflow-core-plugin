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

import com.jcatalog.grailsflow.utils.ConstantUtils

import grails.converters.JSON
/**
 * AnalyseController provides some reports information.
 * It is prepared statistic information about each process type, such
 * as executed nodes, how many times it was executed, the fastest and
 * longest node execution times, etc.
 *
 * @author Stephan Albers
 * @author Alexander Shulga
 */
class AnalyseController extends GrailsFlowSecureController {
    private static final String SORT_BY_NODE = "SORT_BY_NODE"
    private static final String SORT_BY_AVERAGE_TIME = "SORT_BY_AVERAGE_TIME"
    private static final String SORT_BY_MIN_TIME = "SORT_BY_MIN_TIME"
    private static final String SORT_BY_MAX_TIME = "SORT_BY_MAX_TIME"
    private static final String SORT_BY_INTERACTIVE_NODE = "SORT_BY_INTERACTIVE_NODE"
    private static final String SORT_BY_NONINTERACTIVE_NODE = "SORT_BY_NONINTERACTIVE_NODE"

    def processManagerService
    def processProtocolingService

    def index = {
        redirect(action: "analyseResponse")
    }

    def analyseResponse = {
        render(view: 'analyseResponse',
            model: [processClasses: processManagerService.supportedProcessClasses])
    }

    def searchNodesInfo = {
        def processNodes = []

        def query = "select pn from com.jcatalog.grailsflow.model.process.ProcessNode pn inner join pn.process as p where p.type = ?"
        def nodes = ProcessNode.executeQuery(query, [params.type])

        def nodesAnalyse = [:]

        nodes.each() {node ->
            if (node.finishedOn) {
                def nodeInfo
                if (!nodesAnalyse[node.nodeID]) {
                    nodeInfo = [:]
                    nodeInfo.nodeID = node.nodeID
                    nodeInfo.label = processManagerService.getProcessClass(node.process.type)?.label
                    nodeInfo.type = node.type
                } else {
                    nodeInfo = nodesAnalyse[node.nodeID]
                }

                if (nodeInfo.maxTime) {
                    if ((node.finishedOn.time - node.startedOn.time) > nodeInfo.maxTime) {
                        nodeInfo.maxTime = (node.finishedOn.time - node.startedOn.time)
                        nodeInfo.processMaxTime = node.process.id
                    }
                } else {
                    nodeInfo.maxTime = (node.finishedOn.time - node.startedOn.time)
                    nodeInfo.processMaxTime = node.process.id
                }
                if (nodeInfo.minTime) {
                    if ((node.finishedOn.time - node.startedOn.time) < nodeInfo.minTime) {
                        nodeInfo.minTime = (node.finishedOn.time - node.startedOn.time)
                        nodeInfo.processMinTime = node.process.id
                    }
                } else {
                    nodeInfo.minTime = (node.finishedOn.time - node.startedOn.time)
                    nodeInfo.processMinTime = node.process.id
                }
                nodeInfo.quantity = nodeInfo.quantity ?
                    nodeInfo.quantity + 1 : new Integer(1)
                nodeInfo.totalTime = nodeInfo.totalTime ?
                    (node.finishedOn.time - node.startedOn.time) + nodeInfo.totalTime :
                    (node.finishedOn.time - node.startedOn.time)
                nodeInfo.averageTime = nodeInfo.totalTime/nodeInfo.quantity

                nodesAnalyse[node.nodeID] = nodeInfo
            }
        }

        processNodes.addAll(nodesAnalyse.values())

        // sorting
        if (params.sortBy == SORT_BY_NODE) {
            processNodes.sort {a, b ->
                if (a.nodeID > b.nodeID) return 1; else
                    if (a.nodeID == b.nodeID) return 0; else
                        if (a.nodeID < b.nodeID) return -1;}
        } else if (params.sortBy == SORT_BY_AVERAGE_TIME) {
            processNodes.sort {a, b ->
                if (a.averageTime > b.averageTime) return 1; else
                    if (a.averageTime == b.averageTime) return 0; else
                        if (a.averageTime < b.averageTime) return -1;}
        } else if (params.sortBy == SORT_BY_MIN_TIME) {
            processNodes.sort {a, b ->
                if (a.minTime > b.minTime) return 1; else
                    if (a.minTime == b.minTime) return 0; else
                        if (a.minTime < b.minTime) return -1;}
        } else if (params.sortBy == SORT_BY_MAX_TIME) {
            processNodes.sort {a, b ->
                if (a.maxTime > b.maxTime) return 1; else
                    if (a.maxTime == b.maxTime) return 0; else
                        if (a.maxTime < b.maxTime) return -1;}
        } else if (params.sortBy == SORT_BY_INTERACTIVE_NODE) {
            processNodes.sort {a, b ->
                def type1 = (a.type == ConstantUtils.NODE_TYPE_WAIT ? 1 : 0)
                def type2 = (b.type == ConstantUtils.NODE_TYPE_WAIT ? 1 : 0)
                if (type1 > type2) return -1; else
                    if (type1 == type2) return 0; else
                        if (type1 < type2) return 1;
            }
        } else {
            processNodes.sort {a, b ->
                def type1 = (a.type == ConstantUtils.NODE_TYPE_WAIT ? 1 : 0)
                def type2 = (b.type == ConstantUtils.NODE_TYPE_WAIT ? 1 : 0)
                if (type1 > type2) return 1; else
                    if (type1 == type2) return 0; else
                        if (type1 < type2) return -1;
            }
        }

        def processTypeProtocol = processProtocolingService
                                      .getProcessExecutionProtocol(params.type)
        
        render(view: 'analyseResponse',
               model: [processClasses: processManagerService.supportedProcessClasses,
                       processNodes: processNodes,
                       processTypeProtocol: processTypeProtocol ?  processTypeProtocol as JSON : null,
                       params: [type: params.type, sortBy: params.sortBy] ])
    }

}