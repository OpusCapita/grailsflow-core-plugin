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

import org.codehaus.groovy.grails.commons.GrailsDomainClass

import org.springframework.util.StringUtils
import com.jcatalog.grailsflow.process.Link
import com.jcatalog.grailsflow.utils.NameUtils

import grails.converters.JSON

import com.jcatalog.grailsflow.model.definition.ProcessDef
import com.jcatalog.grailsflow.model.definition.ProcessVariableDef
import com.jcatalog.grailsflow.model.definition.ProcessVarDefListItem
import com.jcatalog.grailsflow.model.process.ProcessVariable
import java.text.ParseException

/**
* Process variable definition controller class is used for working with
* variable definitions. It is possible to add/change/delete variables,
* also the input for variable value (widget) is changed according to
* selected variable type.  
*
* @author Stephan Albers
* @author July Karpey
*/
class ProcessVarDefController extends GrailsFlowSecureController {
    private static final String RESOURCE_BUNDLE = "grailsflow.processTypes"

    def grailsflowMessageBundleService
    
    def index = {
        redirect(controller: "processDef")
    }

    // the delete, save and update actions only accept POST requests
    // def allowedMethods = [delete:'POST', save:'POST', update:'POST']
    def static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def changeVarInput = {
        def var = new ProcessVariableDef()
        if (params.varID) {
            var = ProcessVariableDef.get(Long.valueOf(params.varID))
        }

        var.type = params.varType
        if (!var.isSuitableValue()) {
            var.defaultValue = null 
        }
        if (var.type == 'Link'){
            var.defaultValue = new Link()
        }else{
            var.defaultValue = null
        }

        render(template: "/processVarDef/variableInput", contextPath: pluginContextPath, model: [variable: var])
    }

    def addVarDef = {
        render(view: 'variableForm', model: [variable: new ProcessVariableDef(type: 'String'),
               process: ProcessDef.get(Long.valueOf(params.id))])
    }

    def editVarDef = {
        def var = ProcessVariableDef.get(Long.valueOf(params.id))
               
        render(view: 'variableForm', model: [variable: var, process: var.processDef])
    }

    def saveVarDef = {
        flash.errors = []
        
        def process = ProcessDef.get(Long.valueOf(params.id))
        def var
        if (params.varID) {
            var = ProcessVariableDef.get(Long.valueOf(params.varID))
        } else {
            var = new ProcessVariableDef()
        }

        if (!params.varName) {
            flash.errors << grailsflowMessageBundleService
                                .getMessage(RESOURCE_BUNDLE, "grailsflow.message.variableName.required")
            return render(view: 'variableForm', model: [variable: var, process: process])
        } else {
            var.name = StringUtils.trimAllWhitespace(params.varName)
            
            // Validate Variable Name
            if (!NameUtils.isValidIdentifier(var.name)) {
                flash.errors << grailsflowMessageBundleService
                                    .getMessage(RESOURCE_BUNDLE, "grailsflow.message.variableName.invalid")
                return render(view: 'variableForm', model: [variable: var, process: process], params: params)
            }

            def duplicateVar = ProcessVariableDef.findWhere(processDef: process, name: var.name)
            if (duplicateVar && (var.id == null || var.id != duplicateVar.id) ) {
                if (var.id) var.discard()
                flash.errors << grailsflowMessageBundleService
                                    .getMessage(RESOURCE_BUNDLE, "grailsflow.message.variableName.duplicated")
                return render(view: 'variableForm', model: [variable: var, process: process], params: params)
            }
            
            if (params.varType.equals("Object") && params.varObjectType) {
                // add verifications that Object type is a custom domain class
                def loadedClass
                try{
                    loadedClass = getClass().getClassLoader().loadClass(params.varObjectType, false)
                } catch (ClassNotFoundException ex) {
                    flash.errors << grailsflowMessageBundleService
                                        .getMessage(RESOURCE_BUNDLE, "grailsflow.message.specifiedClass.invalid")
                    return render(view: 'variableForm', model: [variable: var, process: process])
                }

                def artefact = grailsApplication.getArtefact("Domain", loadedClass.getName())
                if (!artefact || !(artefact instanceof GrailsDomainClass)) {
                    flash.errors << grailsflowMessageBundleService
                                        .getMessage(RESOURCE_BUNDLE, "grailsflow.message.specifiedClass.notDomain")
                    return render(view: 'variableForm', model: [variable: var, process: process])
                }

                var.type = params.varObjectType
            } else {
                var.type = params.varType
            }

            if (params.varType.equals("Date")) {
                def date = GrailsflowUtils.getParsedDate(params.varValue, gf.datePattern()?.toString())
                if(date){
                    var.defaultValue = date.time
                } else if (params.varValue) {
                    flash.errors << grailsflowMessageBundleService
                        .getMessage(RESOURCE_BUNDLE, "grailsflow.message.specifiedValue.notSuitable")
                    return render(view: 'variableForm', model: [variable: var, process: process])
                }
            } else if (params.varType.equals("Boolean")) {
                if (params.varValue) var.defaultValue = true
                else var.defaultValue = false
            } else if (params.varType.equals("Link")) {
                if (params.varValue_path) var.defaultValue = true
                if (params.varValue_description) var.defaultValue = true
                else var.defaultValue = false
                def string = ""
                if (params.varValue_path) {
                    string = string + "'path':'"+params.varValue_path+"'"
                    if (params.varValue_description) string = string + ","
                }
                if (params.varValue_description) string = string + "'description':'"+params.varValue_description+"'"
                var.defaultValue = "[ $string ]"
            } else if (params.varType.equals("List"))  {
                var.defaultValue = null
                var.subType = params["parent_varType_${var.id ? var?.name : ''}"]
                Collection<ProcessVarDefListItem> items = var.items
                var.items?.clear()
                items*.delete()

                try {
                    params.datePattern = gf.datePattern()?.toString()
                    def newItems = GrailsflowRequestUtils.getVariableItemsFromParams(var.id ? var?.name : '', params)
                    ProcessVariable tempVariable = new ProcessVariable()
                    Set preparedItems = new HashSet<ProcessVarDefListItem>(newItems?.size())
                    newItems?.each() { Object listValue ->
                        tempVariable.value = listValue
                        preparedItems.add(new ProcessVarDefListItem(content: tempVariable.variableValue, processVariableDef: var))
                    }
                    var.items = preparedItems
                } catch (Exception e) {
                    flash.errors << grailsflowMessageBundleService
                        .getMessage(RESOURCE_BUNDLE, "grailsflow.message.specifiedValue.notSuitable")
                    return render(view: 'variableForm', model: [variable: var, process: process])
                }
            } else {
                var.defaultValue = params.varValue
            }

            // we should check if the value is suitable
            if (!var.isSuitableValue()) {
                flash.errors << grailsflowMessageBundleService
                    .getMessage(RESOURCE_BUNDLE, "grailsflow.message.specifiedValue.notSuitable")
                return render(view: 'variableForm', model: [variable: var, process: process])
            }

            var.processDef = process
            var.isProcessIdentifier = params.isProcessIdentifier ? true : false
            var.required = params.required ? true : false
            var.view = GrailsflowRequestUtils.getVariableViewFromParams(params)

            if (var.id) {
                // update existing var
                var.save()
            } else {
                // add new variable to variables list
	            process.addToVariables(var)
	            process.save()
            }
            
            redirect(controller: "processDef", action: "editProcess", params: [id: process.id])
        }
    }

    def deleteVarDef = {
        def var = ProcessVariableDef.get(Long.valueOf(params.id))
        def processDefID = var?.processDef?.id
        if (var) {
            def varName = var.name
            var.removeFromAssociations()
            var.delete()
            flash.message = grailsflowMessageBundleService.getMessage(RESOURCE_BUNDLE,
                    "grailsflow.message.variable.deleted", [varName])
        }

        redirect(controller: "processDef", action: "editProcess", params: [id: processDefID] )
    }

    def orderMoveUp = {
      def variable =  ProcessVariableDef.get(Long.valueOf(params.id))
      def processDef = variable?.processDef
      def result
      if (!variable || !processDef) {
        log.debug("Nothing found to update for id=${params.id}")
        result = [orderChanged: false]
      } else if (variable == processDef.variables[0]) {
        log.debug("Cannot move up first element")
        result = [orderChanged: false]
      } else {
	      log.debug("Updating variables order for process ${processDef.processID}")
	      def oldOrder = processDef.variables.findIndexOf() { it == variable }
	      processDef.variables = moveElementUp(processDef.variables, variable)
	      if (!processDef.save()) {
		      processDef.errors.each() {
		        log.error(it)
		      }
		      result = [errors: processDef.errors, orderChanged: false]
	      }
	      result = [orderChanged: true, oldOrder: oldOrder]
	    }
      render result as JSON
    }

    def orderMoveDown = {
      def variable =  ProcessVariableDef.get(Long.valueOf(params.id))
      def processDef = variable?.processDef
      def result
      if (!variable || !processDef) {
        log.debug("Nothing found to update for id=${params.id}")
        result = [orderChanged: false]
      } else if (variable == processDef.variables[-1]) {
        log.debug("Cannot move up first element")
        result = [orderChanged: false]
      } else {
	      log.debug("Updating variables order for process ${processDef.processID}")
	      def oldOrder = processDef.variables.findIndexOf() { it == variable }
	      processDef.variables = moveElementDown(processDef.variables, variable)
	      if (!processDef.save()) {
		      processDef.errors.each() {
		        log.error(it)
		      }
		      result = [errors: processDef.errors, orderChanged: false]
	      }
	      result = [orderChanged: true, oldOrder: oldOrder]
	    }
      render result as JSON
    }

    private def moveElementUp(def list, def element){
      // move up is move down in reversed list
      return moveElementDown(list.reverse(), element).reverse()
    }

    private def moveElementDown(def list, def element){
      def newList = []
      def flag = false
      list.each() { it ->
        if (it == element) {
           flag = true
        } else {
          newList << it
          if (flag) { // previous element was element to move
            newList << element
            flag = false
          }
        }
      }
      if (flag) { // element was not inserted: it was last
        newList << element
      }
      return newList
    }

    def editVariableTranslations = {
        def variable = ProcessVariableDef.get(Long.valueOf(params.id))
        render(view: 'editVariableTranslations', model: [variable: variable])
    }

    def saveVariableTranslations = {
        def variable = ProcessVariableDef.get(Long.valueOf(params.id))

        def labels = GrailsflowRequestUtils.getTranslationsMapFromParams(params, 'label_')
        def descriptions = GrailsflowRequestUtils.getTranslationsMapFromParams(params, 'description_')
        variable.label = labels
        variable.description = descriptions
        variable.save()

        redirect(action: 'editVarDef', params: [id: params.id])
    }

    def showProcessEditor = {
        redirect(controller: "processDef", action: 'editProcess', params: [id: params.id])
    }
}

