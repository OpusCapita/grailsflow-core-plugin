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
package com.jcatalog.grailsflow.model.definition

import org.hibernate.type.YesNoType

/**
 * Process Node Definition class
 *
 * Describes the process node definition from UI
 *
 * NOTE: Use 'incomingTransitions' in code instead of 'transitions2DestinationNode'
 *
 * TODO: discuss processActions
 *
 *
 * @author Stephan Albers
 * @author July Karpey
 * @author Maria Voitovich
 */
class ProcessNodeDef {
    String nodeID

    Map label = [:]
    Map description = [:]

    // actionStatements are ordered
    List actionStatements

    Long dueDate
    Long expectedDuration
    String type

    String editorType
    String externalUrl

    String protocolGroup

    boolean forcedStart

    static belongsTo = [ processDef: ProcessDef ]
    static hasMany = [ actionStatements: ActionStatement, assignees: ProcessDefAssignee, transitions: ProcessTransitionDef, transitions2DestinationNode: Transition2DestinationNode, variables2NodeVisibility: Variable2NodeVisibility ]
    static mappedBy = [ assignees: "processNodeDef", transitions: "fromNode", transitions2DestinationNode: "destinationNode", variables2NodeVisibility: "node" ]

    static transients = [ "incomingTransitions", "variablesVisibility", "forcedStart" ]

    static constraints = {
        nodeID(unique: 'processDef')
        dueDate(nullable:true)
        expectedDuration(nullable:true)
        editorType(nullable:true)
        externalUrl(nullable:true)
        protocolGroup(nullable:true)
    }

    static mapping = {
        processDef index: 'IDX_PROCESS_NODE_DEF_2'
        assignees cascade: "all,delete-orphan"
        label indexColumn:[name:"language", type:String, length:5],joinTable:[key:'process_node_def_id' ,column:'label'],length:255
        description indexColumn:[name:"language", type:String, length:5],joinTable:[key:'process_node_def_id' ,column:'description'],length:255
        actionStatements cascade: "all,delete-orphan"
        transitions cascade: "all,delete-orphan"
        transitions2DestinationNode: cascade: "all,delete-orphan"
        variables2NodeVisibility cascade: "all,delete-orphan"
        columns {
            isFinal type:YesNoType
        }
    }

    Map getVariablesVisibility() {
      def variablesVisibility = [:]
      variables2NodeVisibility.each() { 
        variablesVisibility.put(it.variable, it.visibilityType)
      }
      return variablesVisibility
    }

    // NOTE: Use 'incomingTransitions' in code instead of 'transitions2DestinationNode'
    Set getIncomingTransitions() {
      return transitions2DestinationNode ? transitions2DestinationNode*.transition : []
    }

    void removeFromAssociations() {
      this.transitions?.each() { transition ->
        transition.transition2DestinationNodes?.each() {
          if (it.destinationNode) {
           it.destinationNode.removeFromTransitions2DestinationNode(it)
          }
        }
      }
      this.transitions?.clear()

      this.transitions2DestinationNode?.each() {
        if (it.transition){
          it.transition.removeFromTransition2DestinationNodes(it)
        }
      }
      this.transitions2DestinationNode?.clear()

      this.variables2NodeVisibility?.each() {
        if (it.variable) {
          it.variable.removeFromVariable2NodesVisibility(it)
        }
      }
      this.variables2NodeVisibility?.clear()
      this.save(flush: true)
      this.processDef?.removeFromNodes(this)
    }
}
