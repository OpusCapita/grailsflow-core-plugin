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

import com.jcatalog.grailsflow.utils.TranslationUtils
import org.springframework.web.servlet.support.RequestContextUtils as RCU

import org.springframework.web.multipart.commons.CommonsMultipartFile
import java.text.SimpleDateFormat

import com.jcatalog.grailsflow.model.process.ProcessVariable
import org.apache.commons.lang.StringUtils
import java.text.ParseException

/**
 * SchedulerDetailsController class is used for monitoring and 
 * managing (CRUD) of Quartz jobs.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class SchedulerDetailsController extends GrailsFlowSecureController {
    private static final String RESOURCE_BUNDLE = "grailsflow.schedulerDetails"

    def grailsflowMessageBundleService
    def processManagerService
    def workareaPathProvider
    def documentsPath
    def datePatterns

    def schedulerOperationsService
    
    def index = {
        redirect(action: "showSchedulerDetails")
    }

    def showSchedulerDetails = {
        def schedulerDetails = getSchedulerDetails()
        if (!schedulerDetails) {
            flash.error = grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.error.schedulerDetails")
        }

        [schedulerDetails: schedulerDetails]
    }

    def pauseScheduler = {
        if (!schedulerOperationsService.pauseResumeScheduler()) {
            flash.error = grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.error.pauseResume")
        }
        redirect(action: "showSchedulerDetails")
    }

    def pause = {
        if (!params.name || !params.group) {
            flash.error = grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.error.pauseResume")
            return forward(action: "showSchedulerDetails", params: params)
        }
        if (params.isRunning == "true") {
            flash.message = grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.job.running", [params.name, params.group])
            return redirect(action: "showSchedulerDetails")
        }

        def shouldBePaused = params.isPaused == "false"
        if (!schedulerOperationsService.pauseResumeJob(params.name, params.group, shouldBePaused)) {
            flash.error = grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.error.pauseResume")
        }
        redirect(action: "showSchedulerDetails")
    }

    def edit = {
        if (!params.name || !params.group) {
            flash.error = grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.error.pauseResume")
            return forward(action: "showSchedulerDetails", params: params)
        }

        def jobDetails = schedulerOperationsService.getJobDetails(params.name, params.group)
        render(view: "editScheduledJob", model: [jobDetails: jobDetails,
               repeatingInfo: getRepeatingPeriods(), params: params])
    }

    def updateJob = {
        if (!flash.errors) flash.errors = []

        if (!params.name || !params.group) {
            flash.error = grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.error.pauseResume")
            return forward(action: "showSchedulerDetails", params: params)
        }

        def startTime_hours = 0
        def startTime_minutes = 0

        if (params.startTime_hours && params.startTime_hours != '00') {
            try {
                startTime_hours = Integer.valueOf(params.startTime_hours)
            } catch (Exception e) {
                flash.errors << grailsflowMessageBundleService
                                    .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.intValue.invalid", ["Start Time hours"])
            }
        }

        if (params.startTime_minutes && params.startTime_minutes != '00') {
            try {
                startTime_minutes = Integer.valueOf(params.startTime_minutes)
            } catch (Exception e) {
                flash.errors << grailsflowMessageBundleService
                    .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.intValue.invalid", ["Start Time minutes"])
            }
        }

        def startTime = (startTime_hours*60 + startTime_minutes)*60000
        def startDay = GrailsflowUtils.getParsedDate(params.startDay, gf.datePattern()?.toString())
        if (!startDay) {
            flash.warnings = [grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.startDay.invalid", [params.startDay])]
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
                flash.errors << grailsflowMessageBundleService.getMessage(RESOURCE_BUNDLE, "grailsflow.messages.error.repeating")
            } else {
                params.customRepeating = repeating
                def isJobUpdated = schedulerOperationsService
                    .updateScheduledJob(params.name, params.group, startDate, repeating)
                if (isJobUpdated) {
                    flash.message = grailsflowMessageBundleService
                        .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.job.updated", [params.name])
                } else {
                    flash.errors << grailsflowMessageBundleService
                        .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.job.notUpdated", [params.name])
                }
            }
        } catch (NumberFormatException nfe) {
            flash.errors << grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.error.convertInt", [params.customRepeating])
            log.error("Error in converting repeting interval: ${nfe.message}")
        }
        redirect(action: "showSchedulerDetails")
    }

    def delete = {
        if (params.name && params.group) {
            if (schedulerOperationsService.deleteScheduledJob(params.name, params.group)) {
                flash.message = grailsflowMessageBundleService
                    .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.job.deleted")
            } else {
                flash.error = grailsflowMessageBundleService
                    .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.error.delete")
            }
        } else {
            flash.error = grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.error.delete")
        }
        redirect(action: "showSchedulerDetails")
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
            flash.error = grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.job.scheduled.error", [params.processID])
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
                        flash.warnings << grailsflowMessageBundleService
                            .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.dateValue.invalid", [variable.name])
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
                flash.errors << grailsflowMessageBundleService
                    .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.required", [variable.name])
            }

            variables[variable.name] = value
        }

        def startDay = GrailsflowUtils.getParsedDate(params.startDay, gf.datePattern()?.toString())
        if (!startDay) {
            flash.warnings << grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.startDay.invalid", [params.startDay])
            startDay = new Date()
        }
        params.startDay = startDay
        
        def startTime_hours = 0
        def startTime_minutes = 0
        if (params.startTime_hours && params.startTime_hours != '00') {
            try {
                startTime_hours = Integer.valueOf(params.startTime_hours)
            } catch (Exception e) {
                flash.errors << grailsflowMessageBundleService
                    .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.intValue.invalid", ["Start Time minutes"])
                log.error("Exception in parsing Integer value from ${params.startTime_hours}: ${e.message}")
            }
        }

        if (params.startTime_minutes && params.startTime_minutes != '00') {
            try {
                startTime_minutes = Integer.valueOf(params.startTime_minutes)
            } catch (Exception e) {
                flash.errors << grailsflowMessageBundleService
                    .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.intValue.invalid", ["Start Time minutes"])
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
                flash.errors << grailsflowMessageBundleService
                    .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.error.repeating")
            } else if (flash.errors.isEmpty()) {
                params.customRepeating = repeating
                def requester = securityHelper.getUser(session)
                if (schedulerOperationsService
                    .scheduleProcessJob(params.processID, variables, requester, startDate, repeating)) {
                    flash.message = grailsflowMessageBundleService
                        .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.job.scheduled", [params.processID])
                } else {
                    flash.errors << grailsflowMessageBundleService
                        .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.job.scheduled.error", [params.processID])
                }
            }
        } catch (NumberFormatException nfe) {
            flash.errors << grailsflowMessageBundleService
                .getMessage(RESOURCE_BUNDLE, "grailsflow.messages.error.convertInt", [params.customRepeating])
            log.error("Error in converting repeating interval: ${nfe.message}")
        }

        render(view: "scheduleProcess", model: [processClasses:  classes, processClass: processClass,
               repeatingInfo: getRepeatingPeriods(), params: params, bean: params])
    }

    private Map getSchedulerDetails() {
        def schedulerDetails = [:]
        schedulerDetails.schedulerInfo = getGeneralInformationText()
        schedulerDetails.schedulerStatus = schedulerOperationsService.getSchedulerStatus()

        def timePatterns = [ year: grailsflowMessageBundleService.getMessage(RESOURCE_BUNDLE, "grailsflow.running.year"),
            month: grailsflowMessageBundleService.getMessage(RESOURCE_BUNDLE, "grailsflow.running.month"),
            day: grailsflowMessageBundleService.getMessage(RESOURCE_BUNDLE, "grailsflow.running.day")
        ]
        schedulerDetails.runningJobs = schedulerOperationsService.getRunningJobsInfo(timePatterns)
        schedulerDetails.scheduledJobs = schedulerOperationsService.getScheduledJobsInfo()
        return schedulerDetails
    }

    private String getGeneralInformationText() {
        def schedulerDataMap = schedulerOperationsService.getSchedulerDataMap()
        String persistance = schedulerDataMap.isPersistanceSupported ? "" :
            grailsflowMessageBundleService.getMessage(RESOURCE_BUNDLE, "grailsflow.text.not")

        return grailsflowMessageBundleService.getMessage(RESOURCE_BUNDLE, "grailsflow.text.info",
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
            0: grailsflowMessageBundleService.getMessage(RESOURCE_BUNDLE, "grailsflow.label.repeating.once"),
            60000: grailsflowMessageBundleService.getMessage(RESOURCE_BUNDLE, "grailsflow.label.repeating.minute"),
            (60000*24): grailsflowMessageBundleService.getMessage(RESOURCE_BUNDLE, "grailsflow.label.repeating.day"),
            (60000*24*7): grailsflowMessageBundleService.getMessage(RESOURCE_BUNDLE, "grailsflow.label.repeating.week")
        ]
    }

}