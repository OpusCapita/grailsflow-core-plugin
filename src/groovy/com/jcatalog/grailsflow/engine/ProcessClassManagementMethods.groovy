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

package com.jcatalog.grailsflow.engine;

import com.jcatalog.grailsflow.utils.ConstantUtils;

import com.jcatalog.grailsflow.model.definition.ProcessNodeDef
import com.jcatalog.grailsflow.actions.ActionContext

import com.jcatalog.grailsflow.model.definition.ProcessVariableDef

import org.apache.commons.lang.StringUtils

import groovy.lang.Binding
import groovy.lang.GroovyShell

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Helper class for storing management data and methods of the process class
 *
 * @author July Antonicheva
 * @author Maria Voitovich
 */
class ProcessClassManagementMethods {
    static Log log = LogFactory.getLog(getClass())

    String processType
    Map<String, String> label 
    Map<String, String> description
    Date validFrom
    Date validTo
    LinkedHashMap<String, ProcessNodeDef> nodesMap = [:] // Map must keep items order!
    Map<String, Closure> nodeActions = [:]
    Set finalNodes = []
    def visibility = [:]
    def processAssignees = [:]
    def nodeInfos = [:]
    def processIdentifier = []
    List<ProcessVariableDef> variables = []

	  public ProcessClassManagementMethods(def processClass) {

        // processClass methods
	  
        processClass.metaClass.'static'.getProcessType << { ->
            return this.processType
        }
        processClass.metaClass.'static'.getLabel << { ->
            return this.label ? this.label : Collections.EMPTY_MAP
        }
        processClass.metaClass.'static'.getDescription << { ->
            return this.description ? this.description : Collections.EMPTY_MAP
        }
        processClass.metaClass.'static'.getValidFrom << { ->
            return this.validFrom
        }
        processClass.metaClass.'static'.getValidTo << { ->
            return this.validTo
        }

        processClass.metaClass.'static'.getStartNode << { ->
          return this.nodesMap ? this.nodesMap.values()?.iterator().next() : null
        }
        processClass.metaClass.'static'.getNodes << { ->
            return this.nodesMap ? this.nodesMap : Collections.EMPTY_MAP
        }
        processClass.metaClass.'static'.getNodesList << { ->
            return this.nodesMap ? this.nodesMap.values() : Collections.EMPTY_LIST
        }
        processClass.metaClass.'static'.getNodeActions << { ->
            return this.nodeActions ?: Collections.EMPTY_MAP
        }
        processClass.metaClass.'static'.getProcessAssignees << { ->
            return this.processAssignees ? this.processAssignees : [:]
        }
        processClass.metaClass.'static'.getNodeInfos << { ->
            return this.nodeInfos ? this.nodeInfos : [:]
        }
        processClass.metaClass.'static'.getFinalNodes << { ->
            return this.finalNodes ?: Collections.EMPTY_SET
        }
        processClass.metaClass.'static'.getVarVisibility << { ->
            return this.visibility ? this.visibility : [:]
        }
        processClass.metaClass.'static'.getProcessIdentifier << { ->
            return this.processIdentifier ? this.processIdentifier : []
        }
        processClass.metaClass.'static'.getVariables = { ->
            return this.variables ? this.variables : Collections.EMPTY_LIST
        }

        processClass.metaClass.'static'.isVisible = {variableName, nodeID ->
             def node = this.nodesMap[nodeID]
             node?.type != ConstantUtils.NODE_TYPE_WAIT || 
             (this.visibility[nodeID] &&
                   this.visibility[nodeID][variableName] != ConstantUtils.INVISIBLE)
        }

        processClass.metaClass.'static'.isWritable = {variableName, nodeID ->
             def node = this.nodesMap[nodeID]
             node?.type != ConstantUtils.NODE_TYPE_WAIT || 
             (this.visibility[nodeID] &&
                   this.visibility[nodeID][variableName] in [ConstantUtils.WRITE_READ, ConstantUtils.REQUIRED])
        }

        processClass.metaClass.'static'.isRequired = {variableName, nodeID ->
             def node = this.nodesMap[nodeID]
             node?.type == ConstantUtils.NODE_TYPE_WAIT &&
             (this.visibility[nodeID] &&
                   this.visibility[nodeID][variableName] == ConstantUtils.REQUIRED)
        }

        // process instance methods
        
        processClass.metaClass.gotoNode = {event, nodeID ->
            def nodeDef = this.nodesMap[nodeID]
            if (!nodeDef) return null
            def transition = nodeDef.transitions?.find() { it.event == event }
            if (transition) {
              return transition.toNodes*.nodeID
            } else {
              return null
            }
            return null
        }
        
        processClass.metaClass.getProcessContext = { ->
            def context = [:]
            this.variables.each { varDef ->
                def value = delegate[varDef.name]
                context.put(varDef.name, value)
            }
            return context
        }

        processClass.metaClass.evaluateExpression = { expressionString ->
          log.debug("Evaluating expression: ${expressionString?.inspect()}")
          if (!expressionString) {
            return null
          } else if (! expressionString.contains("#")) {
            return expressionString
          }
          def expr = StringUtils.replace(expressionString.inspect(), '#', '$')
          try {
              def contextVariables = delegate.processContext
              Binding binding = new Binding(contextVariables)
              GroovyShell gs = new GroovyShell(binding)
              def result = gs.evaluate(expr)
              log.debug("Evaluated expression ${expressionString?.inspect()} value: ${result?.inspect()}")
              return "${result}".toString()
          } catch (Exception e) {
            log.error("Failed to evaluate expression ${expr?.inspect()}", e)
            return null
          }
        }

  }


}