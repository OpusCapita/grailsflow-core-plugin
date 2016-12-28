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

/**
 * Process Transition Definition class
 *
 * Describes the process transition definition
 *
 * NOTE: Use 'toNodes' in code instead of 'transition2DestinationNodes'
 *
 * @author Stephan Albers
 * @author July Karpey
 * @author Maria Voitovich
 */
class ProcessTransitionDef {
    String event

    Map label = [:]

    static belongsTo = [ fromNode: ProcessNodeDef ]
    static hasMany = [ transition2DestinationNodes: Transition2DestinationNode ]

    static transients = [ "toNodes", "translationsKey" ]

    static constraints = {
        event(unique: 'fromNode' )
    }

    static mapping = {
        transition2DestinationNodes cascade: "all,delete-orphan"
        label indexColumn:[name:"language", type:String, length:5],joinTable:[key:'process_transition_def_id', column:'label'],length:20
    }
    
    // Used to write and parse process class
    String getTranslationsKey() {
      return "${fromNode.nodeID}_${event}".toString()
    }

    // NOTE: Use 'toNodes' in code instead of 'transition2DestinationNodes'
    Set getToNodes() {
      return transition2DestinationNodes ? transition2DestinationNodes*.destinationNode : []
    }

    // NOTE: Use 'toNodes' in code instead of 'transition2DestinationNodes'
    void setToNodes(Set toNodes) {
      if (transition2DestinationNodes) {
	      this.transition2DestinationNodes.each() {
	        if (it.destinationNode) {
	          it.destinationNode.removeFromTransitions2DestinationNode(it)
	        }
	      }
        transition2DestinationNodes.clear()
      } else {
        transition2DestinationNodes = []
      } 
      toNodes?.each() { toNode ->
        def transition2DestinationNode = new Transition2DestinationNode(transition: this, destinationNode: toNode)
        toNode.addToTransitions2DestinationNode(transition2DestinationNode)
        transition2DestinationNodes << transition2DestinationNode
      }
    }

    void removeFromAssociations() {
      this.transition2DestinationNodes?.each() {
        if (it.destinationNode) {
          it.destinationNode.removeFromTransitions2DestinationNode(it)
        }
        it.transition?.removeFromTransition2DestinationNodes(it)
      }

      this.save(flush: true)
      this.fromNode?.removeFromTransitions(this)
    }

}
