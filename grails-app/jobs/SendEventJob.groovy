import com.jcatalog.grailsflow.model.process.ProcessNode
import com.jcatalog.grailsflow.model.process.BasicProcess
/**
 * Send event job executes event sending.
 * This job is not executed by schedule but is invoked manually.
 *
 * @author Maria Voitovich
 */
class SendEventJob {
    static triggers = {
    }
    def group = "PROCESS"
    def concurrent = true
    def volatility = false

    def processManagerService
    def grailsflowLockService

    def execute(context) {

        def dataMap = context.mergedJobDataMap

        if (!processManagerService) {
            log.warn("processManagerService is not configured.")
            return
        } else {
            def processID = dataMap.processID
            def nodeID = dataMap.nodeID
            def caller = dataMap.user
            def event = dataMap.event
            def variables = dataMap.variables

            ProcessNode node = ProcessNode.findByProcessAndNodeID(BasicProcess.get(dataMap.processID), dataMap.nodeID)
            if (grailsflowLockService.lockProcessExecution(node) ) {
                log.debug("Invoking asynchronous node '${nodeID}' of process #${processID}.${event ? ' Sending event '+event :''}")
                def result = processManagerService.invokeNodeExecution(processID, nodeID, event, caller, variables)
                if (result != 0) {
                  log.warn("Node ${nodeID} of process #${processID} failed with code ${result}")
                } else {
                  log.debug("Node ${nodeID} of process #${processID} is done")
                }
                grailsflowLockService.unlockProcessExecution(node)
            }

        }
    }

}