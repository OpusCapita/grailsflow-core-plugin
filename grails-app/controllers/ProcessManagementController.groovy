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
import org.springframework.web.servlet.support.RequestContextUtils as RCU

import org.springframework.web.multipart.commons.CommonsMultipartFile

import com.jcatalog.grailsflow.model.process.BasicProcess
import com.jcatalog.grailsflow.model.process.FlowStatus
import com.jcatalog.grailsflow.model.process.ProcessNode
import com.jcatalog.grailsflow.model.process.ProcessVariable

import com.jcatalog.grailsflow.utils.TranslationUtils
import com.jcatalog.grailsflow.utils.AuthoritiesUtils

import com.jcatalog.grailsflow.engine.execution.ExecutionResultEnum

import com.jcatalog.grailsflow.model.definition.ProcessVariableDef
import com.jcatalog.grailsflow.process.Link
import com.jcatalog.grailsflow.process.Document
import com.jcatalog.grailsflow.status.ProcessStatusEnum
import grails.converters.JSON

/**
 * ProcessManagement controller class is used for executing process actions from UI.
 *
 * @author Stephan Albers
 * @author July Karpey
 * @author Maria Voitovich
 */
class ProcessManagementController extends GrailsFlowSecureController {
    def processManagerService
    def additionalWorklistColumns
    def maxResultSize

    def processClassValidator

    def workareaPathProvider
    def documentsPath
    def datePatterns

    // default outcome for error
    def processManagementErrorController = "process"
    def processManagementErrorAction = "showTypes"

    // default outcome for success
    def processManagementResultController = "process"
    def processManagementResultAction = "showProcessDetails"


    private def gotoError = {
      def controller = params.errorController ?: processManagementErrorController
      def action = params.errorAction ?: processManagementErrorAction
      params.controller = controller
      params.action = action
      redirect(controller: controller, action: action, params: params)
    }

    /**
     * Perform redirect after success action in next order:
     * 1. to URL from 'backUrl' request parameter
     * 2. to controller from 'resultController' and action from 'resultAction' request parameters
     * 3. to grailsflow url '/process/showProcessDetails'
     */
    private def gotoResult = {
        if (params.backUrl) {
            return redirect(url: params.backUrl)
        }
      def controller = params.resultController ?: processManagementResultController
      def action = params.resultAction ?: processManagementResultAction
      params.controller = controller
      params.action = action
      redirect(controller: controller, action: action, params: params)
    }

  /**
    * Starts process
    *
    * params:
    *   - id -- process name
    *   - errorController, errorAction (optional) controller and action to forward to in case of errors
    *   - resultController, resultAction (optional) controller and action to forward to in case of no errors
    */
    def startProcess = {
        log.debug("Starting process of type '${params.id}'")

        if (!flash.errors) flash.errors = []
        if (!flash.warnings) flash.warnings = []

        def processClass = processManagerService.getProcessClass(params.id)
        if (!processClass) {
            flash.errors << g.message(code: "plugin.grailsflow.message.processScript.invalid", args: [params.id])
            withFormat {
                html { gotoError() }
                json { render(errors: flash.errors, params: params) as JSON}
            }
            return
        }

        // validate process definition
        if (processClassValidator != null) {
          def validationResult = processClassValidator.validate(processClass)
          validationResult.errors.each() {
            log.debug("Process ${processClass.processType} validation error: ${it}")
          }
          validationResult.warnings.each() {
            log.debug("Process ${processClass.processType} validation warning: ${it}")
          }
          flash.errors.addAll(validationResult.errors)
          flash.warnings.addAll(validationResult.warnings)

          if (!validationResult.isValid()) {
              withFormat {
                  html { gotoError() }
                  json { render(errors: flash.errors, warnings: flash.warnings, params: params) as JSON}
              }
              return
          }
        }

        // checking process assignees and user authorities
        def authorities = getUserAuthorities(session)

        def processAssignees = processClass.processAssignees.collect() { it.assigneeID.trim() }
        if (!processAssignees.isEmpty() && processAssignees.intersect(authorities).isEmpty()) {
            flash.errors << g.message(code: "plugin.grailsflow.message.processAuthorities.invalid")
            withFormat {
                html { gotoError() }
                json { render(errors: flash.errors, params: params) as JSON}
            }
            return
        }

        // TODO: may be handle this on UI?
        if (processClass.startNode.type == ConstantUtils.NODE_TYPE_WAIT) {
            forward(controller: "process", action: "showStartDetails", params: params)
        } else {
            if (!processManagerService.checkProcessIdentifier(params.id, null)) {
                flash.errors << g.message(code: "plugin.grailsflow.message.process.parallel", args: [params.processType])
                withFormat {
                    html { gotoError() }
                    json { render(errors: flash.errors, params: params) as JSON}
                }
                return
            }

            def result = processManagerService.startProcess(params.id, securityHelper.getUser(session), null)
            if (result == null) {
                flash.errors = processManagerService.errors
                withFormat {
                    html { gotoError() }
                    json { render(errors: flash.errors, params: params) as JSON}
                }
                return
            } else {
                params.id = null
                params.processID = result
                withFormat {
                    html { gotoResult() }
                    json { render params as JSON}
                }
                return
            }
        }

    }

    /**
     * Send event
     * parameters:
     *   - isStarted
     *   - processID
     *   - processType
     *
     *   - errorController, errorAction (optional) controller and action to forward to in case of errors
     *   - resultController, resultAction (optional) controller and action to forward to in case of no errors
     */
    def sendEvent = {
        flash.errors = []
        flash.message = ''
        def lang = request.locale.language.toString()

        if (request.getParameter("processNode_eventForwarding")) {
            return forward(action: "forwardEvent", params: params)
        }

        def processInstance
        ProcessNode node
        if (params.isStarted && params.isStarted == "false") {
            processInstance = processManagerService.getNewProcessInstance(params.processType)
            node = processInstance ? new ProcessNode(processInstance.class.startNode) : null
        } else {
            Long processID = params.processID as Long
            processInstance = processManagerService.getRunningProcessInstance(processID)
            def process = BasicProcess.get(processID)
            node = (process && params.nodeID) ?  ProcessNode.findWhere("nodeID": params.nodeID,
                "process": process) : null
        }

        def processClass = processInstance?.class
        if (!processClass) {
            flash.errors << g.message(code: "plugin.grailsflow.message.processScript.invalid", args: [params.processType])
            withFormat {
                html { gotoError() }
                json { render(errors: flash.errors, params: params) as JSON }
            }
            return
        }

        if (!node) {
            flash.errors << g.message(code: "plugin.grailsflow.message.parameters.invalid")
            withFormat {
                html { gotoError() }
                json { render(errors: flash.errors, params: params) as JSON }
            }
            return
        }

        def nodeID = node.nodeID

        // checking process node assignees and user authorities
        def authorities = getUserAuthorities(session)

        def nodeAssignees = []
        node?.process?.assignees?.each {
            if (it.nodeID == nodeID) nodeAssignees << it.assigneeID.trim()
        }
        if (nodeAssignees.isEmpty()) {
            nodeAssignees = processClass.nodes[nodeID]?.assignees?.collect() { it.assigneeID.trim() }
        }
        if (nodeAssignees && !nodeAssignees.isEmpty() && nodeAssignees.intersect(authorities).isEmpty()) {
            flash.errors << g.message(code: "plugin.grailsflow.message.nodeAuthorities.invalid")
            withFormat {
                html { gotoError() }
                json { render(errors: flash.errors, params: params) as JSON }
            }
            return
        }

        // Map of variables passed by user
        def variables = [:]

        // get passed ProcessVariable values
        // check if all 'required' properties are filled
        processClass.variables?.each {variable ->
            def name = variable.name
            if (hasVariable(name, variable.type)){
                variables[name] = getVariableValueFromParams(variable)
                log.debug("Request contains value ${variables[name]} for ${name} variable")
            } else {
                log.debug("Request does not contain new value for ${name} variable")
            }
            
            // validate 'required' fields values
            def newValue = variables.keySet().contains(name)

          if ((variable.required || processClass.isRequired(name, nodeID)) &&
              (newValue && (!variables[name] || String.valueOf(variables[name]).trim() == '') //  no new value
              || !newValue && processInstance[name] == null)) { // there's no new value and current value is 'null'
                flash.errors << g.message(code: "plugin.grailsflow.message.property.required", args: [TranslationUtils.getTranslatedValue(variable.label, variable.name, lang)])
            }
        }

        // if there were errors in flash scope after variables filling
        if (!flash.errors.isEmpty()) {
            if (params.nodeFormController && params.nodeFormAction && params.nodeFormID != null) {
                params.id = params.nodeFormID
                params.controller = params.nodeFormController
                params.action = params.nodeFormAction
                withFormat {
                    html { return forward(controller: params.nodeFormController, action: params.nodeFormAction, params: params) }
                    json { render(errors: flash.errors, params: params) as JSON }
                }
                return
            } else {
                params.id = params.processType
                withFormat {
                    html { gotoError() }
                    json { render(errors: flash.errors, params: params) as JSON }
                }
                return
            }
        }

        // if process is not started start it
        if (node.id == null) {
            // checking process identifier
            if (!processManagerService.checkProcessIdentifier(params.processType, variables)) {
                flash.errors << g.message(code: "plugin.grailsflow.message.process.parallel", args: [params.processType])
                withFormat {
                    html { gotoError() }
                    json { render(errors: flash.errors, params: params) as JSON }
                }
                return
            }

            // starting process
            def result = processManagerService.startProcess(params.processType, securityHelper.getUser(session), variables)
            if (!result) {
                flash.errors.addAll(processManagerService.errors)
                withFormat {
                    html { gotoError() }
                    json { render(errors: flash.errors, params: params) as JSON}
                }
                return
            } else {
                def process = BasicProcess.get(result)
                node = process.nodes.find() { it.nodeID == node.nodeID }
            }
        }

        // common behaviour for sending event
        def event
        def availableEvents = processClass.nodes[node.nodeID]?.transitions?.collect() { it.event }
        availableEvents?.each { eventID ->
	        if (request.getParameter("event_" + eventID))
	            event = eventID
        }

        log.debug("Sending event ${event} to node ${node.nodeID} of process #${node.process.id}")
        def res = processManagerService.sendEvent(node.process, node, event, securityHelper.getUser(session), variables)
        log.debug("The result of sending event is: $res")

        // TODO get rid of "isEmbeded"
        if (params.isEmbedded == "true") return render("<script>window.close();</script>")

        if (res != ExecutionResultEnum.EXECUTED_SUCCESSFULLY.value()) {
            flash.errors << g.message(code: "plugin.grailsflow.message.sendEvent.error.${res}",
                args: [params.processType, node.process.id.toString(), node.nodeID, node.id.toString(), event])
            withFormat {
                html { gotoError() }
                json { render(errors: flash.errors, params: params) as JSON }
                return
            }
        } else {
            if (node.type == ConstantUtils.NODE_TYPE_WAIT
                && processClass.nodeActions[node.nodeID]) {
                flash.message = g.message(code: "plugin.grailsflow.message.sendEvent.warning", args: [node.nodeID])
            }
            params.putAll([id: null, "processID": node.process.id, nodeID: node.nodeID])
            withFormat {
                html {gotoResult()}
                json {render params as JSON}
            }
            return
        }
    }

    def forwardEvent = {
        def process = BasicProcess.get(Long.valueOf(params.processID))
        def activatedStatus = FlowStatus.findByStatusID("ACTIVATED")
        // node that received event
        def node = ProcessNode.findWhere("nodeID": params.nodeID,
            "process": process, "status": activatedStatus)

        def assignees = []
        assignees += AuthoritiesUtils.getUserAuthorities(params.processNode_users?.split(",")*.trim()) 
        assignees += AuthoritiesUtils.getRoleAuthorities(params.processNode_roles?.split(",")*.trim())
        assignees += AuthoritiesUtils.getGroupAuthorities(params.processNode_groups?.split(",")*.trim())

        if (processManagerService
              .forwardProcessNode(node, assignees, securityHelper.getUser(session))) {
            flash.message = g.message(code: "plugin.grailsflow.message.forwarding.success")
        } else {
            flash.message = g.message(code: "plugin.grailsflow.message.forwarding.error")
        }

        if (params.isEmbedded == "true") return render("<script>window.open('','_self');window.close();</script>")

        params.id = params.processID
        withFormat {
            html {
              if (params.resultController) {
                return chain(controller: (params.resultController ? params.resultController : 'process'),
                            action: params.resultAction ? params.resultAction : "index", params: params)
              }

              forward(controller: "process", action: params.resultAction ? params.resultAction : "showWorklist", params: params)
            }
            json { render(errors: flash.errors, message: flash.message, params: params) as JSON }
        }
    }
    
    def killProcess = {
        def result = processManagerService.killProcess(new Long(params.id), securityHelper.getUser(session))
        log.debug("Killing of process #${params.id} finished with code ${result}")
        if (!result) {
            flash.message = g.message(code: "plugin.grailsflow.message.process.killed.error", args: [String.valueOf(params.id)])
        } else {
            BasicProcess process = BasicProcess.get(new Long(params.id))
            if (process?.status.statusID == ProcessStatusEnum.KILLING.value()) {
                flash.message = g.message(code: "plugin.grailsflow.message.process.killing", args: [String.valueOf(params.id)])
            } else if (process?.status.statusID == ProcessStatusEnum.KILLED.value()) {
                flash.message = g.message(code: "plugin.grailsflow.message.process.killed", args: [String.valueOf(params.id)])
            }
        }
        return forward(controller: "process", action: "search", params: params)
    }


  private boolean hasVariable(def name, def variableType){
      def type = ProcessVariable.defineType(variableType)
      def parameterName = "var_${name}".toString()
      switch (type){
        case ProcessVariable.BOOLEAN :
          if (request.getParameterMap().keySet().contains(parameterName)) {
            return Boolean.TRUE
          }
          return request.getParameterMap().keySet().contains("_"+parameterName)
        case ProcessVariable.DOCUMENT :
          CommonsMultipartFile file = request.getFile(parameterName)
		      return file && !file.isEmpty()
		    case ProcessVariable.LINK :
		      def path = request.getParameter("${parameterName}.path".toString())
		      def description = request.getParameter("${parameterName}.description".toString())
		      return path || description
        case ProcessVariable.LIST:
          return request.getParameterMap().keySet().collect {
              it.indexOf("listItemValue_${name}_") != -1
            }.size() > 0
        default:
          return request.getParameterMap().keySet().contains(parameterName)
      }
  }

    private def getVariableValueFromParams(ProcessVariableDef variable){
        String name = variable?.name
        String variableType = variable?.type
        if (!flash.errors) flash.errors = []

        def parameterName = "var_${name}".toString()
        if (ProcessVariable.isValueIdentifier(variableType)) {
            def value = params.remove(parameterName)

            if (!value) return null;
            try {
                def domainClass = getClass().getClassLoader().loadClass(variableType, false)
                if (domainClass && domainClass.list(max:1)) {
                    def ident = domainClass.list(max:1).get(0).ident()
                    def identType = ProcessVariable.defineType(ident.getClass())
                    def key = ProcessVariable.getConvertedValue(value, identType)
                    return domainClass.get(key)
                }
            } catch (Exception e) {
                log.error("Exception occured in getting object of type '${type}' by key", e)
                flash.errors << "${name} cannot be converted to domain object of ${type} type"
                return null
            }
        }

        def type = ProcessVariable.defineType(variableType)
        switch (type) {
            case ProcessVariable.DATE :
                try {
                    def value = params.remove(parameterName)
                    return GrailsflowUtils.getParsedDate(value, gf.datePattern()?.toString())
                } catch (Exception e) {
                    flash.errors << "$name cannot be parsed to Date value: $e"
                    return null
                }
            case ProcessVariable.DOUBLE :
                try {
                    def value = params.remove(parameterName)
                    if (!value) return null;
                    return java.text.NumberFormat.getInstance(request.locale).parse(value)
                } catch (Exception e) {
                    flash.errors << "$name cannot be parsed to Double value"
                    return null
                }
            case ProcessVariable.INTEGER :
                try {
                    def value = params.remove(parameterName)
                    if (!value) return null;
                    return Integer.valueOf(value)
                } catch (java.lang.NumberFormatException e) {
                    flash.errors << "$name cannot be parsed to Integer value"
                    return null
                }
            case ProcessVariable.LONG :
                try {
                    def value = params.remove(parameterName)
                    if (!value) return null;
                    return Long.valueOf(value)
                } catch (java.lang.NumberFormatException e) {
                    flash.errors << "$name cannot be parsed to Integer value"
                    return null
                }
            case ProcessVariable.BOOLEAN :
                if (!params[parameterName]) return Boolean.FALSE
                Boolean result = params.remove(parameterName) == "on" ? Boolean.TRUE : Boolean.FALSE
                params.remove("_${parameterName}".toString())
                return result
            case ProcessVariable.DOCUMENT:
                File documentsRoot = workareaPathProvider.getResourceFile(documentsPath)
                Document document = GrailsflowRequestUtils.getDocumentFromRequest(request, parameterName, documentsRoot)
                params.remove(parameterName)
                return document
            case ProcessVariable.LINK :
                Link link = GrailsflowRequestUtils.getLinkFromParams(params, parameterName)
                params.remove(parameterName)
                params.remove("${parameterName}.path".toString())
                params.remove("${parameterName}.description".toString())
                return link
            case ProcessVariable.LIST :
                params.datePattern = gf.datePattern()?.toString()
                try {
                    List<Object> listItems = GrailsflowRequestUtils.getVariableItemsFromParams(variable?.name, params)
                    params.findAll {String key, value -> key.startsWith("listItemType_${variable?.name}_") }
                            .each { params.remove(it.key) }
                    params.findAll { key, value -> key.indexOf("listItemValue_${variable?.name}_") != -1 }
                            .each { params.remove(it.key) }
                    params.findAll {String key, value -> key.indexOf("parent_varType_${variable?.name}") != -1 }
                            .each { params.remove(it.key) }
                    params.findAll {String key, value -> key.indexOf("previousType_${variable?.name}") != -1 }
                            .each { params.remove(it.key) }
                    return listItems
                } catch (Exception ex) {
                    flash.errors << ex
                    return null
                }
            default:
                return params.remove(parameterName)
        }
    }
}
