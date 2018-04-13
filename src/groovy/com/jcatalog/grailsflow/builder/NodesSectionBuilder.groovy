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

package com.jcatalog.grailsflow.builder;

import org.codehaus.groovy.runtime.InvokerHelper

import com.jcatalog.grailsflow.utils.AuthoritiesUtils

import com.jcatalog.grailsflow.model.definition.ProcessNodeDef
import com.jcatalog.grailsflow.model.definition.ProcessDefAssignee

import java.text.SimpleDateFormat
import java.text.ParseException
/**
 * NodesSectionBuilder class is used to construct Nodes from nodes closure of process definition file.
 * Nodes closure name should be same as process class name. 
 *
 * The result of NodesSectionBuilder work is:
 *   - nodes: 				list of ProcessNodeDef objects
 *   - assignees: 		Map of nodeID -> [ ProcessDefAssignee ]
 *   - varVisibility: Map of nodeID -> [ variableName -> variableVisibility ]
 *   - transitions:   Map of nodeID -> [ event -> [destination nodes IDs] ]
 *   - actionClosures: Map of nodeID -> action closure
 *
 * @author July Antonicheva
 * @author Maria Voitovich
 */
class NodesSectionBuilder extends AbstractSectionBuilder {
    static final String ASSIGNEES_CLOSURE = "assignees"
    static final String VARIABLES_CLOSURE = "variable"
    static final String ACTIONS_CLOSURE = "action"
    static final String EVENT_CLOSURE = "on"
    static final String VALIDFROM_CLOSURE = "validFrom"
    static final String VALIDTO_CLOSURE = "validTo"

    static final def DATE_FORMATTER = new SimpleDateFormat("yyyy.MM.dd")

    private String nodesSectionName
    private def currentEvent

    private def nodes
    private def assignees
    private def validRange
    private def varVisibility
    private def transitions
    private Map<String, Closure> nodeActions
    
    public def getNodes(){
      return this.nodes
    } 

    public def getAssignees(){
      return this.assignees
    }

    public def getValidRange(){
      return this.validRange
    }

    public def getVarVisibiliry(){
      return this.varVisibility
    } 

    public def getTransitions(){
      return this.transitions
    } 

    public Map<String, Closure> getNodeActions(){
      return this.nodeActions
    } 

    public NodesSectionBuilder(def process) {
      this.nodesSectionName = process.class.simpleName
      this.nodes = new ArrayList()
      this.validRange = new Expando()  
      this.assignees = [:]       // NodeID -> [ ProcessDefAssignee ]
      this.varVisibility = [:]   // NodeID -> [variable name -> visibility type] 
      this.nodeActions = [:]    // NodeID -> action closure
      this.transitions = [:]     // NodeID -> [event -> [destination nodes IDs] ]
      build(process)
    }

    public List<String> getSupportedSections() {
      return [ nodesSectionName ]
    }

    protected Object createNode(Object name) {
        return createNode(name, null, null)
    }
    protected Object createNode(Object name, Object value){
        return createNode(name, null, value)
    }
    protected Object createNode(Object name, Map attributes) {
        return createNode(name, attributes, null)
    }

    protected Object createNode(Object name, Map attributes, Object value) {
      if (name == ASSIGNEES_CLOSURE) {
        log.debug("Building assignees for ${current ? 'node '+current : 'process'} from ${attributes}")
        this.assignees[current ?: nodesSectionName] = createAssignees(attributes)
        return null
      } else if (name == VARIABLES_CLOSURE) {
        log.debug("Building variables visibility for node ${current} from ${attributes}")
        this.varVisibility[current] = createVisibility(attributes)
        return null
      } else if (name == EVENT_CLOSURE && current != ACTIONS_CLOSURE) {
        log.debug("Building event for node ${current} from ${value}")
        if (!this.transitions[current]) {
          this.transitions[current] = [:]
        }
        this.transitions[current].put(value, new HashSet())
        currentEvent = value
        // closure for executing ".to()" method for created event
        def toMethod = { destinationNodes ->
            if (this.currentEvent) {
                log.debug("Building transitions for node ${this.current} and event ${this.currentEvent} from ${destinationNodes}")
                if (destinationNodes) {
                  this.transitions[this.current][this.currentEvent].addAll(destinationNodes)
                } else {
                  log.warn("No destination nodes specified for event ${this.currentEvent} of node ${this.current}.")
                  log.warn("Event ${this.currentEvent} removed from ${this.current} node events.")
                  this.transitions[this.current].remove(this.currentEvent)
                }
                this.currentEvent = null
            }
          }
        return [ to: toMethod ]
      } else if (name == VALIDFROM_CLOSURE) {
        log.debug("Building ValidFrom date for ${current} from ${value}")
        return this.validRange.validFrom = createValidRange(value)
      } else if (name == VALIDTO_CLOSURE) {
        log.debug("Building ValidTo date for ${current} from ${value}")
        return this.validRange.validTo = createValidRange(value)
      } else if (current == null) {
        log.debug("Building node ${name} from ${attributes}")
        def nodeDef = createProcessNode(name.toString(), attributes)
        this.nodes.add(nodeDef)
        return nodeDef.nodeID
      } else {
        log.warn("Incorrect DSL syntax at node ${name} with attributes ${attributes} and value ${value.inspect()}")
        return null
      }
      return null
    }

    protected void setParent(Object parent, Object child) { }
    
    // overwritten to store action closures
    public Object invokeMethod(String methodName, Object args){
      if (current != null && methodName == ACTIONS_CLOSURE) {
        List list = InvokerHelper.asList(args);
        if (list.size() == 1) {
            nodeActions[current] = args[0]
        } else if (list.size() == 2) {
            nodeActions[current] = args[1]
        }
        return null
      } else {
        return super.invokeMethod(methodName, args)
      }
    }

    private Date createValidRange(String value) {
        def validRange
        try {
            validRange = DATE_FORMATTER.parse(value)
        } catch (ParseException e) {
            log.error("Cannot parse date ${value}: $e")
        }
        return validRange
    }    
    
    private createVisibility(Map attributes) {
      return attributes
    }    
    
    private createAssignees(Map attributes) {
      def assignees = []
      if (attributes) {
        def users = attributes.users
        def roles = attributes.roles
        def groups = attributes.groups
        assignees += AuthoritiesUtils.getUserAuthorities(users)
        assignees += AuthoritiesUtils.getRoleAuthorities(roles)
        assignees += AuthoritiesUtils.getGroupAuthorities(groups)
        def otherAttributes = attributes.keySet()-["users", "roles", "groups"]
        if (otherAttributes.size()) {
          log.warn("Unsupported assignees types: ${otherAttributes}")
        }
      }       
      return assignees.collect() { new ProcessDefAssignee(assigneeID: it) }
    }

    private createProcessNode(String name, Map properties) {
      def processNode = new ProcessNodeDef()
      if (properties) {
          processNode.properties = properties
          if (properties.dueDate && properties.dueDate instanceof Number) {
              try {
                  processNode.dueDate = Long.valueOf(properties.dueDate as String)
              } catch (NumberFormatException e) {
                  log.error("Cannot convert Due Date number to Long value!", e)
                  processNode.dueDate = 0 as Long
              }
          }

          if (properties.forcedStart) {
              if (properties.forcedStart instanceof Boolean) {
                  processNode.forcedStart = properties.forcedStart
              } else {
                  log.error "Property 'forcedStart' should be a boolean value"
              }
          }

          if (properties.expectedDuration && properties.expectedDuration instanceof Number) {
              try {
                  processNode.expectedDuration = Long.valueOf(properties.expectedDuration as String)
              } catch (NumberFormatException e) {
                  log.error("Cannot convert Expected Duration number to Long value!", e)
                  processNode.expectedDuration = 0 as Long
              }
          }

          if (processNode.hasErrors()) {
              log.error("The Process Node for $name was updated with errors: ${processNode.errors.allErrors}")
          }
      }
      if (name.endsWith("Wait")) {
          processNode.nodeID = name.substring(0, name.lastIndexOf("Wait"))
          processNode.type = "Wait"
      } else if (name.endsWith("Fork")) {
          processNode.nodeID = name.substring(0, name.lastIndexOf("Fork"))
          processNode.type = "Fork"
      } else if (name.endsWith("AndJoin")) {
          processNode.nodeID = name.substring(0, name.lastIndexOf("AndJoin"))
          processNode.type = "AndJoin"
      } else if (name.endsWith("OrJoin")) {
          processNode.nodeID = name.substring(0, name.lastIndexOf("OrJoin"))
          processNode.type = "OrJoin"
      } else if (current == null){
          if (name.endsWith("Activity")) {
             processNode.nodeID = name.substring(0, name.lastIndexOf("Activity"))    
          } else {
             processNode.nodeID = name
          }
          processNode.type = "Activity"
      }
      return processNode
    }

}
