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
 
package com.jcatalog.grailsflow.bean;

import org.apache.commons.lang.StringUtils
import com.jcatalog.grailsflow.utils.ConstantUtils

import com.jcatalog.grailsflow.model.definition.ProcessNodeDef
import com.jcatalog.grailsflow.model.process.ProcessNode

import com.jcatalog.grailsflow.model.process.BasicProcess

import com.jcatalog.grailsflow.model.process.ProcessVariable

/**
 * NodeDetails bean represents node on NodeDetails UI
 *
 * @author Maria Voitovich
 */

class NodeDetails extends BeanProxySupport {
  private BeanProxySupport process

  private Set<String> assignees
  private Set<BeanProxySupport> events
  private Map<String, BeanProxySupport> variablesMap

  /**
   * DO NOT USE CONSTRUCTOR OUTSIDE OF GRAILSFLOW PLUGIN! SIGNATURE/IMPLEMENTATION MAY CHANGE
   *
   * constructor for building NodeDetails for Node form preview 
   */
  public NodeDetails(ProcessNodeDef processNodeDef, Map<String, Integer> varVisibility) {
    super(
          ["label", "description"]: processNodeDef,
          ["id", "nodeID", "type", "status", "caller", "startedOn", "startedExecutionOn", "finishedOn", "dueOn"] : new ProcessNode(processNodeDef)
    )

    def processDef = processNodeDef.processDef
    def processProxyDelegates = BeanProxySupport.createDelegatesMap()
    processProxyDelegates.put(["id", "status", "type",
            "createdOn", "createdBy",
            "lastModifiedOn", "lastModifiedBy",
            "finishedOn", "finishedBy"], new BasicProcess(type: processDef.processID))
    processProxyDelegates.put(["label", "description"], processDef)
    this.process = new BeanProxySupport(processProxyDelegates)

    this.assignees = processNodeDef.assignees?.collect() { it.assigneeID }

    this.events = processNodeDef.transitions?.collect() { new BeanProxySupport([["event", "label"]: it]) }
    
    varVisibility = varVisibility ?: processNodeDef.variablesVisibility

    this.variablesMap = new LinkedHashMap()
    processDef.variables?.each() { varDef ->
      def name = varDef.name
      def visibility = varVisibility[name]
      if (visibility != ConstantUtils.INVISIBLE) {
        def value = ProcessVariable.getConvertedValue(varDef.defaultValue, ProcessVariable.defineType(varDef.type))
        def variableProxyDelegates = BeanProxySupport.createDelegatesMap()

        variableProxyDelegates.put(["name", "label", "description", "type", "view", "required", "items", "subType"], varDef)
        variableProxyDelegates.put(["value", "visibility"], ["value": value, "visibility" : visibility])

        this.variablesMap.put(name, new BeanProxySupport(variableProxyDelegates))
      }
    }
  }
  
  /**
   * DO NOT USE CONSTRUCTOR OUTSIDE OF GRAILSFLOW PLUGIN! SIGNAURE/IMPLEMENTATION MAY CHANGE
   *
   * constructor for building NodeDetails for Worklist, StartNode and NodeDetails
   *
   * TODO: move worklist item to separate bean
   */
  public NodeDetails(ProcessNode processNode, def processInstance) {
      super(
          ["label", "description"]: processInstance.class.nodes[processNode.nodeID],
          ["id", "nodeID", "type", "status", "caller", "startedOn", "startedExecutionOn", "finishedOn", "dueOn"] : processNode
      )


      def processClass = processInstance.class
      def nodeDef = processClass.nodes[processNode.nodeID]
      def process = processNode.process ?: new BasicProcess("type": processClass.processType)

      def processProxyDelegates = BeanProxySupport.createDelegatesMap()
      processProxyDelegates.put(["id", "status", "type",
              "createdOn", "createdBy",
              "lastModifiedOn", "lastModifiedBy",
              "finishedOn", "finishedBy"], process)
      processProxyDelegates.put(["label", "description"], processClass)
      this.process = new BeanProxySupport(processProxyDelegates)


      this.assignees = process.assignees ?
                        process.assignees.findAll() { it.nodeID == nodeDef.nodeID }.collect() { it.assigneeID } :
                        processClass.nodes[nodeDef.nodeID].assignees?.collect() { it.assigneeID }

      this.events = nodeDef.transitions?.collect() { new BeanProxySupport([["event", "label"]: it]) }

      def processContext = processInstance.processContext

      this.variablesMap = new LinkedHashMap()
      processClass.variables?.each() { varDef ->
        def name = varDef.name
        if (processClass.isVisible(name, nodeDef?.nodeID)) {
          def visibility = processClass.varVisibility[nodeDef.nodeID]?.get(name)
          def value =  processContext[name]

          def variableProxyDelegates = BeanProxySupport.createDelegatesMap()
          variableProxyDelegates.put(["name", "label", "description", "type", "view", "required", "items", "subType"], varDef)
          variableProxyDelegates.put(["value", "visibility"], ["value": value, "visibility" : visibility])

          this.variablesMap.put(name, new BeanProxySupport(variableProxyDelegates))
        }

      }
   }
  
  public BeanProxySupport getProcess() {
    return this.process
  }

  public Set<String> getAssignees() {
    return this.assignees
  }
  
  public Set<BeanProxySupport> getEvents() {
    return this.events ? this.events : []
  }
  
  public Map<String, BeanProxySupport> getVariables() {
    return this.variablesMap
  }
  
}
