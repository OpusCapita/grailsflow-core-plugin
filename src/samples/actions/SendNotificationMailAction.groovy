package mail

import com.jcatalog.grailsflow.actions.Action

import com.jcatalog.grailsflow.model.process.BasicProcess
import com.jcatalog.grailsflow.model.process.ProcessVariable
import org.apache.commons.logging.LogFactory

class SendNotificationMailAction extends Action {
    // mail parameters
    public String mailFrom
    public String mailTo
    public String subject

    // message
    public String message
    public String messageTemplate

    // grailsflow parameters
    public String nodeID
    public String events
    public String variables

    protected static def log = LogFactory.getLog(SendNotificationMailAction.class)

    def execute() {
        log.info("Sending notification mail")
        log.info("From: $mailFrom")
        log.info("To: $mailTo")
        log.info("Subject: $subject")

        def mailService = getObjectByName("mailService")
        def processManagerService = getObjectByName("processManagerService")
        BasicProcess basicProcess = BasicProcess.get(actionContext.processID)

        if (mailService && processManagerService && basicProcess) {
            def workflowManagementMail = basicProcess.domainClass
                .grailsApplication.config?.grailsflow?.events?.mail?.address
            String processType = basicProcess.type
            def processClass = processManagerService.getProcessClass(processType)

            def eventsList = events ? events.split(',') : getNodeEvents(processClass, nodeID)

            def eventMessageSubject = "Process=${actionContext.processID}-${nodeID}"

            def variablesMap = variables ? getFilteredNodeVariables(processClass, basicProcess, nodeID, variables.split(',')) : getNodeVariables(processClass, basicProcess, nodeID)

            if (!messageTemplate) {
                def eventMessageBody = "$message <br/>"
                eventsList.each(){ event ->
                    eventMessageBody += "<br/><a href='mailto:${workflowManagementMail}?subject=${eventMessageSubject}&body=${event}'>${event}</href>"
                }
                mailService.sendMail {
                    from "$mailFrom"
                    to "$mailTo"
                    subject "$subject"
                    html "$eventMessageBody"
                }
            } else {
                def messageContext = [workflowManagementMail: workflowManagementMail,
                    processID: actionContext.processID,
                    nodeID: actionContext.nodeID,
                    user: actionContext.user,
                    notificationNodeID: nodeID,
                    events: eventsList,
                    variables: variablesMap]
                mailService.sendMail {
                    from "$mailFrom"
                    to "$mailTo"
                    subject "$subject"
                    body( view: messageTemplate, model: messageContext)
                }
            }
        } else {
            log.error("Unable to send email due to configuration: mailService = $mailService, processManagerService = $processManagerService for process = $basicProcess")
        }
        return "okay"
    }
    
    def getNodeEvents(def processClass, def nodeID){
        def events = processClass.nodes[nodeID].transitions?.collect() { it.event }
        return events ? events : []
    }
    
    def getNodeVariables(def processClass, def basicProcess, def nodeID){
        def variables = [:] 
        processClass.variables.each() { var ->
            if (processClass.isWritable(var.name, nodeID)) {
                variables[var.name] = ProcessVariable.findWhere('process': basicProcess, 'name': var.name)?.variableValue
            }
        }
        return variables
    }

    def getFilteredNodeVariables(def processClass, def basicProcess, def nodeID, def variablesNamesList) {
        def variables = [:] 
        variablesNamesList.each() { name ->
            if (processClass.isWritable(name, nodeID)) {
                variables[name] = ProcessVariable.findWhere('process': basicProcess, 'name': name)?.variableValue
            }
        }
        return variables
    }

}