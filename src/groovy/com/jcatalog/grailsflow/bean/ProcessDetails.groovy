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

import com.jcatalog.grailsflow.model.process.BasicProcess

/**
 * ProcessDetails bean represents bean for ProcessDetails and ProcessList UI
 *
 * @author Maria Voitovich
 */

class ProcessDetails extends BeanProxySupport {
  private List<BeanProxySupport> nodes
  private Map<String, BeanProxySupport> variables

  /**
   * DO NOT USE CONSTRUCTOR OUTSIDE OF GRAILSFLOW PLUGIN! SIGNATURE/IMPLEMENTATION MAY CHANGE
   */
  public ProcessDetails(BasicProcess basicProcess, def processClass) {
    super([
       ["id", "type", "status", "createdOn", "createdBy",
               "lastModifiedOn", "lastModifiedBy",
               "finishedOn", "finishedBy"]: basicProcess,
       ["label", "description"]: processClass
    ])

    def nodesDetails = []
    basicProcess?.nodes?.sort(){ it.id }.each () { processNode ->
      def nodeDef = processClass ? processClass.nodes[processNode.nodeID] : null
      def nodeDetailsDelegates = BeanProxySupport.createDelegatesMap()
      nodeDetailsDelegates.put(["id", "nodeID", "status", "caller", "startedOn", "startedExecutionOn", "finishedOn", "dueOn", "assignees" ], processNode)
      nodeDetailsDelegates.put(["label", "description", "type"], nodeDef)
      nodesDetails << new BeanProxySupport(nodeDetailsDelegates)
    }
    this.nodes = nodesDetails

    def variablesDetails = new LinkedHashMap()

    basicProcess?.variables?.sort() {it.id}.each() { processVariable ->
      def variableDef = processClass ? processClass.variables.find {it.name == processVariable.name} : null
      def variableDetailsDelegates = BeanProxySupport.createDelegatesMap()
      variableDetailsDelegates.put(["name", "value", "variableValue"], processVariable)
      variableDetailsDelegates.put(["label", "description", "type"], variableDef)
      variablesDetails.put(processVariable.name, new BeanProxySupport(variableDetailsDelegates))
    }

    this.variables = variablesDetails
  }

  public List<BeanProxySupport> getNodes() {
    return this.nodes
  }

  public Map<String, BeanProxySupport> getVariables() {
    return this.variables
  }

}
