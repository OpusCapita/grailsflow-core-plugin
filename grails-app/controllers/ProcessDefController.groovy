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

import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.StringUtils

import com.jcatalog.grailsflow.utils.NameUtils
import com.jcatalog.grailsflow.utils.TranslationUtils
import com.jcatalog.grailsflow.utils.AuthoritiesUtils

import com.jcatalog.grailsflow.utils.JSONConverter

import com.jcatalog.grailsflow.model.definition.ProcessDef
import com.jcatalog.grailsflow.model.definition.ProcessNodeDef
import com.jcatalog.grailsflow.model.definition.ProcessDefAssignee

import com.jcatalog.grailsflow.model.graphics.ProcessNodeDefPosition

import com.jcatalog.grailsflow.engine.concurrent.ProcessTypeLock

import com.jcatalog.grailsflow.model.process.BasicProcess
import com.jcatalog.grailsflow.engine.ProcessBuilder
import com.jcatalog.grailsflow.process.script.ProcessScript

import grails.converters.JSON
import java.text.DateFormat
import java.text.SimpleDateFormat
/**
 * Process definition controller class is used for executing actions from UI
 * These actions act with process scripts: generation and editing.
 *
 * methods:
 *
 *   - showProcessScript -- shows process script
 *   - processDefinition -- creates new process definition
 *   - createProcess -- shows UI for process definition creation
 *   - saveProcess -- saves process definition
 *   - reloadProcessDef -- reloads process definition from file
 *   - generateProcess -- generates process script
 *   - editTypes -- shows list of process types
 *   - editProcess -- shows process editor (by id)
 *   - editProcessTranslations -- shows process translations editor
 *   - saveProcessTranslations -- saves process translations
 *   - editProcessDef -- shows process editor (by type)
 *   - addNodeDef -- shows UI for adding node
 *   - addVarDef -- shows UI for adding variable
 *   - addAssignee -- asynchronous assignee adding
 *   -
 *   - deleteProcessDef -- removes process definition, class, script
 *   - exportAsHTML -- exports process definition as HTML
 *   - editNodeDef -- shows node editor
 *   - showGraphic -- shows graphics UI
 *   - saveNodesPositions -- saves graphics positions
 *
 *
 * @author Stephan Albers
 * @author July Karpey
 * @author Maria Voitovich
 */
class ProcessDefController extends GrailsFlowSecureController {
    private static final String RESOURCE_BUNDLE = "grailsflow.processTypes"
    private static final String DETAILS_BUNDLE = "grailsflow.processDetails"

    def processExporterService
    def processManagerService
    def generateProcessService
    def processScriptProvider

    def grailsflowMessageBundleService
    def processFactory

    def processDefValidator
    def datePatterns

    def index = {
        flash.message = ""
        redirect(action: "editTypes")
    }

    // the delete, save actions only accept POST requests
    def static allowedMethods = [delete: 'POST', save: 'POST']

    def showProcessScript = {
        def processScript = processScriptProvider.readProcessScript(params.processID)

        render(view: "processScript", model: [processID: params.id,
               processType: params.processID,
               processCode: processScript ? processScript.source: null])
    }

    def processDefinition = {
        flash.message = ""
        flash.errors = []
        if (!params.processID) {
            flash.errors << grailsflowMessageBundleService
                                .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processID.required")
            forward(action: "createProcess", params: params)
        } else {
            params.processID = params.processID.trim()
            // validate process name
            if (!NameUtils.isValidProcessName(params.processID)) {
                flash.errors << grailsflowMessageBundleService
                                    .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processID.invalid")
                return forward(action: "createProcess", params: params)
            }

            def process = new ProcessDef()
            process.description = [:]
            def processID = NameUtils.upCase(params.processID)
            process.processID = processID

            if (params.description) {
                params.description = StringUtils.replace(params.description, "\\", "\\\\")
                process.description["${request.locale.language}"] = (params.description && params.description.length() > 255) ?
                                       params.description.substring(0, 255) : params.description
            }

            if (!process.save()) {
              process.errors.each() {
                log.error(it)
              }
              flash.errors << grailsflowMessageBundleService
                                  .getMessage(RESOURCE_BUNDLE, "grailsflow.message.generation.error")
              redirect(action: editTypes, params: [sort: params.sort, order: params.order])
            } else {
                redirect(action: editProcess, params: [id: process.id])
            }

        }
    }

    def createProcess = {
        render(view: "createProcess", params: params)
    }


   /**
    * Stores process definition in DB
    *
    */
    def saveProcess = {
      flash.errors = []
      flash.warnings = []
      flash.message = []
      def process = ProcessDef.get(Long.valueOf(params.id))

      process = process ?: new ProcessDef()

      if (params.processID) {
          // Validate Process ID
          if (!NameUtils.isValidProcessName(params.processID)) {
              flash.errors << grailsflowMessageBundleService
                                  .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processID.invalid")
              return forward(action: "editProcess", params: params)
          }

          def processID = NameUtils.upCase(params.processID)
          process.processID = processID
      } else {
          flash.errors << grailsflowMessageBundleService
                              .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processID.required")
          return forward(action: "editProcess", params: params)
      }

      if (params.description) {
          params.description = StringUtils.replace(params.description, "\\", "\\\\")
      }

      if (params.description) {
          if (!process.description) process.description = [:]
          process.description["${request.locale.language}"] = (params.description && params.description.length() > 255) ?
                                   params.description.substring(0, 255) : params.description
      }

      process.validFrom = getParsedDate(params.validFrom)
      process.validTo = getParsedDate(params.validTo)

      if(!process.save()) {
          flash.errors = process.errors
          return redirect(action: editTypes, params: [sort: params.sort, order: params.order])
      }

      def validationResult
      // validate process definition
      if (processDefValidator != null) {
          validationResult = processDefValidator.validate(process)
          validationResult.errors.each() {
              log.debug("Process ${process.processID} validation error: ${it}")
          }
          validationResult.warnings.each() {
              log.debug("Process ${process.processID} validation warning: ${it}")
          }
          flash.errors.addAll(validationResult.errors)
          flash.warnings.addAll(validationResult.warnings)
      }

      return forward(action: "editProcess", params: params)
    }

    /**
     * Reload process definition from script file
     *
     */
    def reloadProcessDef = {
      def processID = params?.processID
      flash.errors = []
      flash.message = []
      if (processID) {
        def processScript = processScriptProvider.readProcessScript(processID)
        if (processScript == null){
		      flash.errors << grailsflowMessageBundleService
		                          .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processFile.notFound")
		      return redirect(action: editProcess, params: [id: params.id])
        }

        try {
            def processDef = reloadProcess(processID)
            redirect(action: editProcess, params: [id: processDef?.id])
        } catch (Exception e) {
            flash.errors << grailsflowMessageBundleService
                                .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processType.invalid", [processID])
            redirect(action: editTypes, params: [sort: params.sort, order: params.order])
        }
      } else {
	      flash.errors << grailsflowMessageBundleService
	                          .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processID.required")
	      return redirect(action: "editProcess", params: [id: params.id])
      }
    }

    def generateProcess = {
        flash.errors = []
        flash.warnings = []
        flash.message = []
        def process = ProcessDef.get(Long.valueOf(params.id))

        if (!process) {
          flash.errors << "Failed to save ProcessDef"
          return forward(action: "editTypes", params: [sort: params.sort, order: params.order])
        }

        if (params.processID) {
            // Validate Process ID
            if (!NameUtils.isValidProcessName(params.processID)) {
                flash.errors << grailsflowMessageBundleService
                                    .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processID.invalid")
                return forward(action: "editProcess", params: params)
            }

            def processID = NameUtils.upCase(params.processID)
            process.processID = processID
        } else {
            flash.errors << grailsflowMessageBundleService
                                .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processID.required")
            return forward(action: "editProcess", params: params)
        }

        if (params.description) {
            params.description = StringUtils.replace(params.description, "\\", "\\\\")
        }

        if (params.description) {
            if (!process.description) process.description = [:]
            process.description["${request.locale.language}"] = (params.description && params.description.length() > 255) ?
                                     params.description.substring(0, 255) : params.description
        }

        process.validFrom = getParsedDate(params.validFrom)
        process.validTo = getParsedDate(params.validTo)

        if(!process.save()) {
            flash.errors = process.errors
            return redirect(action: editTypes, params: [sort: params.sort, order: params.order])
        }

        def validationResult
        // validate process definition
        if (processDefValidator != null) {
            validationResult = processDefValidator.validate(process)
            validationResult.errors.each() {
                log.debug("Process ${process.processID} validation error: ${it}")
            }
            validationResult.warnings.each() {
                log.debug("Process ${process.processID} validation warning: ${it}")
            }
            flash.errors.addAll(validationResult.errors)
            flash.warnings.addAll(validationResult.warnings)
        }

        // delete all started processes if exist
        def startedProcesses = BasicProcess.findAllWhere("type": process.processID)
        startedProcesses?.each { proc ->
            if (proc.status?.statusID in ["ACTIVATED", "SUSPENDED"]) {
                processManagerService.killProcess(proc.id, securityHelper.getUser(session))
            }
        }

        // delete process builder from cache
        processFactory.removeProcessClass(process.processID)

        // try to generate groovy script from process definition
        def result = generateProcessService.generateGroovyProcess(process)
        if (!result) {
            flash.errors << grailsflowMessageBundleService
                              .getMessage(RESOURCE_BUNDLE, "grailsflow.message.generation.error")
        } else {
            flash.message = grailsflowMessageBundleService
                              .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processType.saved", [process.processID])
            // remove ProcessDefinition from DB
            def processDef = ProcessDef.findWhere("processID": params.id)
            if (processDef) {
                ProcessNodeDefPosition.findAllWhere("processDef": processDef)*.delete()
                processDef.delete()
            }


        }

        redirect(action: editTypes, params: [sort: params.sort, order: params.order])
    }

    def editTypes = {
        flash.message = ""
        flash.errors = []

        if (!params.sort) params.sort = "type"
        if (!params.order) params.order = "asc"
        def lang = request.locale.language.toString()
        def scripts = processManagerService.getSupportedProcessScripts()
        def processClasses = scripts.keySet().
              sort{a, b ->
                def labelA = scripts[a] ?
                  TranslationUtils.getTranslatedValue(scripts[a].label, scripts[a].processType, lang) : a
                def labelB = scripts[b] ?
                  TranslationUtils.getTranslatedValue(scripts[b].label, scripts[b].processType, lang) : b
                return (params.order == "asc") ?
                    labelA.compareTo(labelB) :
                    -labelA.compareTo(labelB) }
        render(view: "editTypes", model: [processClasses: processClasses, scripts: scripts], params: params)
    }

    def editProcess = {
        if (!flash.message) flash.message = ""
        def processDef = params.id ? ProcessDef.get(Long.valueOf(params.id)) : null

        if (!processDef) {
            flash.errors = ["Impossible to edit process with key ${params.id}"]
            return redirect(action: editTypes)
        }

        render(view: "editProcess", model: [processDetails: processDef])
    }

    def editProcessTranslations = {
        if (!flash.message) flash.message = ""
        def processDef = params.id ? ProcessDef.get(Long.valueOf(params.id)) : null

        if (!processDef) {
            flash.errors = ["Impossible to edit process with key ${params.id}"]
            return redirect(action: editTypes)
        }

        render(view: "editProcessTranslations", model: [processDef: processDef])
    }

    def saveProcessTranslations = {
        if (!flash.message) flash.message = []
        def processDef = params.id ? ProcessDef.get(Long.valueOf(params.id)) : null
        if (!processDef) {
            flash.errors = ["Impossible to edit process with key ${params.id}"]
            return redirect(action: editTypes)
        }
        def labels = GrailsflowRequestUtils.getTranslationsMapFromParams(params, 'label_')
        def descriptions = GrailsflowRequestUtils.getTranslationsMapFromParams(params, 'description_')
        processDef.label = labels
        processDef.description = descriptions

        processDef.save()

        redirect(action: editProcess, params: [id: params.id])
    }


    def editProcessDef = {
        // Reload process definition in the DB, if it's already there or
        // parse the process definition class to read the nodes and transitions
        // and store them in the DB for editing purpose
        try {
            def processDef = reloadProcess(params.id)
            redirect(action: editProcess, params: [id: processDef?.id])
        } catch (Exception e) {
            flash.message = grailsflowMessageBundleService
                                .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processType.invalid", [params.id])
            log.error("Cannot build process definition for ${params.id}", e)
            redirect(action: editTypes, params: [sort: params.sort, order: params.order])
        }
    }

    def editProcessScript = {
        flash.errors = []

        if (!params.id) {
            flash.errors << grailsflowMessageBundleService
                              .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processType.empty")
            return redirect(action: editTypes)
        }

        def processScript = params.code ?
            new ProcessScript(params.id, params.code, new Date()) : processScriptProvider.readProcessScript(params.id)
        def builder = new ProcessBuilder(processScript)

        if (builder.processClass != null && builder.errors.size() == 0) {
            flash.message = grailsflowMessageBundleService
                              .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processScript.valid")
        } else {
              flash.errors.addAll(builder.errors)
        }

        render(view: "editProcessScript", model: [processType: params.id, processCode: processScript?.source])

    }

    def saveProcessScript = {
        if (!flash.errors) flash.errors = []
        if (!params.id) {
            flash.errors << grailsflowMessageBundleService
                              .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processType.empty")
            return redirect(action: editTypes)
        }

        // delete process definition if exist
        if (!deleteProcessInfo(params?.id)) {
            flash.errors = [grailsflowMessageBundleService
                              .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processScript.deleteError")]
        } else {
            flash.message = grailsflowMessageBundleService
                              .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processType.deleted", [params.id])
        }

        def result
        // create and write groovy script
        synchronized (ProcessTypeLock.getLock(params.id)) {
            try{
               result = processScriptProvider
                          .writeProcessScript(params.id, params.code)
            } catch (Throwable ex){
                log.error("Unexpected exception occurred in synchronized block! Please, contact to administrator. ", ex)
                result = Boolean.FALSE
            }
        }

        if (result) {
           flash.message = grailsflowMessageBundleService
                             .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processScript.saved", [params.id])
        } else flash.errors << grailsflowMessageBundleService
                                 .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processScript.saveError")

        redirect(action: editTypes)
    }

    def deleteProcessScript = {
        if (!flash.errors) flash.errors = []

        redirect(action: deleteProcessDef, params: params)
    }

    def addNodeDef = {
        chain(controller: "processNodeDef", action: "addNodeDef", params: params)
    }

    def addVarDef = {
        chain(controller: "processVarDef", action: "addVarDef", params: params)
    }

    /**
     * Asynchronous add of process assignees.
     * parameters:
     *  - id	  			  processDef ID
     *  - authority_type	  'users' or 'roles'
     *  - userAssignees	    comma-separated list of users or roles
     *
     */
    def addAssignees = {
        def processDef = ProcessDef.get(Long.valueOf(params.id))
        def assignees
        switch (params.authority_type) {
          case 'users':
            assignees = AuthoritiesUtils.getUserAuthorities(params.userAssignees?.split(",")*.trim())
            break;
          case 'roles':
            assignees = AuthoritiesUtils.getRoleAuthorities(params.roleAssignees?.split(",")*.trim())
            break;
          case 'groups':
            assignees = AuthoritiesUtils.getGroupAuthorities(params.groupAssignees?.split(",")*.trim())
            break;
          default:
            break;
        }

        def currentAssignees = processDef?.processAssignees?.collect() { it.assigneeID }

        def addedAssignees = []
        assignees?.unique()?.each() { id ->
          // skip duplications
          if (! currentAssignees.contains(id)) {
            def assignee = new ProcessDefAssignee(processDef: processDef, assigneeID: id)
            processDef.addToAssignees(assignee)
            addedAssignees << id
          }
        }
        if (!addedAssignees.isEmpty()) {
          processDef.save(flush: true)
        }
        switch (params.authority_type) {
          case 'users':
            addedAssignees = AuthoritiesUtils.getUsers(addedAssignees)
            break;
          case 'roles':
            addedAssignees = AuthoritiesUtils.getRoles(addedAssignees)
            break;
          case 'groups':
            addedAssignees = AuthoritiesUtils.getGroups(addedAssignees)
            break;
          default:
            break;
        }
        def result = [authorityType: params.authority_type, addedAssignees: addedAssignees]
        render result as JSON
    }

    /**
     * Asynchronous remove of process assignee.
     * parameters:
     *  - id	  			  processDef ID
     *  - authority_type	  'users' or 'roles'
     *  - assigneeID	      ID of user or role
     *
     */
    def deleteAssignee = {
        def processDef = ProcessDef.get(Long.valueOf(params.id))
        def assigneeID = params.assigneeID
        switch (params.authority_type) {
          case 'users':
            assigneeID = AuthoritiesUtils.getUserAuthority(assigneeID)
            break;
          case 'roles':
            assigneeID = AuthoritiesUtils.getRoleAuthority(assigneeID)
            break;
          case 'groups':
            assigneeID = AuthoritiesUtils.getGroupAuthority(assigneeID)
            break;
          default:
            break;
        }
        def removedAssignee = null
        def assignee = processDef.processAssignees?.find() { it.assigneeID ==  assigneeID }
        if (assignee) {
            removedAssignee = params.assigneeID
            processDef.removeFromAssignees(assignee)
            processDef.save(flush: true)
        }
        def result = [authorityType: params.authority_type, removedAssignee: removedAssignee]
        render result as JSON
    }

    def deleteProcessDef = {
        if (!params.id) {
            flash.errors << grailsflowMessageBundleService
                              .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processType.empty")
            return redirect(action: editTypes)
        }
        if (!deleteProcessInfo(params?.id)) {
            flash.errors = [grailsflowMessageBundleService
                              .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processScript.deleteError")]
        } else {
            flash.message = grailsflowMessageBundleService
                              .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processType.deleted", [params.id])
        }
        redirect(action: editTypes)
    }

    def exportAsHTML = {
        if (!params.id) return redirect(action: editTypes, params: [sort: params.sort, order: params.order])

        def processDef = ProcessDef.get(Long.valueOf(params.id))
        if (!processDef) {
            flash.message = "There is no process definition with key '${params.id}'"
            return redirect(action: editTypes, params: [sort: params.sort, order: params.order])
        }

        try {
            def file = processExporterService.exportAsHTML(processDef, securityHelper.getUser(session), request.locale.language.toString())
            response.contentType = ProcessExporterService.CONTENT_TYPE_ARCHIVE
            response.addHeader('Content-Length', file?.length()?.toString())
            response.addHeader('Content-disposition',
                    "attachment;filename=${processDef.processID}.${FilenameUtils.getExtension(file?.name)}")
            response.outputStream << file?.readBytes()
        } catch (Exception e) {
            flash.message = e.message
            redirect(action: editProcess, params: [id: params.id])
        }
    }

    def editNodeDef = {
        def process = ProcessDef.get(Long.valueOf(params.processID))
        def node = ProcessNodeDef.findWhere(processDef: process, nodeID: params.nodeID)
        redirect(controller: "processNodeDef", action: "editNodeDef", params: [id: node.id])
    }

    def showGraphic = {
        if (!params.processID) return []

        def processDef = ProcessDef.get(Long.valueOf(params.processID))

        def trList = []
        def nodeInfos = []
        def isPositionsHandled = Boolean.TRUE

        processDef?.nodes?.each() { node ->
            def nPos = ProcessNodeDefPosition
                           .findWhere("processDef": processDef,
                                      "nodeID": node.nodeID)
            if (!nPos || !nPos.startX) {
                isPositionsHandled = Boolean.FALSE
            }
            Map position = nPos
                .properties["nodeID", "actionType", "knotType", "dueDate", "startX", "startY", "width", "height"]
            position.nodeLabel = gf.translatedValue(translations: node.label, default: node.nodeID)?.toString()
            position.knotTypeLabel = position.knotType ? grailsflowMessageBundleService
                .getMessage(DETAILS_BUNDLE, "grailsflow.label.graphic.node.${position.knotType}") : ''
                  nodeInfos << position
		    node.transitions?.each() { transition ->
		        Map trans = [:]
		        trans.fromNodeID = transition.fromNode.nodeID
		        trans.onEventID = transition.event
                trans.eventLabel = gf.translatedValue(translations: transition.label, default: transition.event)?.toString()
                trans.toNodeIDs = transition.toNodes.collect() { it.nodeID }.join(",")
		        trList << trans
		    }
        }

        def transitionsJson = JSONConverter.toJSON(trList)
        def positions = JSONConverter.toJSON(nodeInfos)

        [transitionsJson: transitionsJson, processID: processDef.id,
         isPositionsHandled: isPositionsHandled, positions: positions]
    }

    def saveNodesPositions = {
        def nodesInfo = params.positions.split(";")
        def processDef = ProcessDef.get(Long.valueOf(params.id))
        nodesInfo.each() {
            def nodeInf = it.split(",")
            def nodePosition = ProcessNodeDefPosition
                                   .findWhere("processDef": processDef,
                                              "nodeID": nodeInf[0])
            nodePosition.startX = Integer.valueOf(nodeInf[1])
            nodePosition.startY = Integer.valueOf(nodeInf[2])
            nodePosition.width = Integer.valueOf(nodeInf[3])
            nodePosition.height = Integer.valueOf(nodeInf[4])
            nodePosition.save()
        }
        render ""
    }

    private String getViewsPath(def app) {
        def viewsPath
        String applicationPath = app.parentContext.servletContext.getRealPath("")
        if (grailsApplication.isWarDeployed()) {
            viewsPath = new File(applicationPath).getAbsolutePath() + "/WEB-INF"
        } else {
            viewsPath = new File(applicationPath, "..").getAbsolutePath()
        }
        viewsPath += "/grails-app/views/manualForms"
        return viewsPath
    }

    private def deleteProcessInfo(String type) {
      synchronized (ProcessTypeLock.getLock(type)) {
        try{
            def startedProcesses = BasicProcess.findAllWhere("type": type)
            startedProcesses?.each {process ->
                if (process.status?.statusID in ["ACTIVATED", "SUSPENDED"]) {
                    processManagerService.killProcess(process.id, securityHelper.getUser(session))
                }
            }

            // remove ProcessDefinition from DB
            def processDef = ProcessDef.findWhere("processID": params.id)
            if (processDef) {
              ProcessNodeDefPosition.findAllWhere("processDef": processDef)*.delete()
              processDef.delete()
            }

            // delete script
            def result = processScriptProvider.deleteProcessScript(params.id)
            log.debug("Script with type ${params.id} was deleted: $result")

            // delete process builder from cache
            processFactory.removeProcessClass(params.id)

            def viewsPath = getViewsPath(grailsApplication)
            def manualPagesContainer = new File(viewsPath + "/" + params.id)
            if (manualPagesContainer.exists()) {
                manualPagesContainer.listFiles().each() {
                    it.delete()
                }
                manualPagesContainer.delete()
            }

        } catch (Throwable ex){
                log.error("Unexpected exception occurred in synchronized block! Please, contact to administrator. ", ex)
                return Boolean.FALSE
        }

        return Boolean.TRUE
      }

    }

    private Date getParsedDate(String dateString) {
        return GrailsflowUtils.getParsedDate(dateString, gf.datePattern()?.toString())
    }

    private def reloadProcess (String processID) {
        def type = processID
        def processDef = null

        synchronized(ProcessTypeLock.getLock(type)){
            processDef = ProcessDef.findWhere(processID: processID)
            // remove ProcessDefinition from DB
            if (processDef) {
                ProcessNodeDefPosition.findAllWhere("processDef": processDef)*.delete()
                processDef.delete()
            }

            // delete process builder from cache
            processFactory.removeProcessClass(processID)

            // read process form process file
            def processClass = processManagerService.getProcessClass(processID)
            processDef = generateProcessService.buildProcessDefinition(processClass)
        }

        return processDef
    }
}
