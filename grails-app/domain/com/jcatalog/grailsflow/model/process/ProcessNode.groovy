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
package com.jcatalog.grailsflow.model.process

import com.jcatalog.grailsflow.model.definition.ProcessNodeDef
import org.hibernate.SQLQuery
import org.hibernate.Session

/**
 * Process Node represents a node for the process.
 *
 * @author Stephan Albers
 * @author July Antonicheva
 * @author Maria Voitovich
 */
class ProcessNode {
    String branchID = "0"  // 0 for "root" branch
    String nodeID
    String type

    String caller

    FlowStatus status

    Date startedOn
    Date startedExecutionOn
    Date finishedOn
    Date dueOn
    String description

    String event
    ProcessNodeException exception

    String protocolGroup

    static belongsTo = [ process: BasicProcess ]

    static hasMany = [ nextNodes : ProcessNode ]

    static constraints = {
        caller(nullable:true)
        startedExecutionOn(nullable:true)
        finishedOn(nullable:true)
        dueOn(nullable:true)
        description(nullable:true)
        exception(nullable:true)
        event(nullable:true)
        protocolGroup(nullable:true)
    }

    static mapping = {
      status index: 'IDX_PROCESS_NODE_1'
      nodeID index:'IDX_PROCESS_NODE_3'
      process index:'IDX_PROCESS_NODE_3'
      nextNodes joinTable:[name:'process_node_transition', key:'from_node', column: "to_node"]
    }

    public ProcessNode() {
    }


    public ProcessNode(ProcessNodeDef nodeDef) {
      this.nodeID = nodeDef.nodeID
      this.type = nodeDef.type
      this.protocolGroup = nodeDef.protocolGroup

    }

    static transients = ["assignees", 'previousNodes']

    public Collection<String> getAssignees() {
      return process?.assignees?.findAll() { it.nodeID == this.nodeID }?.collect() { it.assigneeID } ?: []
    }

    Collection<ProcessNode> getPreviousNodes() {
        List<Long> previousNodesIds = []
        withSession { Session session ->
            SQLQuery query = session.createSQLQuery('select distinct pnt.from_node from process_node_transition as pnt where pnt.to_node = :currentNodeId')
            query.setParameter('currentNodeId', this.id)
            previousNodesIds = query.list().collect { it as Long } // need to explicitly cast values to Long because it returns BigInteger
        }
        if (!previousNodesIds) {
            return []
        }
        return executeQuery("from ProcessNode pn where pn.id in (:previousNodesIds)", [previousNodesIds: previousNodesIds])
    }
}
