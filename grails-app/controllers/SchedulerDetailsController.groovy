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


import com.jcatalog.grailsflow.model.process.ProcessVariable
import com.jcatalog.grailsflow.utils.TranslationUtils
import grails.converters.JSON
import groovy.util.logging.Slf4j
/**
 * SchedulerDetailsController class is used for monitoring and
 * managing (CRUD) of Quartz jobs.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
@Slf4j
class SchedulerDetailsController extends GrailsFlowSecureController {
    def processManagerService
    def workareaPathProvider
    def documentsPath

    def schedulerOperationsService

    def static allowedMethods = [
            delete: 'DELETE',
            pause: 'POST'
    ]

    def index = {
        redirect(action: "showSchedulerDetails")
    }

    def showSchedulerDetails = {
        def schedulerDetails = getSchedulerDetails()
        if (!schedulerDetails) {
            flash.error = g.message(code: "plugin.grailsflow.messages.error.schedulerDetails")
        }

        [schedulerDetails: schedulerDetails]
    }

    def pauseScheduler = {
        if (!schedulerOperationsService.pauseResumeScheduler()) {
            flash.error = g.message(code: "plugin.grailsflow.messages.error.pauseResume")
        }
        redirect(action: "showSchedulerDetails")
    }

    def pause = {
        try {
            if (!params.name || !params.group) {
                throw new IllegalArgumentException(g.message(code: "plugin.grailsflow.messages.error.pauseResume") as String)
            }

            if (params.isRunning == "true") {
                throw new IllegalStateException(g.message(code: "plugin.grailsflow.message.job.running", args: [params.name, params.group]) as String)
            }

            def shouldBePaused = params.isPaused == "false"
            if (!schedulerOperationsService.pauseResumeJob(params.name, params.group, shouldBePaused)) {
                throw new RuntimeException(g.message(code: "plugin.grailsflow.messages.error.pauseResume") as String)
            }

            render([success: true] as JSON)
        } catch (Exception e) {
            log.error("Error pausing/resuming job: ${params.name}/${params.group}", e)
            response.status = 200
            render([
                success: false,
                message: e.message ?: g.message(code: "plugin.grailsflow.messages.error.pauseResume")
            ] as JSON)
        }
    }

    def edit = {
        if (!params.name || !params.group) {
            flash.error = g.message(code: "plugin.grailsflow.messages.error.pauseResume")
            return forward(action: "showSchedulerDetails", params: params)
        }

        def jobDetails = schedulerOperationsService.getJobDetails(params.name, params.group)
        render(view: "editScheduledJob", model: [jobDetails: jobDetails,
               repeatingInfo: getRepeatingPeriods(), params: params])
    }

    def updateJob = {
        if (!flash.errors) flash.errors = []

        if (!params.name || !params.group) {
            flash.error = g.message(code: "plugin.grailsflow.messages.error.pauseResume")
            return forward(action: "showSchedulerDetails", params: params)
        }

        def startTime_hours = 0
        def startTime_minutes = 0

        if (params.startTime_hours && params.startTime_hours != '00') {
            try {
                startTime_hours = Integer.valueOf(params.startTime_hours)
            } catch (Exception e) {
                flash.errors << g.message(code: "plugin.grailsflow.messages.intValue.invalid", args: ["Start Time hours"])
            }
        }

        if (params.startTime_minutes && params.startTime_minutes != '00') {
            try {
                startTime_minutes = Integer.valueOf(params.startTime_minutes)
            } catch (Exception e) {
                flash.errors << g.message(code: "plugin.grailsflow.messages.intValue.invalid",args:  ["Start Time minutes"])
            }
        }

        def startTime = (startTime_hours*60 + startTime_minutes)*60000
        def startDay = GrailsflowUtils.getParsedDate(params.startDay, gf.datePattern()?.toString())
        if (!startDay) {
            flash.warnings = [g.message(code: "plugin.grailsflow.messages.startDay.invalid", args: [params.startDay])]
            startDay = new Date()
        }

        def startDate = new Date(startDay.time + startTime)

        def calendarTime = Calendar.getInstance()
        calendarTime.time = startDate
        params.startTime_hours = calendarTime.get(Calendar.HOUR_OF_DAY) < 10 ?
            "0${calendarTime.get(Calendar.HOUR_OF_DAY)}" : calendarTime.get(Calendar.HOUR_OF_DAY)
        params.startTime_minutes = calendarTime.get(Calendar.MINUTE) < 10 ?
            "0${calendarTime.get(Calendar.MINUTE)}" : calendarTime.get(Calendar.MINUTE)
        params.startDay = startDay

        try{
            def repeating = (params.repeating && params.repeating != '') ? Integer.valueOf(params.repeating) :
                (params.customRepeating && params.customRepeating != '') ? Integer.valueOf(params.customRepeating) : null
            if (repeating == null || repeating < 0) {
                flash.errors << g.message(code: "plugin.grailsflow.messages.error.repeating")
            } else {
                params.customRepeating = repeating
                def isJobUpdated = schedulerOperationsService
                    .updateScheduledJob(params.name, params.group, startDate, repeating)
                if (isJobUpdated) {
                    flash.message = g.message(code: "plugin.grailsflow.messages.job.updated", args: [params.name])
                } else {
                    flash.errors << g.message(code: "plugin.grailsflow.messages.job.notUpdated", args: [params.name])
                }
            }
        } catch (NumberFormatException nfe) {
            flash.errors << g.message(code: "plugin.grailsflow.messages.error.convertInt", args: [params.customRepeating])
            log.error("Error in converting repeting interval: ${nfe.message}")
        }
        redirect(action: "showSchedulerDetails")
    }

    def delete = {
        boolean success = false
        if (params.name && params.group) {
            if (schedulerOperationsService.deleteScheduledJob(params.name, params.group)) {
                success = true
            }
        }

        render([success: success, message: (success)
                ? g.message(code: "plugin.grailsflow.messages.job.deleted")
                : g.message(code: "plugin.grailsflow.messages.error.delete")] as JSON)
    }

    def scheduleProcess = {
        def authorities = getUserAuthorities(session)
        def lang = request.locale.language.toString()

        def classes = processManagerService.getSupportedProcessClasses().
            findAll { processClass ->
                def processAssignees = processClass.processAssignees.collect() { it.assigneeID.trim() }
                processAssignees.isEmpty() || !processAssignees.intersect(authorities).isEmpty()
            }.
            sort{a, b ->
                def labelA = TranslationUtils.getTranslatedValue(a.label, a.processType, lang)
                def labelB = TranslationUtils.getTranslatedValue(b.label, b.processType, lang)
                return labelA.compareTo(labelB) }
        def processClass = classes ? classes.get(0) : null
        params.repeating = 0
        return [processClasses: classes, processClass: processClass,
                repeatingInfo: getRepeatingPeriods(), bean: params, params: params]
    }

    def updateVariables = {
        def processClass

        if (params.type) {
            processClass = getProcessScriptInfo(params.type)
        }
        render(template: "/schedulerDetails/variablesForm", contextPath: pluginContextPath,
               model: [variables: processClass?.variables])
    }

    def addJob = {
        flash.errors = []
        flash.warnings = []

        def classes = processManagerService.getSupportedProcessClasses()
        def processClass = getProcessScriptInfo(params.processID)
        if (!processClass) {
            flash.error = g.message(code: "plugin.grailsflow.messages.job.scheduled.error", args: [params.processID])
            return render(view: "scheduleProcess", model: [processClasses: classes,
               repeatingInfo: getRepeatingPeriods(), params: params, bean: params])
        }

        def variables = [:]
        processClass.variables?.each() { variable ->
            def value
            def type = ProcessVariable.defineType(variable.type)
            if (request.getParameter("var_" + variable.name)) {
                if (type == ProcessVariable.DATE) {
                    value = GrailsflowUtils.getParsedDate(params["var_" + variable.name], gf.datePattern()?.toString())
                    if (!value) {
                        flash.warnings << g.message(code: "plugin.grailsflow.messages.dateValue.invalid", args: [variable.name])
                    } else {
                        value = value.time.toString()
                    }
                } else if (type == ProcessVariable.BOOLEAN) {
                    value = (request.getParameter("var_" + variable.name) == "on" ? "true" : "false")
                } else if (type == ProcessVariable.LIST) {
                    value = null
                } else {
                    value = request.getParameter('var_' + variable.name)
                }
            } else if (type == ProcessVariable.BOOLEAN && !variable.defaultValue) {
                value = "false"
            } else if (type == ProcessVariable.DOCUMENT){
                File documentsRoot = workareaPathProvider.getResourceFile(documentsPath)
                def document = GrailsflowRequestUtils
                    .getDocumentFromRequest(request, "var_${variable.name}", documentsRoot)
                value = document?.toString()
            } else if (type == ProcessVariable.LINK){
                def link = GrailsflowRequestUtils.getLinkFromParams(params, "var_${variable.name}")
                value = "${link.toString()}"
            } else if (type == ProcessVariable.LIST){
                params.datePattern = gf.datePattern()?.toString()
                try {
                    value = GrailsflowRequestUtils.getVariableItemsFromParams(variable.name, params)
                } catch (Exception ex) {
                    flash.errors << "Specified values for List items are not fit the type of List elements. "+ex
                    log.error("Specified values for List items are not fit the type of List elements", ex)
                    render(view: "scheduleProcess", model: [processClasses:  classes, processClass: processClass,
                            repeatingInfo: getRepeatingPeriods(), params: params, bean: params])
                }
            } else if (type == ProcessVariable.OBJECT) {
                value = null
            }

            if (!value && (variable.required == true)) {
                flash.errors << g.message(code: "plugin.grailsflow.messages.required", args: [variable.name])
            }

            variables[variable.name] = value
        }

        def startDay = GrailsflowUtils.getParsedDate(params.startDay, gf.datePattern()?.toString())
        if (!startDay) {
            flash.warnings << g.message(code: "plugin.grailsflow.messages.startDay.invalid", args: [params.startDay])
            startDay = new Date()
        }
        params.startDay = startDay

        def startTime_hours = 0
        def startTime_minutes = 0
        if (params.startTime_hours && params.startTime_hours != '00') {
            try {
                startTime_hours = Integer.valueOf(params.startTime_hours)
            } catch (Exception e) {
                flash.errors << g.message(code: "plugin.grailsflow.messages.intValue.invalid", args: ["Start Time minutes"])
                log.error("Exception in parsing Integer value from ${params.startTime_hours}: ${e.message}")
            }
        }

        if (params.startTime_minutes && params.startTime_minutes != '00') {
            try {
                startTime_minutes = Integer.valueOf(params.startTime_minutes)
            } catch (Exception e) {
                flash.errors << g.message(code: "plugin.grailsflow.messages.intValue.invalid", args: ["Start Time minutes"])
                log.error("Exception in parsing Integer value from ${params.startTime_minutes}: ${e.message}")
            }
        }

        def startTime = (startTime_hours*60 + startTime_minutes)*60000
        def startDate = new Date(startDay?.time + startTime)

        def calendarTime = Calendar.getInstance()
        calendarTime.time = startDate
        params.startTime_hours = calendarTime.get(Calendar.HOUR_OF_DAY) < 10 ?
            "0${calendarTime.get(Calendar.HOUR_OF_DAY)}" : calendarTime.get(Calendar.HOUR_OF_DAY)
        params.startTime_minutes = calendarTime.get(Calendar.MINUTE) < 10 ?
            "0${calendarTime.get(Calendar.MINUTE)}" : calendarTime.get(Calendar.MINUTE)

        try{
            def repeating = (params.repeating && params.repeating != '') ? Integer.valueOf(params.repeating) :
                (params.customRepeating && params.customRepeating != '') ? Integer.valueOf(params.customRepeating) : null
            if (repeating == null || repeating < 0) {
                flash.errors << g.message(code: "plugin.grailsflow.messages.error.repeating")
            } else if (flash.errors.isEmpty()) {
                params.customRepeating = repeating
                def requester = securityHelper.getUser(session)
                if (schedulerOperationsService
                    .scheduleProcessJob(params.processID, variables, requester, startDate, repeating)) {
                    flash.message = g.message(code: "plugin.grailsflow.messages.job.scheduled", args: [params.processID])
                } else {
                    flash.errors << g.message(code: "plugin.grailsflow.messages.job.scheduled.error", args: [params.processID])
                }
            }
        } catch (NumberFormatException nfe) {
            flash.errors << g.message(code: "plugin.grailsflow.messages.error.convertInt", args: [params.customRepeating])
            log.error("Error in converting repeating interval: ${nfe.message}")
        }

        render(view: "scheduleProcess", model: [processClasses:  classes, processClass: processClass,
               repeatingInfo: getRepeatingPeriods(), params: params, bean: params])
    }

    private Map getSchedulerDetails() {
        def schedulerDetails = [:]
        schedulerDetails.schedulerInfo = getGeneralInformationText()
        schedulerDetails.schedulerStatus = schedulerOperationsService.getSchedulerStatus()

        def timePatterns = [ year: g.message(code: "plugin.grailsflow.running.year"),
            month: g.message(code: "plugin.grailsflow.running.month"),
            day: g.message(code: "plugin.grailsflow.running.day")
        ]
        schedulerDetails.runningJobs = schedulerOperationsService.getRunningJobsInfo(timePatterns)
        schedulerDetails.scheduledJobs = schedulerOperationsService.getScheduledJobsInfo()
        return schedulerDetails
    }

    private String getGeneralInformationText() {
        def schedulerDataMap = schedulerOperationsService.getSchedulerDataMap()
        String persistance = schedulerDataMap.isPersistanceSupported ? "" :
                g.message(code: "plugin.grailsflow.text.not")

        return g.message(code: "plugin.grailsflow.text.info", args:
            [schedulerDataMap.schedulerName, schedulerDataMap.instanceId,
             gf.displayDateTime(value: schedulerDataMap.runningSince)?.toString() ?: '', String.valueOf(schedulerDataMap.numberOfJobs),
             schedulerDataMap.storeClassName, persistance, schedulerDataMap.poolClassName,
             String.valueOf(schedulerDataMap.poolSize), schedulerDataMap.version])
    }

    private def getProcessScriptInfo(def scriptName) {
        if (!scriptName) return null
        return processManagerService.getProcessClass(scriptName)
    }

    private def getRepeatingPeriods() {
        return [
            0: g.message(code: "plugin.grailsflow.label.repeating.once"),
            60000: g.message(code: "plugin.grailsflow.label.repeating.minute"),
            (60000 * 60 * 24): g.message(code: "plugin.grailsflow.label.repeating.day"),
            (60000 * 60 * 24 * 7): g.message(code: "plugin.grailsflow.label.repeating.week")
        ]
    }
}
