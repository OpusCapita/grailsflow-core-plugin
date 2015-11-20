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

import java.math.BigInteger
import org.apache.commons.lang.time.DateUtils
import org.springframework.util.FileCopyUtils
import com.jcatalog.grailsflow.utils.ConstantUtils
import com.jcatalog.grailsflow.utils.NameUtils
import com.jcatalog.grailsflow.utils.AuthoritiesUtils

import org.apache.commons.lang.StringUtils
import com.jcatalog.grailsflow.bean.NodeDetails

import com.jcatalog.grailsflow.model.definition.ProcessDef
import com.jcatalog.grailsflow.model.definition.ProcessDefAssignee
import com.jcatalog.grailsflow.model.definition.ProcessNodeDef
import com.jcatalog.grailsflow.model.definition.Variable2NodeVisibility

import com.jcatalog.grailsflow.model.graphics.ProcessNodeDefPosition

import grails.converters.JSON

/**
* ProcessNodeDefinition controller class is used for managing node definitions
* in Process Editor.
* It is possible to create/change/delete definition, add custom pages for 'Wait' nodes,
* define variables visibility.
*
* @author Stephan Albers
* @author July Karpey
*/
class ProcessNodeDefController extends GrailsFlowSecureController {
    def processManagerService

    def index = {
        redirect(controller: "processDef")
    }

    def static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def addNodeDef = {
        params.dueDate_days = 0
        params.dueDate_hours = 0
        params.dueDate_minutes = 0
        params.expectedDuration_days = 0
        params.expectedDuration_hours = 0
        params.expectedDuration_minutes = 0
        params.formType = 1
        render(view: 'nodeForm', model: [node: new ProcessNodeDef(),
               process: params.id ? ProcessDef.get(Long.valueOf(params.id)) : null,
               params:params])
    }

    def orderMoveUp = {
        def node = ProcessNodeDef.get(Long.valueOf(params.id))
        def processDef = node?.processDef
        def result
        if (!node || !processDef) {
            log.debug("Nothing found to update for id=${params.id}")
            result = [orderChanged: false]
        } else if (node == processDef.nodes[0]) {
            log.debug("Cannot move up first element")
            result = [orderChanged: false]
        } else {
            log.debug("Updating nodes order for process ${processDef.processID}")
            def oldOrder = processDef.nodes.findIndexOf() { it == node }
            processDef.nodes = moveElementUp(processDef.nodes, node)
            if (!processDef.save(flush: true)) {
                processDef.errors.each() {
                    log.error(it)
                }
                result = [errors: processDef.errors, orderChanged: false]
            } else
                result = [orderChanged: true, oldOrder: oldOrder]
        }
        render result as JSON
    }

    def orderMoveDown = {
        def node =  ProcessNodeDef.get(Long.valueOf(params.id))
        def processDef = node?.processDef
        def result
        if (!node || !processDef) {
            log.debug("Nothing found to update for id=${params.id}")
            result = [orderChanged: false]
        } else if (node == processDef.nodes[-1]) {
            log.debug("Cannot move up first element")
            result = [orderChanged: false]
        } else {
            log.debug("Updating nodes order for process ${processDef.processID}")
            def oldOrder = processDef.nodes.findIndexOf() { it == node }
            processDef.nodes = moveElementDown(processDef.nodes, node)
            if (!processDef.save(flush: true)) {
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

    def editNodeDef = {
        def node = ProcessNodeDef.get(Long.valueOf(params.id))

        def dueDate = new BigInteger( (node.dueDate != null) ? node.dueDate.toString() : "0")

        params.dueDate_days = dueDate.divide(DateUtils.MILLIS_PER_DAY).longValue()
        dueDate = dueDate.remainder(DateUtils.MILLIS_PER_DAY)
        params.dueDate_hours = dueDate.divide(DateUtils.MILLIS_PER_HOUR).longValue()
        dueDate = dueDate.remainder(DateUtils.MILLIS_PER_HOUR)
        params.dueDate_minutes = dueDate.divide(DateUtils.MILLIS_PER_MINUTE).longValue()

        def expectedDuration = new BigInteger( (node.expectedDuration != null) ? node.expectedDuration.toString() : "0")

        params.expectedDuration_days = expectedDuration.divide(DateUtils.MILLIS_PER_DAY).longValue()
        expectedDuration = expectedDuration.remainder(DateUtils.MILLIS_PER_DAY)
        params.expectedDuration_hours = expectedDuration.divide(DateUtils.MILLIS_PER_HOUR).longValue()
        expectedDuration = expectedDuration.remainder(DateUtils.MILLIS_PER_HOUR)
        params.expectedDuration_minutes = expectedDuration.divide(DateUtils.MILLIS_PER_MINUTE).longValue()

        def viewsPath = getViewsPath(grailsApplication)
        def controllersPath = getControllersPath(grailsApplication)
        def formTextArea
        def controllerTextArea
        def multiPages = []

        // define type of form editor
        def type = node.editorType
        // editor type can be changed from UI (params.formType is not null)
        if (params.formType) {
            type = (params.formType != 1) ? ConstantUtils.EDITOR_MANUAL : ConstantUtils.EDITOR_AUTO
        }

        switch (type) {
            case [ConstantUtils.EDITOR_AUTO, null]:
                params.formType = "1"
                break
            case ConstantUtils.EDITOR_MANUAL:
                def formFile = new File(viewsPath + "/" + node.processDef.processID + "/_"
                                        + node.nodeID + ".gsp")
                if (formFile.exists()) {
                    formTextArea = formFile.text
                }

                def controllerFile = new File(controllersPath + "/"
                                              + node.processDef.processID
                                              + "_" + node.nodeID + "Controller.groovy")
                if (controllerFile.exists()) {
                    controllerTextArea = controllerFile.text
                    viewsPath = viewsPath.substring(0, viewsPath.length() - 12)
                    String processFolder = NameUtils.downCase(node.processDef.processID)

                    def files = new File(viewsPath + "/" + processFolder + "_"
                                         + node.nodeID).listFiles()
                    files.each() {
                        multiPages << it.name
                    }
                }

                if (controllerTextArea) params.formType = "3"
                else params.formType = "2"
                break
            default: break
        }

        render(view: 'nodeForm',
               model: [node: node, formTextArea: formTextArea,
               pageTextArea: formTextArea, controllerTextArea: controllerTextArea,
               multiPages: multiPages, process: node.processDef,
               externalUrl: node.externalUrl, params: params])
    }

    /**
     * Asynchronous add of node assignees.
     * parameters:
     *  - ndID	  					nodeDef ID
     *  - authority_type	  'users' or 'roles'
     *  - userAssignees	    comma-separated list of users or roles
     *
     */
    def addAssignees = {
        def node = ProcessNodeDef.get(Long.valueOf(params.ndID))
        def processDef = node?.processDef
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

        def currentAssignees = node?.assignees?.collect() { it.assigneeID }

        def addedAssignees = []
        assignees?.each() { id ->
          // skip duplications
          if (! currentAssignees.contains(id)) {
            def assignee = new ProcessDefAssignee(processDef: processDef, assigneeID: id, processNodeDef: node)
            node.addToAssignees(assignee)
            assignee.save()
            addedAssignees << id
          }
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
     * Asynchronous remove of node assignee.
     * parameters:
     *  - ndID	  					nodeDef ID
     *  - authority_type	  'users' or 'roles'
     *  - assigneeID	      ID of user or role
     *
     */
    def deleteAssignee = {
        def nodeDef = ProcessNodeDef.get(Long.valueOf(params.ndID))
        def processDef = nodeDef?.processDef
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
        def assignee = ProcessDefAssignee.findWhere(processDef: processDef, processNodeDef: nodeDef, assigneeID: assigneeID)
        if (assignee) {
	        assignee.delete()
          removedAssignee = params.assigneeID
        }
        def result = [authorityType: params.authority_type, removedAssignee: removedAssignee]
        render result as JSON
    }

    def generateManualForm = {
        def processDef = ProcessDef.get(Long.valueOf(params.id))
        def node = ProcessNodeDef.get(Long.valueOf(params.ndID))

        def viewsPath = getViewsPath(grailsApplication)

        if (params.formTextArea) {
            try {
                String path = viewsPath + "/" + processDef.processID + "/_" + node.nodeID + ".gsp"
                if (!new File(viewsPath + "/" + processDef.processID).exists()) {
                    new File(viewsPath + "/" + processDef.processID).mkdir()
                }
                FileOutputStream fileOutputStream = new FileOutputStream(path)
                FileCopyUtils.copy(new ByteArrayInputStream(
                        params.formTextArea.getBytes("UTF-8")), fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                log.debug("Generating manual form is finished")
            } catch (Exception e) {
                log.error("Errors in generating manual forms", e)
            }
        }

        // delete controller and multi-step pages
        // if they are existed
        def controllersPath = getControllersPath(grailsApplication)
        if (new File(controllersPath + "/" + processDef.processID + "_" + node.nodeID + "Controller.groovy").exists()) {
            new File(controllersPath + "/" + processDef.processID + "_" + node.nodeID + "Controller.groovy").delete()
        }
        viewsPath = viewsPath.substring(0, viewsPath.length() - 12)
        String processFolder = NameUtils.downCase(processDef.processID)
        def multiPages = new File(viewsPath + "/" + processFolder + "_" + node.nodeID)
        if (multiPages.exists()) {
            multiPages.listFiles().each() {
                it.delete()
            }
            multiPages.delete()
        }

        params.id = node.id
        params.formType = "2"
        params.formTextArea = null
        params.controllerTextArea = null
        params.pageTextArea = null
        forward(action: "editNodeDef", params: params)
    }

    def generateManualActivity = {
        def processDef = ProcessDef.get(Long.valueOf(params.id))
        def node = ProcessNodeDef.get(Long.valueOf(params.ndID))

        def viewsPath = getViewsPath(grailsApplication)
        def controllersPath = getControllersPath(grailsApplication)

        try {
            FileOutputStream fileOutputStream
            String path = viewsPath + "/" + processDef.processID + "/_" + node.nodeID + ".gsp"
            if (params.pageTextArea) {
                if (!new File(viewsPath + "/" + processDef.processID).exists()) {
                    new File(viewsPath + "/" + processDef.processID).mkdir()
                }
                fileOutputStream = new FileOutputStream(path)
                FileCopyUtils.copy(new ByteArrayInputStream(
                        params.pageTextArea.getBytes("UTF-8")), fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
            }

            if (params.controllerTextArea) {
                String controllerText = "class " + node.processDef.processID + "_" + node.nodeID
                if (params.controllerTextArea.startsWith(controllerText)) {
                    controllerText = params.controllerTextArea
                } else {
                    controllerText += "Controller extends GrailsFlowSecureController {\n"
                    controllerText += " def index = { redirect( controller: 'processDef', action: 'editTypes') } \n"
                    controllerText += params.controllerTextArea + "\n"
                    controllerText += "}"
                }
                path = controllersPath + "/" + node.processDef.processID + "_" + node.nodeID + "Controller.groovy"
                fileOutputStream = new FileOutputStream(path)
                FileCopyUtils.copy(new ByteArrayInputStream(
                        controllerText.getBytes("UTF-8")), fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                log.debug("Generating manual form is finished")
            }
        } catch (Exception e) {
            log.error("Errors in generating manual activity", e)
        }

        params.id = node.id
        params.formType = "3"
        params.formTextArea = null
        params.controllerTextArea = null
        params.pageTextArea = null
        forward(action: "editNodeDef", params: params)
    }

    def addMultiPage = {
        def processDef = ProcessDef.get(Long.valueOf(params.id))
        def node = ProcessNodeDef.get(Long.valueOf(params.ndID))

        def viewsPath = getViewsPath(grailsApplication)
        viewsPath = viewsPath.substring(0, viewsPath.length() - 12)

        def multiStepPage = "<html> \n"
        multiStepPage += "<head><meta name='layout' content='main'></meta> \n"
        multiStepPage += "</head><body><div name=content> \n"
        multiStepPage += params.multiStepPage + "\n"
        multiStepPage += "</div></body> \n"
        multiStepPage += "</html>"

        String processFolder = NameUtils.downCase(processDef.processID)
        try {
            FileOutputStream fileOutputStream
            if (!params.pageName) {
                params.pageName = "no_name"
            }
            String path = viewsPath + "/" + processFolder + "_" + node.nodeID + "/" + params.pageName + ".gsp"
            if (multiStepPage) {
                if (!new File(viewsPath + "/" + processFolder + "_" + node.nodeID).exists()) {
                    new File(viewsPath + "/" + processFolder + "_" + node.nodeID).mkdir()
                }
                fileOutputStream = new FileOutputStream(path)
                FileCopyUtils.copy(new ByteArrayInputStream(
                        multiStepPage.getBytes("UTF-8")), fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
            }
        } catch (Exception e) {
            log.error("Exception in saving manual form", e)
        }

        def multiPages = []
        def files = new File(viewsPath + "/" + processFolder + "_" + node.nodeID).listFiles()
        files.each() {
            multiPages << it.name
        }

        params.formType = "3"
        render(view: 'nodeForm', model: [node: node,
               pageTextArea: params.pageTextArea,
               controllerTextArea: params.controllerTextArea,
               multiPages: multiPages, process: node.processDef,
               params: params])
    }

    def deleteMultiPage = {
        def processDef = ProcessDef.get(Long.valueOf(params.id))
        def node = ProcessNodeDef.get(Long.valueOf(params.ndID))

        def viewsPath = getViewsPath(grailsApplication)
        viewsPath = viewsPath.substring(0, viewsPath.length() - 12)
        String processFolder = NameUtils.downCase(processDef.processID)
        def multiPages = []
        def files = new File(viewsPath + "/" + processFolder + "_" + node.nodeID)
                        .listFiles()
        files.each() {
            if (it.name == params.pageName) {
                it.delete()
            } else multiPages << it.name
        }

        params.formType = "3"
        render(view: 'nodeForm', model: [node: node,
               pageTextArea: params.pageTextArea,
               controllerTextArea: params.controllerTextArea,
               multiPages: multiPages, process: node.processDef,
               params: params])
    }

    def editNodeTranslations = {
        if (!flash.message) flash.message = ""
        def processNodeDef = params.id ? ProcessNodeDef.get(Long.valueOf(params.id)) : null

        if (!processNodeDef) {
            flash.errors = ["Impossible to edit node with key ${params.id}"]
            return redirect(controller: 'processDef', action: 'editTypes')
        }

        render(view: 'editNodeTranslations', model: [processNodeDef: processNodeDef])
    }

    def saveNodeTranslations = {
        if (!flash.message) flash.message = []
        def processNodeDef = params.id ? ProcessNodeDef.get(Long.valueOf(params.id)) : null
        if (!processNodeDef) {
            flash.errors = ["Impossible to edit node with key ${params.id}"]
            return redirect(controller: 'processDef', action: 'editTypes')
        }
        def labels = GrailsflowRequestUtils.getTranslationsMapFromParams(params, 'label_')
        def descriptions = GrailsflowRequestUtils.getTranslationsMapFromParams(params, 'description_')
        processNodeDef.label = labels
        processNodeDef.description = descriptions
        processNodeDef.save()
        redirect(action: 'editNodeDef', params: [id: params.id])
    }


    def saveNodeDef = {
        if (!flash.errors) flash.errors = []

        def process = ProcessDef.get(Long.valueOf(params.id))
        def node, nodePosition

        if (params.ndID) {
            node = ProcessNodeDef.get(Long.valueOf(params.ndID))
        } else {
            node = new ProcessNodeDef()
            node.processDef = process
            node.properties = params
        }

        // Validate parameters
        if (!params.nodeID) {
            flash.errors << g.message(code: "plugin.grailsflow.message.nodeID.required")
            return render(view: 'nodeForm', model: [node: node, process: process])
        } else {
            if (!NameUtils.isValidIdentifier(params.nodeID)) {
                flash.errors << g.message(code: "plugin.grailsflow.message.nodeID.invalid")
                return render(view: 'nodeForm', model: [node: node, process: process])
            }
        }

        def ndID = params.ndID ? Long.valueOf(params.ndID) : null
        def duplicateNode = ProcessNodeDef.findWhere(processDef: process, nodeID: params.nodeID)
        if ( duplicateNode && (ndID == null || ndID != duplicateNode.id) ) {
            flash.errors << g.message(code: "plugin.grailsflow.message.nodeID.duplicated")
            return render(view: 'nodeForm', model: [node: new ProcessNodeDef(nodeID: params.nodeID), process: process])
        }

        if (ndID != null) {
            node = ProcessNodeDef.get(Long.valueOf(ndID))
            nodePosition = ProcessNodeDefPosition
                               .findWhere("processDef": process, "nodeID": node.nodeID)
        } else {
            node = new ProcessNodeDef()
            node.processDef = process
            process.addToNodes(node)
            nodePosition = new ProcessNodeDefPosition()
        }

        def dueDate = 0
        def dueDate_days
        try {
            dueDate_days = Long.parseLong(params.dueDate_days)
        } catch (NumberFormatException e) {
            dueDate_days = Long.valueOf(0)
        }
        dueDate += dueDate_days * DateUtils.MILLIS_PER_DAY

        def dueDate_hours
        try {
            dueDate_hours = Long.parseLong(params.dueDate_hours)
        } catch (NumberFormatException e) {
            dueDate_hours = Long.valueOf(0)
        }
        dueDate += dueDate_hours * DateUtils.MILLIS_PER_HOUR

        def dueDate_minutes
        try {
            dueDate_minutes = Long.parseLong(params.dueDate_minutes)
        } catch (NumberFormatException e) {
            dueDate_minutes = Long.valueOf(0)
        }
        dueDate += dueDate_minutes * DateUtils.MILLIS_PER_MINUTE

        def expectedDuration = 0
        def expectedDuration_days
        try {
            expectedDuration_days = Long.parseLong(params.expectedDuration_days)
        } catch (NumberFormatException e) {
            expectedDuration_days = Long.valueOf(0)
        }
        expectedDuration += expectedDuration_days * DateUtils.MILLIS_PER_DAY

        def expectedDuration_hours
        try {
            expectedDuration_hours = Long.parseLong(params.expectedDuration_hours)
        } catch (NumberFormatException e) {
            expectedDuration_hours = Long.valueOf(0)
        }
        expectedDuration += expectedDuration_hours * DateUtils.MILLIS_PER_HOUR

        def expectedDuration_minutes
        try {
            expectedDuration_minutes = Long.parseLong(params.expectedDuration_minutes)
        } catch (NumberFormatException e) {
            expectedDuration_minutes = Long.valueOf(0)
        }
        expectedDuration += expectedDuration_minutes * DateUtils.MILLIS_PER_MINUTE

        if (params.type == ConstantUtils.NODE_TYPE_WAIT && params.nodeID) {
            process.variables.each { var ->
                if (request.getParameter("visibility_" + var.name)) {
                    def visibility = Integer.parseInt(request.getParameter("visibility_" + var.name))
                    def varPerNode
                    if (node.id) {
                      varPerNode = node.variables2NodeVisibility?.find() { it.variable == var }
                    }
                    if (!varPerNode) {
                        varPerNode = new Variable2NodeVisibility()
                        var.addToVariable2NodesVisibility(varPerNode)
                        node.addToVariables2NodeVisibility(varPerNode)
                    }

                    varPerNode.visibilityType = visibility
                    varPerNode.visibilityDesc = ConstantUtils.getVisibilityTypes()[visibility]
                }
            }
        } else if (params.type != ConstantUtils.NODE_TYPE_WAIT) {
           if (node.id) { // if it's not new node
             node.variables2NodeVisibility?.each() {
               it.variable?.removeFromVariable2NodesVisibility(it)
             }
             node.variables2NodeVisibility?.clear()
             node.assignees.each() {
               node.processDef.removeFromAssignees(it)
             }
             node.assignees*.delete()
           }
        }

        node.nodeID = params.nodeID
        node.type = params.type
        node.dueDate = dueDate
        node.expectedDuration = expectedDuration
        node.externalUrl = params.externalUrl
        node.protocolGroup = params.protocolGroup

        switch (params.manualForm) {
            case '1':
                node.editorType = ConstantUtils.EDITOR_AUTO
                break
            case ['2','3']:
                node.editorType = ConstantUtils.EDITOR_MANUAL
                break
            default: break
        }

        if (!node.save(flush: true)){
          node.errors.each() {
            log.error(it)
          }
        }

        // updating node position
        nodePosition.processDef = process
        nodePosition.nodeID = node.nodeID
        nodePosition.actionType = node.type
        nodePosition.dueDate = dueDate
        nodePosition.save()

        // updating parameters
        params.id = process.id
        params.formTextArea = null
        params.controllerTextArea = null
        params.pageTextArea = null
        redirect(controller: "processDef", action: "editProcess", params: [id: process.id])
    }

    def deleteNodeDef = {
        def node = ProcessNodeDef.get(Long.valueOf(params.id))
        def process = node.processDef

        def viewsPath = getViewsPath(grailsApplication)
        def controllersPath = getControllersPath(grailsApplication)
        def controllerFile = new File(controllersPath + "/"
                                      + process.processID + "_"
                                      + node.nodeID + "Controller.groovy")
        def manualFormFile = new File(viewsPath + "/" + process.processID
                                      + "/_" + node.nodeID + ".gsp")
        if (manualFormFile.exists()) manualFormFile.delete()
        if (controllerFile.exists()) controllerFile.delete()

        viewsPath = viewsPath.substring(0, viewsPath.length() - 12)
        String processFolder = NameUtils.downCase(process.processID)
        def multiPages = new File(viewsPath + "/" + processFolder + "_" + node.nodeID)
        if (multiPages.exists()) {
            multiPages.listFiles().each() {
                it.delete()
            }
            multiPages.delete()
        }

        def processID = process.id
        def nodeID = node.nodeID
        node.removeFromAssociations()
        node.delete(flush:true)

        flash.message = "Process Node '${nodeID}' was deleted."
        redirect(controller: "processDef", action: 'editProcess', params: [id: processID])
    }

    def showProcessEditor = {
        redirect(controller: "processDef", action: 'editProcess', params: [id: params.id])
    }

    def showPageRules = {}

    def editNodeAction = {
        redirect(controller: "processActionDef", action: "showEditor",
                 params: [id: params.id, ndID: params.ndID])
    }

    private Map getVarVisibilityFromParams(def params) {
      def varVisibility = [:]
      def visibilityParams = params ? params.findAll() {key, value -> key.startsWith("visibility_")} : [:]
      visibilityParams.each() { key, value ->
        def name = StringUtils.substringAfter(key, "visibility_")
        varVisibility.put(name, Integer.valueOf(value))
      }
      return varVisibility
    }

    def previewGeneratedForm = {
        def nodeDef
        if (params.ndID) {
          nodeDef = ProcessNodeDef.get(Long.valueOf(params.ndID))
        } else {
          def process = ProcessDef.get(Long.valueOf(params.id))
          nodeDef = new ProcessNodeDef(processDef: process, nodeID: params.nodeID)
        }
        def varVisibility = getVarVisibilityFromParams(params)
        render(view: "nodePreview", model: [ nodeDetails: new NodeDetails(nodeDef, varVisibility)] )
    }

    private String getViewsPath(def app) {
        def viewsPath
        String applicationPath = app.parentContext.servletContext.getRealPath("");
        if (grailsApplication.isWarDeployed()) {
            viewsPath = new File(applicationPath).getAbsolutePath() + "/WEB-INF"
        } else {
            viewsPath = new File(applicationPath, "..").getAbsolutePath()
        }
        viewsPath += "/grails-app/views/manualForms"
        return viewsPath
    }

    private String getControllersPath(def app) {
        def viewsPath
        String applicationPath = app.parentContext.servletContext.getRealPath("");
        if (grailsApplication.isWarDeployed()) {
            viewsPath = new File(applicationPath).getAbsolutePath() + "/WEB-INF"
        } else {
            viewsPath = new File(applicationPath, "..").getAbsolutePath()
        }
        viewsPath += "/grails-app/controllers"
        return viewsPath
    }

}

