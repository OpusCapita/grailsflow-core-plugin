package com.jcatalog.grailsflow
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

import com.jcatalog.grailsflow.utils.ConstantUtils
import com.jcatalog.grailsflow.utils.AuthoritiesUtils
import com.jcatalog.grailsflow.utils.NameUtils
import com.jcatalog.grailsflow.utils.ClassUtils

import com.jcatalog.grailsflow.model.definition.ProcessDef
import com.jcatalog.grailsflow.model.definition.ProcessDefAssignee
import com.jcatalog.grailsflow.model.definition.ProcessNodeDef
import com.jcatalog.grailsflow.model.definition.ActionStatement
import com.jcatalog.grailsflow.model.definition.ProcessTransitionDef
import com.jcatalog.grailsflow.model.definition.ProcessVariableDef
import com.jcatalog.grailsflow.model.definition.Variable2NodeVisibility

import com.jcatalog.grailsflow.model.view.VariableView

import com.jcatalog.grailsflow.engine.concurrent.ProcessTypeLock

import com.jcatalog.grailsflow.model.graphics.ProcessNodeDefPosition
import java.text.SimpleDateFormat
import com.jcatalog.grailsflow.model.definition.ProcessVarDefListItem

/**
 * GenerateProcessService class can generate Groovy Script from process
 * definition object, on the other hand it can prepare process definition
 * object from Groovy Script class (from workflow Groovy script).
 *
 * The generated script represents the executable process based on the
 * process definition. Created Groovy script is saved to the resource path
 * folder. This class can be edited an changed by the user, however
 * Grailsflow can currently not "reengeneer" the changes, when the class
 * is created again.
 *
 *
 * - generateGroovyProcess(processDef) -- writes process script
 * - buildProcessDefinition(def processClass) -- builds process definition from processClass 
 *
 * @author Stephan Albers
 * @author July Karpey
 * @author Maria Voitovich
 */
class GenerateProcessService {
    boolean transactional = true
    def grailsApplication
    def processScriptProvider
    
    private String writeProcessClassImports(ProcessDef processDef){
        def imports = new StringBuffer()

        // general imports
        imports << "import com.jcatalog.grailsflow.utils.ConstantUtils \n"

        // imports for variables types        
        def importedTypes = []
        processDef.variables.each { var ->
          if (!importedTypes.contains(var.type)) {
              if ("Document".equals(var.type)) {
                  imports << "import com.jcatalog.grailsflow.process.Document \n"
              } else if ("Link".equals(var.type)) {
                  imports << "import com.jcatalog.grailsflow.process.Link \n"
              } else if (!ProcessVariableDef.types.contains(var.type)) {
                  imports << "import $var.type \n"
              }
              importedTypes << var.type
          }
        }
        return imports.toString() 
    }
    
    private String writeProcessClassVariables(ProcessDef processDef) {
        def vars = new StringBuffer()
        
        def processKeys = []
        
        def requiredVariables = []

        processDef.variables.each { var ->
            vars << "    public ${var.type == 'List' ? var.type+'<'+var.subType+'>' : var.type} ${var.name} ${variableDefaultValue(var)} \n"

            if (var.isProcessIdentifier == Boolean.TRUE) {
                processKeys << var.name
            }
            if (var.required == Boolean.TRUE){
                requiredVariables << var.name
            }
        }
        
        // add ProcessIdentifier definition
        if (processKeys.size() > 0) {
          vars<< "\n\n"
          vars << "    def ProcessIdentifier = ${processKeys.inspect()} \n"
        }
        
        return vars.toString()
    }
    
    private String variableDefaultValue(ProcessVariableDef var){
      if (var.value) {
        return " = ${var.toString()}"
      } else {
        return ""
      }
    }

    private String writeProcessValidRangeIfExist(ProcessDef processDef) {
        def processProps = new StringBuffer()

        def sdf = new SimpleDateFormat('yyyy.MM.dd')
        if (processDef.validFrom) {
          processProps << "      validFrom ${sdf.format(processDef.validFrom).inspect()}\n"
        }
        if (processDef.validTo) {
          processProps << "      validTo ${sdf.format(processDef.validTo).inspect()}\n"
        }
        return  processProps ? "${processProps}\n" : ""
    }

    private String writeVariableConstraintsIfExist(ProcessVariableDef variable) {
      def varProps = []
      if (variable.required) {
        varProps << "required: true"
      }
      return  varProps ? "      ${variable.name}(${varProps.join(', ')})\n" : ""
    }
    
    private String writeProcessClassConstraints(ProcessDef processDef) {
        def constraints = new StringBuffer()

        constraints << "    def constraints = { \n"

        processDef.variables?.each() { var ->
          constraints << writeVariableConstraintsIfExist(var)
        }

        constraints << "    }\n"
        return constraints.toString()
    }

    private String writeProcessClassTranslations(ProcessDef processDef) {
        def translations = new StringBuffer()

        translations << "    def descriptions = { \n"

        // processDef translations
        translations << writeTranslationsIfExist(processDef.processID, processDef.label, processDef.description)

        // nodes translations
        processDef.nodes.each() { nodeDef ->
          translations << writeTranslationsIfExist(nodeDef.nodeID, nodeDef.label, nodeDef.description)
          // events translations
          nodeDef.transitions.each() { transition ->
            translations << writeTranslationsIfExist(transition.translationsKey, transition.label, [:])
          }
        }

        // variables translations
        processDef.variables.each() { varDef ->
          translations << writeTranslationsIfExist(varDef.name, varDef.label, varDef.description)
        }
        translations << "    }\n"
        return translations.toString()
    }
    
    private String writeTranslationsIfExist(def name, def labels, def descriptions){
      // check if there're translations
      if (labels.size() + descriptions.size() > 0) {
        def translations = []
        labels.each() { lang, value ->
          def key = "label_${lang}"
          translations.add("${key.inspect()} : ${value.inspect()}") 
        }
        descriptions.each() { lang, value ->
          def key = "description_${lang}"
          translations.add("${key.inspect()} : ${value.inspect()}") 
        }
        return "        ${name}( ${translations.join(',\n          ')} ) \n"
      } else {
        return ""
      }
    }

    private String writeProcessClassViews(ProcessDef processDef){
      def views = new StringBuffer()

      views << "    def views = { \n"

      processDef.variables.each { variable ->
        views << "        ${variable.name}( ${writeVariableView(variable.view)} ) \n"
      }
      views << "    } \n"
      return views.toString()
    }

    private String writeVariableView(def variableView) {
      if (!variableView) return "";
      def view = new StringBuffer()

      def properties = []

      def type = NameUtils.upCase(NameUtils.upCase(variableView.type))
      def viewClass = grailsApplication.getClassForName("${VariableView.class.package.name}.${type}")

      ClassUtils.getDomainClassProperties(viewClass).each() { name ->
        def value = variableView."$name"
        if (value != null) {
          properties << "${name}: ${value.inspect()}"
        }
      }
      view << "${NameUtils.downCase(variableView.type)}( ${properties.join(", ")} )"
      return view.toString()
    }
    
    /**
     * Writes ProcessDef as process class Groovy file.
     *
     * return true if successful, false otherwise
     */
    public boolean generateGroovyProcess(processDef) {
        if (!processDef) {
            return Boolean.FALSE
        }

        def groovyCode = new StringBuffer()

        groovyCode << writeProcessClassImports(processDef)
        groovyCode << "\n\n"

        groovyCode << "/** \n"
        groovyCode << " * Please remember: you can use process variable values in expressions. \n"
        groovyCode << " * Example: \"Value is \${someProcessVariable}\" \n"
        groovyCode << " * Besides process variables there are some additional variables available for node's actions: \n"
        groovyCode << " *  - actionContext of com.jcatalog.grailsflow.actions.ActionContext type \n"
        groovyCode << " */ \n"

        groovyCode << "class ${processDef.processID}Process { \n"

        groovyCode << writeProcessClassVariables(processDef)
        groovyCode << "\n\n"
        groovyCode << writeProcessClassConstraints(processDef)
        groovyCode << "\n\n"
        groovyCode << writeProcessClassTranslations(processDef)
        groovyCode << "\n\n"
        groovyCode << writeProcessClassViews(processDef)
        groovyCode << "\n\n"

        // add process nodes definition
        groovyCode << "    def ${processDef.processID}Process = { \n"

        def processAssignees = processDef.processAssignees
        if (processAssignees) {
          groovyCode << "      ${assigneesDefinition(processAssignees)}\n"
        }

        groovyCode << writeProcessValidRangeIfExist(processDef)
        
        processDef.nodes?.each() { node ->
            def nodeCode = new StringBuffer("      "+node.nodeID)
            if (!node.type.equals(ConstantUtils.NODE_TYPE_ACTIVITY))
                nodeCode << node.type
            nodeCode << "("
            // node params

            def nodeParams = []

            if (node.dueDate && node.dueDate > 0) {
                nodeParams << "dueDate: ${node.dueDate}"
            }

            if (node.expectedDuration && node.expectedDuration > 0) {
                nodeParams << "expectedDuration: ${node.expectedDuration}"
            }

            if (node.type == ConstantUtils.NODE_TYPE_WAIT) {
                if (node.editorType)
                    nodeParams << "editorType: ConstantUtils.${ConstantUtils.getEditorType(node.editorType)}"
                if (node.externalUrl) nodeParams << "externalUrl: '${node.externalUrl}'"
            }

            if (node.protocolGroup) {
                nodeParams << "protocolGroup: '${node.protocolGroup}'"
            }
            
            if (nodeParams) {
              nodeCode << nodeParams.join(", ")
            }
            nodeCode << ") { \n"
            // node code

            if (node.type == ConstantUtils.NODE_TYPE_WAIT && node.variables2NodeVisibility) {
            	nodeCode << "        variable( "
                def vrs = new StringBuffer()
                node.variables2NodeVisibility.each { vis ->
                    if (vrs.toString() != "") {
                        vrs << ", "
                    }
                    vrs << vis.variable.name + ": ConstantUtils."
                    vrs << vis.visibilityDesc
                }

            	nodeCode << vrs+") \n"
            }

            // node assignees
            if (node.type == ConstantUtils.NODE_TYPE_WAIT && node.assignees) {
              nodeCode << "        ${assigneesDefinition(node.assignees)}\n"
            }

            if (node.actionStatements || (node.type != ConstantUtils.NODE_TYPE_WAIT)) {
                nodeCode << "        action { \n"

                node.actionStatements?.each() { line ->
                    nodeCode << "            "+(line.content ?: "") + "\n"
                }
                nodeCode << "        } \n"
            }
            node.transitions.each { transition ->
	            def destinationNodes = transition.toNodes ? transition.toNodes.collect() { it.nodeID.inspect() } : null
	            if (destinationNodes) {
	              nodeCode << "        on(${transition.event.inspect()}).to([ ${destinationNodes.join(', ')} ]) \n"
	            }  
            }
            groovyCode << nodeCode + "      } \n\n"
        }
        groovyCode << "    } \n }"

        // create and write groovy script
        synchronized (ProcessTypeLock.getLock(processDef.processID)) {
            try{
                return processScriptProvider.writeProcessScript(processDef.processID, groovyCode.toString())
            }catch (Throwable ex){
                log.error("Unexpected exception occurred in synchronized block! Please, contact to administrator. ", ex)
                return Boolean.FALSE
            }
        }
    }

    private String assigneesDefinition(Collection<ProcessDefAssignee> assignees) {
      def assigneesIDs = assignees.collect() { it.assigneeID.trim() }
      def users = AuthoritiesUtils.getUsers(assigneesIDs).collect(){ it.inspect() }
      def roles = AuthoritiesUtils.getRoles(assigneesIDs).collect(){ it.inspect() }
      def groups = AuthoritiesUtils.getGroups(assigneesIDs).collect(){ it.inspect() }

      def assigneesList = []
      if (users) {
        assigneesList << "users: [ ${users.join(', ')} ]"
      }
      if (roles) {
        assigneesList << "roles: [ ${roles.join(', ')} ]"
      }
      if (groups) {
        assigneesList << "groups: [ ${groups.join(', ')} ]"
      }

      return "assignees( ${assigneesList.join(', ')} )"
    }


   public def buildProcessDefinition(def scriptClass) {
        if (!scriptClass) return null
        log.debug("Generating process definition for ${scriptClass.processType} process.")

        def processDef = new ProcessDef()
        processDef.processID = scriptClass.processType
        processDef.label = scriptClass.label
        processDef.description = scriptClass.description
        processDef.validFrom = scriptClass.validFrom
        processDef.validTo = scriptClass.validTo
        processDef.save(flush: true)

        // define process nodes definitions
        def finalNodeIDs = scriptClass.finalNodes.collect() { it.nodeID }
        def startNodeID = scriptClass.startNode?.nodeID
        scriptClass.nodesList?.each { scriptNode ->
            def nodeDef = new ProcessNodeDef()
            nodeDef.nodeID = scriptNode.nodeID
            nodeDef.type = scriptNode.type
            processDef.addToNodes(nodeDef)
            
            nodeDef.label = scriptNode.label
            nodeDef.description = scriptNode.description
            nodeDef.dueDate = scriptNode.dueDate
            nodeDef.expectedDuration = scriptNode.expectedDuration
            nodeDef.protocolGroup = scriptNode.protocolGroup

            if (scriptNode.editorType) {
                nodeDef.editorType = scriptNode.editorType
                nodeDef.externalUrl = scriptNode.externalUrl
            } else {
                nodeDef.editorType = ConstantUtils.EDITOR_AUTO
            }
						
            // copy assignees
            scriptNode.assignees?.each() {
              processDef.addToAssignees(new ProcessDefAssignee(assigneeID: it.assigneeID, processNodeDef: nodeDef))
            }

            // copy statements
            scriptNode.actionStatements?.each() {
                def statement = new ActionStatement()
                statement.properties = it.properties
                nodeDef.addToActionStatements( statement )
            }


            // create NodePosition
            def nodePosition = new ProcessNodeDefPosition()
            nodePosition.processDef = processDef
            nodePosition.nodeID = nodeDef.nodeID
            nodePosition.actionType = nodeDef.type

            if (startNodeID == nodeDef.nodeID) {
                nodePosition.knotType = "start"
            } else if (finalNodeIDs.contains(nodeDef.nodeID)) {
                nodePosition.knotType = "final"
            } else nodePosition.knotType = ""

            nodePosition.save()
        }

        // define process variables definitions   
        scriptClass.variables.each { scriptVariable ->
	        def variable = new ProcessVariableDef()
	        variable.name = scriptVariable.name
	        variable.type = scriptVariable.type
	        variable.label = scriptVariable.label
	        variable.description = scriptVariable.description

            // copy variable view
            if (scriptVariable.variableView) {
                def variableView = scriptVariable.variableView.class.newInstance()
		        variableView.properties = scriptVariable.variableView.properties
		        variable.view = variableView
	        } 
            variable.required = scriptVariable.required

            variable.defaultValue = scriptVariable.defaultValue
            variable.subType = scriptVariable.subType
            scriptVariable.items?.each() {
                variable.addToItems(new ProcessVarDefListItem(content: it.content))
            }

	        variable.isProcessIdentifier = scriptVariable.isProcessIdentifier 
	        processDef.addToVariables(variable)
        }

        // define process assignees
        scriptClass.processAssignees.each() {
            def processAssignee = new ProcessDefAssignee(assigneeID: it.assigneeID)
            processDef.addToAssignees(processAssignee)
        }

        ProcessDef.withTransaction { status ->
          if (!processDef.save(flush: true)) {
            processDef.errors.each() {
              log.error(it)
            }
            status.setRollbackOnly()
          }
        }
        
        scriptClass.nodesList?.each { scriptFromNode ->
            def fromNode = processDef.nodes.find() { it.nodeID == scriptFromNode.nodeID }
            if (!fromNode) {
              log.error("Node ${scriptFromNode.nodeID} not found for creating transitions")
              return
            }
            scriptFromNode.transitions?.each() { scriptTransition ->
              def transDef = new ProcessTransitionDef(event: scriptTransition.event, fromNode: fromNode)
              transDef.label = scriptTransition.label
              def toNodes = []
              scriptTransition.toNodes?.each() { scriptToNode ->
                  def toNode = processDef.nodes.find() { it.nodeID == scriptToNode.nodeID }
                  if (toNode) { 
                    toNodes << toNode
                  } else {
                    log.error("Destination node ${scriptToNode.nodeID} not found for transition ${event}")
                  }
              }
              if ( toNodes.size() > 0 ) {
                transDef.toNodes = toNodes
              } else {
                log.error("There's no destination nodes for transition ${scriptTransition.event} from node ${scriptFromNode.nodeID}.")
              }
            }
        }

        scriptClass.varVisibility.keySet()?.each() { nodeID ->
            def node = processDef.nodes.find() { it.nodeID == nodeID }
            if (node) {
	            scriptClass.varVisibility[nodeID]?.keySet()?.each() { varName->
	               def variable = processDef.variables.find() { it.name == varName }
	               if (variable) {
		               def visibility = new Variable2NodeVisibility(visibilityType: scriptClass.varVisibility[nodeID][varName],
		                                           visibilityDesc: ConstantUtils.getVisibilityTypes()[scriptClass.varVisibility[nodeID][varName]]) 
		               variable.addToVariable2NodesVisibility(visibility)
		               node.addToVariables2NodeVisibility(visibility)
		            } else {
		              log.error("Variable ${varName} not found for storing variable visibility for node ${nodeID}")
		            }
	            }
	          } else {
	            log.error("Node ${nodeID} not found for storing variables visibilities")
	          }
        }
        ProcessDef.withTransaction { status ->
          if (!processDef.save(flush: true)) {
            processDef.errors.each() {
              log.error(it)
            }
            status.setRollbackOnly()
          }
        }

       return processDef
   }

}
