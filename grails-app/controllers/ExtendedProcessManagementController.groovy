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
import com.jcatalog.grailsflow.model.process.BasicProcess

import javax.servlet.http.HttpServletResponse
import java.io.PrintWriter
import com.jcatalog.grailsflow.engine.execution.ExecutionResultEnum

/**
 * ExtendedProcess controller.
 *
 * @author Maria Voitovich
 */
class ExtendedProcessManagementController extends GrailsFlowSecureController {

    def processManagerService
    def scriptsProvider
    def callbacksPath


    /**
     * Calls script from workarea specified by params.id and passes request and params there.
     * Script should parse request and params to get parameters for sending event.
     * Controller sends event to process node based on results of the script.
     *
     */
    def sendEvent = {
      def callbackId = params.id

      if (!callbackId) {
        writeErrorResponse(response, "Script name (id parameter) must be specified")
        return
      }

      def callbacksRoot = scriptsProvider.getResourceFile(callbacksPath)
      File callbackFile = new File(callbacksRoot, "${callbackId}.groovy")

      if (!callbackFile.exists()){
        writeErrorResponse(response, "${callbackId} callback script doesn't exist")
        return
      }

      def context = [:]
      context.params = params
      context.request = request

      // Default values
      def result = [statusCode: 200, message: "Event invoked", requester : securityHelper.getUser(session)]
      context.result = result

      GroovyShell shell = new GroovyShell(new Binding(context))

      try {
        // get Process params from request
        shell.evaluate(callbackFile)
      } catch (Exception e){
        writeErrorResponse(response, e.message, e)
        return
      }

      def processKey = result.processKey
      def nodeID = result.nodeID
      def event = result.event
      def requester = result.requester
      def variables = result.variables

      if (!processKey){
        writeErrorResponse(response, "Callback script does not return processKey")
        return
      }

      if (!nodeID){
        writeErrorResponse(response, "Callback script does not return nodeID")
        return
      }

      if (!event){
        writeErrorResponse(response, "Callback script does not return event")
        return
      }

      // checking process node assignees and user authorities
      def authorities = getUserAuthorities(session)
      def process = BasicProcess.get(processKey)
      if (process) {
          def processClass = processManagerService.getProcessClass(process?.type)
          def nodeAssignees = processClass.nodes[nodeID]?.assignees?.collect() { it.assigneeID.trim() }
          if (nodeAssignees && !nodeAssignees.isEmpty() && nodeAssignees.intersect(authorities).isEmpty()) {
            writeErrorResponse(response, "The requester '${requester}' cannot send event to the node '${nodeID}'")
            return
          }
      }

      // sending event to the process
      def executionResult = processManagerService.sendEvent(processKey, nodeID,
                 event, requester, variables)

      switch (executionResult) {
        case ExecutionResultEnum.EXECUTED_SUCCESSFULLY.value():
          writeResponse(response, result.statusCode ? result.statusCode : HttpServletResponse.SC_OK, result.message ? result.message : "Event invoked")
          break
        case ExecutionResultEnum.NO_PROCESS_FOR_PROCESSID.value():
  	      writeErrorResponse(response, "Process #${processKey} not found")
        case ExecutionResultEnum. NO_NODEID_IN_PROCESS.value():
  	      writeErrorResponse(response, "Process #${processKey} does not have node ${nodeID}")
          break
        case ExecutionResultEnum.NO_TRANSITION_FOR_EVENT.value():
  	      writeErrorResponse(response, "Node ${nodeID} of process #${processKey} does not have transition for event ${event}")
          break
        case ExecutionResultEnum.NO_NODE_OR_NODE_COMPLETED.value():
  	      writeErrorResponse(response, "Node ${nodeID} of process #${processKey} does not wait for user event")
          break
        case ExecutionResultEnum.NO_PROCESS_DEFINITION.value():
          writeErrorResponse(response, "Error while compiling #${processKey} process script")
          break
        default:
          writeErrorResponse(response, "ProcessManagerService has returned unknown code: ${executionResult}")
          break
      }
    }

    /**
     * Calls script from workarea specified by params.id and passes request and params there.
     * Script should parse request and params to get parameters for starting process.
     * Controller starts process based on results of the script.
     *
     */
    def startProcess = {
      def callbackId = params.id

      if (!callbackId) {
        writeErrorResponse(response, "Script name (id parameter) must be specified")
        return
      }

      def callbacksRoot = scriptsProvider.getResourceFile(callbacksPath)
      File callbackFile = new File(callbacksRoot, "${callbackId}.groovy")

      if (!callbackFile.exists()){
        writeErrorResponse(response, "${callbackId} callback script doesn't exist")
        return
      }

      def context = [:]
      context.params = params
      context.request = request

      // Default values
      def result = [statusCode: 200, message: "Process started", requester : securityHelper.getUser(session)]
      context.result = result

      GroovyShell shell = new GroovyShell(new Binding(context))

      try {
        // get Process params from request
        shell.evaluate(callbackFile)
      } catch (Exception e){
        writeErrorResponse(response, e.message, e)
        return
      }

      def processType = result.processType
      def requester = result.requester
      def variables = result.variables

      if (!processType){
        writeErrorResponse(response, "Callback script does not return processType")
        return
      }

      // checking process assignees and user authorities
      def authorities = getUserAuthorities(session)
      def processClass = processManagerService.getProcessClass(processType)
      def processAssignees = processClass.processAssignees.collect() { it.assigneeID.trim() }
      if (!processAssignees.isEmpty() && processAssignees.intersect(authorities).isEmpty()) {
        writeErrorResponse(response, "Process '${processType}' cannot be started by the requester ${requester}")
        return
      }

      // starting process
      def processID = processManagerService.startProcess(processType, requester, variables)

      if (processID != null) {
        writeResponse(response, result.statusCode ? result.statusCode : HttpServletResponse.SC_OK, result.message ? result.message : "Event invoked")
      } else {
        writeErrorResponse(response, "Process startup failed")
      }
    }

    private void writeResponse(def response, def statusCode, def message){
        log.debug(message);
        response.status = statusCode
        def printWriter = new PrintWriter(response.writer)
        printWriter.write(message)
        printWriter.flush()
    }

    private void writeErrorResponse(def response, def message){
        log.error(message);
        writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message)
    }

    private void writeErrorResponse(def response, def message, def error){
        log.error(message, error);
        writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message)
    }

}
