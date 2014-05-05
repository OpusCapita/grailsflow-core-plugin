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

import com.jcatalog.grailsflow.utils.JSONConverter
import com.jcatalog.grailsflow.utils.ConstantUtils
import com.jcatalog.grailsflow.utils.AuthoritiesUtils

import org.apache.commons.lang.StringUtils

import grails.util.GrailsUtil

import com.jcatalog.grailsflow.model.process.BasicProcess
import com.jcatalog.grailsflow.model.process.FlowStatus
import com.jcatalog.grailsflow.model.process.ProcessNode
import com.jcatalog.grailsflow.model.process.ProcessVariable

import com.jcatalog.grailsflow.bean.ProcessDetails
import com.jcatalog.grailsflow.bean.NodeDetails

import com.jcatalog.grailsflow.utils.TranslationUtils

import com.jcatalog.grailsflow.model.graphics.ProcessNodePosition

import org.springframework.web.servlet.support.RequestContextUtils as RCU
import com.jcatalog.grailsflow.status.NodeStatusEnum

import com.jcatalog.grailsflow.process.ProcessSearchParameters

/**
 * Process controller class is used for displaying process-related views.
 * It is also contains logic for worklist representation.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class ProcessController extends GrailsFlowSecureController {
    private static final String RESOURCE_BUNDLE = "grailsflow.processDetails"
    private static final String WORKLIST_BUNDLE = "grailsflow.worklist"

    static final String GRAPHIC_NODE_VISITED = "visited"
    static final String GRAPHIC_NODE_ACTIVE = "active"
    static final String GRAPHIC_NODE_UNTOUGHED = "untouched"

    // this is a maximum value for processes size in SQL 'in' clause
    private static final int MAX_PROCESSES_SIZE = 500

    def processManagerService
    def processWorklistService
    def grailsflowMessageBundleService
    def datePatterns
    // Export service provided by Export plugin
    def exportService
    def additionalWorklistColumns
    def maxResultSize
    def maxRestrictedProcesses
    def isWorklistFilterAvailable

    def index = {
        forward(action: "list", params: params)
    }

  /**
    * ProcessList UI
    */
    def list = {
        return [processDetailsList: [],  // show empty list by default
               processClasses: processManagerService.supportedProcessClasses, params: params]
    }

    /**
     * Shows worklist for current user
     *
     * parameters:
     *   - paging:
     *     -- sort
     *     -- order
     *     -- max
     *     -- offset
     *   - varsFilter.<varName>
     *   - filterVariable
     *   - filterVariableValue
     *   - isEmbedded (TODO: may be remove it)
     *
     */
    def showWorklist = {
        def startOperationTime = System.currentTimeMillis()
        // get paging parameters
        def pagingParameters = getPagingParameters(params)
        if (!pagingParameters.sort ||
            pagingParameters.sort in ["nodeLabel", "processTypeLabel"]) {
            pagingParameters.sort = "startedOn"
        }
        params.max = pagingParameters.max
        String lang = params.lang ? params.lang : RCU.getLocale(request)?.language.toString()

        // get authorities
        def authorities = getUserAuthorities(session)

        // preparing model
        def model = [:]
        def listFilter = [:]
        def worklist
        // prepare filtering if necessary
        if (isWorklistFilterAvailable) {
            // define request variables filter
            def varsFilter = [:]
            params.keySet()?.each() { parameter ->
                if (parameter.startsWith("varsFilter.")
                    && parameter.size() > "varsFilter.".size()) {

                    def procVariable = StringUtils.substringAfter(parameter, "varsFilter.")

                    if (procVariable.endsWith(".list")
                        && parameter.size() > ".list".size()) {
                        // evaluate parameter value to List
                        try {
                            params[parameter] = params[parameter].trim()
                            if (!params[parameter].startsWith("[")) {
                                params[parameter] = "[${params[parameter]}]"
                            }
                            def listValue = new GroovyShell().evaluate(params[parameter])
                            varsFilter[StringUtils.substringBeforeLast(procVariable,".list")] = listValue
                        } catch (Exception e) {
                            log.error("Parameter value cannot be evaluated to List value", e)
                        }
                    } else {
                        varsFilter[procVariable] = params[parameter]
                    }
                }
            }

            listFilter += varsFilter
            if (params.filterVariable && params.filterVariableValue){
                listFilter[params.filterVariable] = params.filterVariableValue
            }

            // get all unfiltered processes
            def availableNodes = processWorklistService
                .getWorklist(authorities, listFilter, pagingParameters.sort, pagingParameters.order, null, null)

            model.varsFilter = varsFilter
            model.filterVariable = params.filterVariable
            model.filterVariableValue = params.filterVariableValue

            // get variables values
            def variableValues = [:]  // map of columnName -> List of available values
            def processVariableValues = [:] // map of columnName -> Map of processID -> process variable
            if (additionalWorklistColumns) {
                def processes = availableNodes.collect(){it.process}.unique()
                if (maxRestrictedProcesses >= processes.size()) {
                          // if we have more then 500 processes in worklist we get variables
                          // by splitting the parameter list into smaller
                          // chunks and then combining the results
                          def pages = Math.round(Math.ceil(processes.size()/MAX_PROCESSES_SIZE))
                          additionalWorklistColumns.each(){ name ->
                              def columnValues = [:]
                              if (availableNodes) {
                                  def variables = []
                                  (0..<pages).each { page->
                                      def useProcesses
                                      if (page*MAX_PROCESSES_SIZE+MAX_PROCESSES_SIZE > processes.size() ) {
                                          useProcesses = processes[page*MAX_PROCESSES_SIZE..-1]
                                      } else {
                                          useProcesses = processes[page*MAX_PROCESSES_SIZE..(page*MAX_PROCESSES_SIZE+MAX_PROCESSES_SIZE-1)]
                                      }
                                      variables.addAll(getVariablesFromProcesses(name, useProcesses))
                                  }
                                  variables.each() { var ->
                                      columnValues.put(var.process.id, var)
                                  }
                                  variableValues.put(name, variables.findAll(){
                                      if (it.value && it.value.trim().size() > 0) {
                                          def item = [:]
                                          item.value = it.value
                                          item.variableValue = it.variableValue
                                          return item
                                      }
                                  }.unique(){
                                      "${it.value}".toString()
                                  }.sort() { "${it.value}".toString() });
                              } else {
                                  variableValues.put(name, []);
                              }
                              processVariableValues.put(name, columnValues)
                          }

                } else {
                    log.info("""There are too many processes '${processes.size()}' for process variables search.
                        Please configure 'maxRestrictedProcesses' value (current value is ${maxRestrictedProcesses}).""")
                    flash.message = grailsflowMessageBundleService.getMessage(WORKLIST_BUNDLE,
                        'grailsflow.message.processes.size.large', [processes.size().toString(), maxRestrictedProcesses?.toString()])
                }
            }
            worklist = applyPagingParameters(availableNodes, pagingParameters)
            // get current page items
            model.variableValues = variableValues
            model.processVariableValues = processVariableValues
        } else {
            // get current page items
            worklist = processWorklistService.getWorklist(authorities, listFilter,
                pagingParameters.sort, pagingParameters.order, pagingParameters.max, pagingParameters.offset)
        }
        model.itemsTotal = processWorklistService.getWorklistSize(authorities, listFilter) ?:0

        // Init additional columns
        def additionalColumns = [:]
        additionalWorklistColumns?.each() { name ->
            additionalColumns.put(name, [:])
        }

        // Build nodes Details
        def processNodes = []
        worklist?.each { processNode ->
            def process = processNode.process
            def processInstance = processManagerService.getRunningProcessInstance(process.id)
            if (processInstance){
                def nodeDetails = new NodeDetails(processNode, processInstance)
                processNodes << nodeDetails
                // Fill labels for additional columns
                additionalWorklistColumns?.each() { name ->
                    def varDetails = nodeDetails.variables?.get(name)
                    if (varDetails?.label) {
                        additionalColumns[name] += varDetails.label
                    }
                }
            }
        }
        if (params.sort == "nodeLabel") {
            processNodes.sort{a, b ->
                String labelA = a.label ?
                    TranslationUtils.getTranslatedValue(a.label,a.nodeID, lang) : a.nodeID
                String labelB = b.label ?
                    TranslationUtils.getTranslatedValue(b.label, b.nodeID, lang) : b.nodeID
                return (params.order == "asc") ?
                    labelA.compareTo(labelB) : -labelA.compareTo(labelB)
            }

        } else if (params.sort == "processTypeLabel") {
            processNodes.sort{a, b ->
                String labelA = a.process.label ?
                    TranslationUtils.getTranslatedValue(a.process.label,a.process.type, lang) : a.process.type
                String labelB = b.process.label ?
                    TranslationUtils.getTranslatedValue(b.process.label, b.process.type, lang) : b.process.type
                return (params.order == "asc") ?
                    labelA.compareTo(labelB) : -labelA.compareTo(labelB)
            }
        }

        model.processNodeList = processNodes
        model.additionalColumns = additionalColumns ?: null
        model.isFilterAvailable = isWorklistFilterAvailable

        model.executionTime = System.currentTimeMillis()- startOperationTime
        log.info("Show worklist: execution time is ${model.executionTime} msec.")
        render(view: 'showWorklist',model: model, params: params)
    }


    /**
     * Shows list of available process classes
     *
     * parameters:
     *   - paging:
     *     -- sort
     *     -- order
     */
    def showTypes = {
        flash.message = ''

        if (!params.sort) params.sort = "type"
        if (!params.order) params.order = "asc"
        def lang = request.locale.language.toString()
        def authorities = getUserAuthorities(session)
        def processClasses = processManagerService.getSupportedProcessClasses().
            findAll { processClass ->
                def processAssignees = processClass.processAssignees.collect() { it.assigneeID.trim() }
                processAssignees.isEmpty() || !processAssignees.intersect(authorities).isEmpty()
            }.
            sort{a, b ->
                def labelA = TranslationUtils.getTranslatedValue(a.label, a.processType, lang)
                def labelB = TranslationUtils.getTranslatedValue(b.label, b.processType, lang)
                return (params.order == "asc") ?
                 labelA.compareTo(labelB) :
                 -labelA.compareTo(labelB) }
        if (processManagerService.errors) {
            flash.message = processManagerService.errors.join("<br/>")
        }
        render(view: 'showTypes', model: [processClasses: processClasses])
    }


   /**
    * search action for processList
    *
    * parameters:
    *   - query:
    *     -- type
    *     -- statusID
    *     -- username
    *     -- startedFrom
    *     -- finishedFrom
    *   - vars.<varName>
    *   - paging:
    *     -- sort
    *     -- order
    *     -- max
    *     -- offset
    */
    def search = {
        // define paging parameters
        if (!params.sort) { params.sort = "createdOn" }
        if (!params.order) { params.order = "desc" }
        if (params.max){
            params.max = params.max.toInteger()
        } else {
            params.max = maxResultSize
        }
        if (params.offset) {
            params.offset = params.offset.toInteger()
        }

        ProcessSearchParameters searchParameters = new ProcessSearchParameters()
        searchParameters.sortBy = params.sort
        searchParameters.maxResult = params.max
        searchParameters.offset = params.offset
        searchParameters.ascending = params.order

        flash.errors = []
        Date startedFromDate = getStartOfParsedDate(params.startedFrom)
        Date startedToDate = getEndOfParsedDate(params.startedTo)
        Date finishedFromDate = getStartOfParsedDate(params.finishedFrom)
        Date finishedToDate = getEndOfParsedDate(params.finishedTo)
        Date modifiedFromDate = getStartOfParsedDate(params.modifiedFrom)
        Date modifiedToDate = getEndOfParsedDate(params.modifiedTo)

        if ((startedToDate && startedFromDate?.after(startedToDate))
            || (finishedToDate && finishedFromDate?.after(finishedToDate))
            || (finishedToDate && startedFromDate?.after(finishedToDate))
            || (finishedToDate && modifiedFromDate?.after(finishedToDate))) {
            flash.errors = [grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.message.dateRanges.invalid")]
            return forward(controller: "process", action: "list", params: params)
        }

        searchParameters.startedBy = params.createdBy
        searchParameters.startedFrom = startedFromDate
        searchParameters.startedTo = startedToDate
        searchParameters.finishedFrom = finishedFromDate
        searchParameters.finishedTo = finishedToDate
        searchParameters.modifiedFrom = modifiedFromDate
        searchParameters.modifiedTo = modifiedToDate
        searchParameters.modifiedBy = params.modifiedBy

        // define request variables filter
        Map varsFilter = [:]
        params.keySet()?.each() { parameter ->
            if (parameter.startsWith("vars.")
                && parameter.size() > "vars.".size()) {

                String procVariable = StringUtils.substringAfter(parameter, "vars.")
                def varValue = params[parameter]

                if (procVariable.endsWith(".list")
                    && procVariable.size() > ".list".size()) {
                    // evaluate parameter value to List
                    try {
                        params[parameter] = params[parameter].trim()
                        if (!params[parameter].startsWith("[")) {
                            params[parameter] = "[${params[parameter]}]"
                        }
                        varValue = new GroovyShell().evaluate(params[parameter])
                        procVariable = StringUtils.substringBeforeLast(procVariable,".list")
                    } catch (Exception e) {
                        log.error(" Parameter value cannot be evaluated to List value", e)
                        varValue = null
                    }
                }
                if (varValue) {
                  varsFilter[procVariable] = varValue
                }
            }
        }
        searchParameters.variablesFilter = varsFilter

        Collection processClasses = processManagerService.supportedProcessClasses
        String type = params.type ?: processClasses*.processType?.join(",")
        searchParameters.type = type

        if (params.statusID) {
            params.statusID = Arrays.asList(request.getParameterValues("statusID"))
            searchParameters.statusID = params.statusID?.join(",")
        }

        try {
            searchParameters.processID = params.processID ? Long.valueOf(params.processID): null
        } catch(NumberFormatException ex) {
            log.error("Impossible to parse ${params.processID} as Long value. ",ex)
            flash.errors = [grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processID.invalid")]
            return forward(controller: "process", action: "list", params: params)
        }

        // get size of list
        Integer itemsTotal = processWorklistService
            .getProcessListSize(searchParameters)

        // get current page items
        Collection processList = processWorklistService
            .getProcessList(searchParameters)


        Collection processDetailsList = []
        processList?.each() { basicProcess ->
            def processClass = processClasses?.find() { it.processType == basicProcess.type }
            processDetailsList << new ProcessDetails(basicProcess, processClass)
        }

        render(view: 'list', params: params,
               model: [processDetailsList: processDetailsList,
                       itemsTotal: itemsTotal ? itemsTotal : 0,
                       processClasses: processClasses])
    }

    def exportProcess = {
        if (!params.format) {
            flash.errors = [grailsflowMessageBundleService
                              .getMessage(RESOURCE_BUNDLE, "grailsflow.message.emptyFormat")]
            return forward(controller: "process", action: "search", params: params)
        } else if (params.format != "html") {

            if (!params.processID) {
                flash.errors = [grailsflowMessageBundleService
                                  .getMessage(RESOURCE_BUNDLE, "grailsflow.message.emptyProcessId")]
                return forward(controller: "process", action: "search", params: params)
            }

            def process = BasicProcess.get(Long.valueOf(params.processID))
            if (!process) {
                flash.errors = [grailsflowMessageBundleService
                                  .getMessage(RESOURCE_BUNDLE, "grailsflow.message.errorProcessId", [params.processID])]
                return forward(controller: "process", action: "search", params: params)
            }

            def filename = grailsflowMessageBundleService.getMessage(RESOURCE_BUNDLE, "grailsflow.label.processExport")+":${process.id}"
            def extension = params.extension ? params.extension : 'txt'
            response.contentType = process.domainClass.grailsApplication
                .config.grails.mime.types[params.format]
            response.setHeader("Content-disposition", "attachment; filename=${filename}.${extension}")

            def fields = ["id", "type", "status", "createdOn", "finishedOn", "variables"]
            def labels = ["id": grailsflowMessageBundleService
                                 .getMessage(RESOURCE_BUNDLE, "grailsflow.label.processID"),
                          "type": grailsflowMessageBundleService
                                   .getMessage(RESOURCE_BUNDLE, "grailsflow.label.type"),
                          "status": grailsflowMessageBundleService
                                     .getMessage(RESOURCE_BUNDLE, "grailsflow.label.status"),
                          "createdOn": grailsflowMessageBundleService
                                        .getMessage(RESOURCE_BUNDLE, "grailsflow.label.createdOnBy"),
                          "finishedOn": grailsflowMessageBundleService
                                         .getMessage(RESOURCE_BUNDLE, "grailsflow.label.finishedOnBy"),
                          "variables": grailsflowMessageBundleService
                                        .getMessage(WORKLIST_BUNDLE, "grailsflow.label.processVars")]
            def variablesFormatter = { domain, variables  ->
                def varsFormatted = []
                variables?.each() {
                   varsFormatted << "${it.name}=${it.value}"
                }
                return varsFormatted.join(",")
            }
            def statusFormatter = { domain, value ->
                return value?.statusID
            }
            def startTimeFormatter = { domain, value ->
                if (domain?.createdOn)
                  return "${new java.text.SimpleDateFormat("dd MM yyyy").format(domain.createdOn)}/${domain?.createdBy}"
            }
            def finishTimeFormatter = { domain, value ->
                if (domain?.finishedOn)
                  return "${new java.text.SimpleDateFormat("dd MM yyyy").format(domain.finishedOn)}/${domain?.finishedBy}"
            }

            def formatters = ["status": statusFormatter,
                              "createdOn": startTimeFormatter,
                              "finishedOn": finishTimeFormatter,
                              "variables": variablesFormatter]

            exportService.export(params.format, response.outputStream, [process], fields, labels, formatters, [:])
        }
    }


    def startProcess = {
      GrailsUtil.deprecated("/process/startProcess in deprecated. Use 'processManagement' controller and 'startProcess' action instead.")
      forward(controller: "processManagement", action: "startProcess", params: params)
    }

    def showStartDetails = {
      if (!flash.errors) flash.errors = []
      if (!flash.warnings) flash.warnings = []

      def processClass = processManagerService.getProcessClass(params.id)
      if (!processClass) {
          flash.errors << grailsflowMessageBundleService
                              .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processScript.invalid", [params.id])
          forward(action: "showTypes", params: params)
      }
      if (processClass.startNode.type == ConstantUtils.NODE_TYPE_WAIT) {
          def processType = processClass.processType
          def processInstance = processManagerService.getNewProcessInstance(processType)
          def startNodeDef = processClass.startNode
          def startNode = new ProcessNode(startNodeDef)
          renderNodeDetails(startNode, processInstance)
      } else {
          flash.errors << "First node of process ${params.id} is automatic."
          forward(action: "showTypes", params: params)
      }
    }

  /**
    * ProcessDetails UI
    *
    * params:
    *   - id - process ID
    *   - processID -- if id is null, processID is tried
    */
    def showProcessDetails = {
        if (params.id == null) {
          params.id = params.processID
        }
        if (params.id != null) {
            def process = BasicProcess.get(Long.valueOf(params.id))
            def processClass = processManagerService.getProcessClass(process?.type)
            if (!processClass) {
                flash.message = grailsflowMessageBundleService
                    .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processScript.notExisted", [process?.type])
                return forward(action: 'showTypes', params: params )
            }
            def processDetails = new ProcessDetails(process, processClass)

            render(view: 'processDetails',
                   model: [processDetails: processDetails, params: params])
        } else {
            flash.message = grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.message.process.notStarted", [params?.toString()])
            forward(action: 'showTypes', params: params )
        }
    }

   /**
    * Evaluate Expression into external URL and open external editor.
    *
    * TODO
    */
    def openExternalUrl = {
       def node = ProcessNode.get(params.id)
       if (!node) {
           flash.message = grailsflowMessageBundleService
               .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processNode.notExisted", [params.id])
           return redirect(action: 'showTypes', params: [sort: params.sort, order: params.order] )
       }

       def basic = node.process
       def process = processManagerService.getRunningProcessInstance(basic.id)
       def processClass = process.class
       def nodeDef = processClass.nodes[node.nodeID]
       def externalUrl = process.evaluateExpression(nodeDef?.externalUrl)

       // organize POST to the specified Url
       if(externalUrl){
          def variables = ProcessVariable.findAllWhere(process: basic)
          render getPreparedPOSTForm(externalUrl, variables)
       } else {
           def errorMessage = grailsflowMessageBundleService
              .getMessage(RESOURCE_BUNDLE, "grailsflow.message.externalUrl.invalid", nodeDef?.externalUrl)
           log.error(errorMessage)
           render errorMessage
       }
     }




  /**
    * NodeDetails UI
    *
    * params:
    *   - id -- Node ID
    */
    def showNodeDetails = {
        def node = ProcessNode.get(params.id)
        if (!node) {
            flash.message = grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processNode.notExisted", [params.id])
            return forward(action: 'showTypes', params: params )
        }
        def basic = node.process
        def processInstance = processManagerService.getRunningProcessInstance(basic.id)
        if (!processInstance) {
            flash.message = grailsflowMessageBundleService
                                .getMessage(RESOURCE_BUNDLE, "grailsflow.message.processScript.notExisted", [basic.type])
            return forward(action: 'showTypes', params: params )
        }
        renderNodeDetails(node, processInstance)
    }

    /**
     * Action that returns back to previous view
     */
    def returnBack = {
        if (!params.backPage) {
            flash.errors = [ grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.message.backpage.error") ]
            return forward(action: 'showTypes', params: params)
        }

        redirect(action: params.backPage)
    }


   /**
    * Helper method for rendering NodeDetails page
    */
    private def renderNodeDetails(ProcessNode node, def processInstance) {
      def nodeDetails = new NodeDetails(node, processInstance)
      def processClass = processInstance.class
      def nodeDef = processClass.nodes[node.nodeID]
      def templatePath
      def templateNotFoundMessage
      if (ConstantUtils.EDITOR_MANUAL == nodeDef?.editorType) {
          templatePath = "/manualForms/" + processClass.processType + "/" + node.nodeID
          templateNotFoundMessage = grailsflowMessageBundleService
              .getMessage(RESOURCE_BUNDLE, "grailsflow.message.manualForm.invalid", [node.nodeID])
      } else {
        templatePath = "/manualForms/automaticForm"
      }
      render(view: 'nodeDetails', model: [template: templatePath,
              templateNotFoundMessage: templateNotFoundMessage,
              nodeDetails: nodeDetails, params: params])

    }

    def sendEvent = {
      GrailsUtil.deprecated("/process/sendEvent in deprecated. Use 'processManagement' controller and 'sendEvent' action instead.")
      forward(controller: "processManagement", action: "sendEvent", params: params)
    }

    def extendedSendEvent = {
      GrailsUtil.deprecated("/process/extendedSendEvent in deprecated. Use 'extendedProcessManagement' controller and 'sendEvent' action instead.")
      forward(controller: "extendedProcessManagement", action: "sendEvent", params: params)
    }

    def extendedStartProcess = {
      GrailsUtil.deprecated("/process/extendedStartProcess in deprecated. Use 'extendedProcessManagement' controller and 'startProcess' action instead.")
      forward(controller: "extendedProcessManagement", action: "startProcess", params: params)
    }


    def forwardEvent = {
      GrailsUtil.deprecated("/process/forwardEvent in deprecated. Use 'processManagement' controller and 'forwardEvent' action instead.")
      forward(controller: "processManagement", action: "forwardEvent", params: params)
    }

    def killProcess = {
      GrailsUtil.deprecated("/process/killProcess in deprecated. Use 'processManagement' controller and 'killProcess' action instead.")
      forward(controller: "processManagement", action: "killProcess", params: params)
    }

    def showGraphic = {
        def process = BasicProcess.get(Long.valueOf(params.processID))
        def trList = []
        def nodeInfos = []
        def isPositionsHandled = Boolean.TRUE

        def processClass = processManagerService.getProcessClass(process.type)
        processClass?.nodes?.values()?.each() { nodeDef ->
            def nPos = ProcessNodePosition.findWhere("process": process, "nodeID": nodeDef.nodeID)
            if (!nPos) {
                nPos = new ProcessNodePosition(nodeID: nodeDef.nodeID, actionType: nodeDef.type)
                isPositionsHandled = Boolean.FALSE
            } else {
                if (!nPos.startX) isPositionsHandled = Boolean.FALSE
            }

            def node = ProcessNode.findByProcessAndNodeID(process, nodeDef.nodeID)
            if (node) {
                if ([NodeStatusEnum.ACTIVATED.value(), NodeStatusEnum.SUSPENDED.value(),
                      NodeStatusEnum.FORWARDED.value(), NodeStatusEnum.PENDING.value(),
                      NodeStatusEnum.RUNNING.value(), NodeStatusEnum.AWAIT_CALLBACK.value()]
                    .contains(node.status.statusID)) {
                    nPos.statusType = GRAPHIC_NODE_ACTIVE
                } else {
                    nPos.statusType = GRAPHIC_NODE_VISITED
                }
            } else {
                nPos.statusType = GRAPHIC_NODE_UNTOUGHED
            }
            Map position = nPos.properties["nodeID", "actionType", "knotType", "statusType", "startX", "startY", "width", "height"]
            position.knotTypeLabel = position.knotType ? grailsflowMessageBundleService
                    .getMessage(RESOURCE_BUNDLE, "grailsflow.label.graphic.node.${position.knotType}") : ''
            position.statusTypeLabel = grailsflowMessageBundleService
                    .getMessage(RESOURCE_BUNDLE, "grailsflow.label.graphic.node.${position.statusType}")
            position.nodeLabel = gf.translatedValue(translations: nodeDef.label, default: nodeDef.nodeID)?.toString()
            nodeInfos << position

            nodeDef.transitions.each() { transition ->
		        transition.toNodes.each() {
                    trList << [ fromNodeID:  nodeDef.nodeID,
		                onEventID:  transition.event,
                        eventLabel: gf.translatedValue(translations: transition.label, default: transition.event)?.toString(),
		                toNodeIDs:  it.nodeID]
                }
            }
        }

        def transitionsJson = JSONConverter.toJSON(trList)
        def positions = JSONConverter.toJSON(nodeInfos)

        [transitionsJson: transitionsJson, processID: process.id,
         isPositionsHandled: isPositionsHandled, positions: positions]
    }

    def saveNodesPositions = {
        if (!params.id) {
           log.error("The identifier for getting process entry is null.")
           render ""
        }

        def process = BasicProcess.get(Long.valueOf(params.id))
        def nodesInfo = params.positions.split(";")

        nodesInfo.each() {
            def nodeInf = it.split(",")
            def nodePosition = ProcessNodePosition.findWhere("process": process,
                    "nodeID": nodeInf[0])
            nodePosition.startX = Integer.valueOf(nodeInf[1])
            nodePosition.startY = Integer.valueOf(nodeInf[2])
            nodePosition.width = Integer.valueOf(nodeInf[3])
            nodePosition.height = Integer.valueOf(nodeInf[4])
            nodePosition.save()
        }
        render ""
    }

    private List getVariablesFromProcesses(String varName, List processes) {
        return ProcessVariable.executeQuery("""select pv from com.jcatalog.grailsflow.model.process.ProcessVariable pv
            where pv.name = :name and pv.process in (:processes)""",
            ['name': varName, 'processes': processes])
    }

    private String getPreparedPOSTForm(String action, List variables) {
        def formParams = new StringBuffer()
        variables?.each() { var ->
            formParams << "<input type='hidden' name='${var.name}' value='${var.variableValue}'/> \n "
        }
        return """<form name="subForm" method="POST" action="${action}"> \n
                  ${formParams.toString()} \n
                  <input type="submit" style="display: none;"/>  \n
                  </form> \n
                 <script>document.subForm.submit();</script>"""
    }

    private Date getStartOfParsedDate(String dateString) {
        return GrailsflowUtils.getStartOfDate(GrailsflowUtils.
                getParsedDate(dateString, gf.datePattern()?.toString()))
    }

    private Date getEndOfParsedDate(String dateString) {
        return GrailsflowUtils.getEndOfDate(GrailsflowUtils.
                getParsedDate(dateString, gf.datePattern()?.toString()))
    }

    private Map<String, Object> getPagingParameters(def params) {
        def pagingParameters = [:]
        pagingParameters.sort = params.sort
        pagingParameters.order = params.order ?: 'desc'
        if (params.max){
            pagingParameters.max = params.max.toInteger()
        } else {
            pagingParameters.max = maxResultSize
        }
        if (params.offset) {
            pagingParameters.offset = params.offset.toInteger()
       } else {
            pagingParameters.offset = 0
       }
       return pagingParameters;
    }

    private List applyPagingParameters(List nodes, Map<String, Object> parameters) {
        if (!nodes) return null
        if (!parameters) return nodes
        if (parameters.max && nodes.size() < parameters.max) return nodes

        if (nodes.size() < parameters.offset) return nodes
        if (nodes.size() < parameters.offset + parameters.max) {
            return nodes[(parameters.offset)..-1]
        }
        return nodes[parameters.offset..<(parameters.offset + parameters.max)]
    }

}
