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

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import com.jcatalog.grailsflow.builder.*

import org.apache.commons.lang.StringUtils

import com.jcatalog.grailsflow.model.definition.ProcessVariableDef
import com.jcatalog.grailsflow.model.definition.ProcessTransitionDef
import com.jcatalog.grailsflow.model.definition.ActionStatement

import com.jcatalog.grailsflow.process.script.ProcessScript

import com.jcatalog.grailsflow.engine.helper.*;

import java.security.AccessController
import java.security.PrivilegedAction
import com.jcatalog.grailsflow.model.process.ProcessVariable

/**
 * A Process Builder used to construct Process class form Process script.
 * 
 * Process class is Groovy class stored in process file. Process Builder extends this class with 
 * methods used to manage process. Fro example:
 * - getNodes()
 * - getVariables()
 * - getAssignees()
 * - etc
 *
 * For more details see {@code com.jcatalog.grailsflow.engine.ProcessClassManagementMethods} 
 *
 *
 * @author Stephan Albers
 * @author July Antonicheva
 * @author Maria Voitovich
 */

class ProcessBuilder {
    protected Log log = LogFactory.getLog(getClass())

    private Class processClass
    public def errors = []

    public ProcessBuilder(ProcessScript processScript) {
        if (processScript == null) {
            log.error("Process script not found.")
            errors << "Proces script not found."
            return
        }

        def processClass, processInstance
        try {
            def groovyCodeSource = (GroovyCodeSource) AccessController.doPrivileged([
              run: {
                return new GroovyCodeSource(processScript.source, "process"+processScript.type+".groovy", "/groovy/script");
              }
            ] as PrivilegedAction)
            groovyCodeSource.cachable = false

            def loader = new GroovyClassLoader(getClass().getClassLoader())
            processClass  = loader.parseClass(groovyCodeSource)

            // Process "ProcessIdentifier" field
            processInstance = processClass.newInstance()
        } catch(Exception e) {
            log.error("Process script ${processScript.type} cannot be compiled and instantiated: $e.message")
            errors << "Process script ${processScript.type} cannot be compiled and instantiated: $e"
            return
        }

        def processClassExtention = new ProcessClassManagementMethods(processClass)

        def processIdentifier
        if (processClass.declaredFields.find {it.name == 'ProcessIdentifier'}) {
            def field = processClass.getDeclaredField("ProcessIdentifier")
            field.setAccessible(true)
            def value = field.get(processInstance)
            if (value && value instanceof String) value = [value]
            if (!value) value = []
            processIdentifier = value
        }
        processClassExtention.processIdentifier = processIdentifier
        
        def processType = processScript.type
        
        processClassExtention.processType = processType

        // Process "constraints" section
        def constraintsSection = new ConstraintsSectionBuilder(processInstance)
        def constraints = constraintsSection.constraints

        // Process "translations" section
        def translationsSection = new TranslationsSectionBuilder(processInstance)
        def translations = translationsSection.translations
        
        // Fill process label and description
        fillTranslations(processClassExtention, translations[processType])

        // Process "views" section
        def viewsSection = new ViewsSectionBuilder(processInstance)
        def views = viewsSection?.views
        def variablesOrder = viewsSection?.order
        
        // Build process variables
        processClassExtention.variables = buildVariablesList(processInstance, processIdentifier, variablesOrder, translations, views, constraints)

        // Process nodes section
        def nodesSection = new NodesSectionBuilder(processInstance)
        def nodes = nodesSection?.nodes
        def assignees = nodesSection?.assignees
        def validRange = nodesSection?.validRange
        def transitions = nodesSection?.transitions
        def visibility = nodesSection?.varVisibility

        // Build process valid range
        processClassExtention.validFrom = validRange?.validFrom
        processClassExtention.validTo = validRange?.validTo

        // Build Process nodes
        processClassExtention.nodesMap = buildNodesMap(nodes, translations, assignees, transitions)
        processClassExtention.visibility = visibility
        processClassExtention.processAssignees = assignees[processInstance.class.simpleName]

        // Calculate final nodes
        processClassExtention.finalNodes = processClassExtention.nodesMap.values().findAll() { node ->
          def nodeTransitions = transitions[node.nodeID]
          return nodeTransitions ? nodeTransitions.isEmpty : true
        }

        // Set process closures
        processClassExtention.nodeActions = nodesSection?.nodeActions

        if (nodes && !nodes.isEmpty()) {
            def actionsParser = new ActionsCodeParser()
            def actionsCode = actionsParser.parse(processScript.source, nodes)

            actionsCode.each() { nodeID, codeLines ->
                log.debug("Setting action code for node ${nodeID}")
                codeLines?.each() { line ->
                  processClassExtention.nodesMap[nodeID]?.addToActionStatements( new ActionStatement(content: line) )
                }
            }
        }

        this.processClass = processClass
    }
    
   private void fillTranslations(def object, def translations) {
      translations?.each() { key, value ->
        if (key.startsWith("label_")) {
          def lang = StringUtils.substringAfter(key, "label_")
          if (lang && value) {
            if (!object.label) {
              object.label = [:]
            }
            object.label.put(lang, value)
          }
        }
        if (key.startsWith("description_")) {
          def lang = StringUtils.substringAfter(key, "description_")
          if (lang && value) {
            if (!object.description) {
              object.description = [:]
            }
            object.description.put(lang, value)
          }
        }
      }
   }
    
    private Map buildNodesMap(def nodesList, def translations, def assignees, def transitions) {
      def nodesMap = new LinkedHashMap()
      nodesList?.each() { nodeDef ->
        nodesMap.put(nodeDef.nodeID, nodeDef)
      }
      nodesList?.each() { nodeDef ->
        fillTranslations(nodeDef, translations[nodeDef.nodeID])
        assignees[nodeDef.nodeID]?.each() {
          nodeDef.addToAssignees( it )
        }
        transitions[nodeDef.nodeID]?.each() { event, toNodesIDs ->
          def transition = new ProcessTransitionDef(fromNode: nodeDef, event: event)
          fillTranslations(transition, translations[transition.translationsKey])
          def toNodes = []
          toNodesIDs?.each() { toNodeID ->
            def toNode = nodesMap[toNodeID]
            if (toNode) {
              toNodes.add(toNode)
            }
          }
          transition.toNodes = toNodes
          nodeDef.addToTransitions(transition)
        }
      }
      return nodesMap
    }
    
    private Collection buildVariablesList(def processInstance, def processIdentifier, def variablesOrder, def translations, def views, def constraints) {
        def processClass = processInstance.class
        def orderedFields = getOrderedVariables(processClass, variablesOrder)
        def variablesList = new ArrayList()
        orderedFields.collect() { property ->
	        def variable = new ProcessVariableDef()
	        variable.name = property.name
	        variable.type = ProcessVariableDef.types.contains(property.type.simpleName) ? property.type.simpleName : property.type.name
            if (ProcessVariable.defineType(variable.type) == ProcessVariable.LIST) {
                Class subClass = property.getGenericType()?.getActualTypeArguments()[0]
                variable.subType = subClass?.simpleName
            }
	        fillTranslations(variable, translations[variable.name])

            variable.variableView = views[variable.name]

	        if (constraints[variable.name]?.'required') {
	          variable.required = constraints[variable.name]['required']
	        }

            variable.value = processClass.getField(property.name).get(processInstance)
	        variable.isProcessIdentifier = processIdentifier ? processIdentifier.contains(property.name) : Boolean.FALSE
	        log.debug("Build variable ${variable.name} of type ${variable.type} with defaultValue ${variable.defaultValue} and ${variable.variableView?.type} view")
	        log.debug("Variable ${variable.name} is ${variable.isProcessIdentifier ? '' : 'not'} process identifier and ${variable.required ? '' : 'not'} required")
	        variablesList.add(variable)
        }
        return variablesList
    }

    private Collection getOrderedVariables(def processClass, def variablesOrder) {
       return  processClass.fields.findAll { 
           it.toString().indexOf(" static ") == -1 
         }.sort() { a, b ->
           if (!variablesOrder) {
             return a.name.compareTo(b.name)
           }
           def indexA = variablesOrder[a.name]
           def indexB = variablesOrder[b.name]
           if (indexA == null && indexB == null){
             return a.name.compareTo(b.name)
           }
           if (indexA == null) {
             return 1
           } else if (indexB == null) {
             return -1
           } else {
             return indexA <=> indexB 
           }
         }
    }

}
