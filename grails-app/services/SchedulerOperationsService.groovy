import org.quartz.SchedulerException
import org.quartz.Trigger
import java.text.DateFormat
import org.apache.commons.lang.time.DurationFormatUtils
import org.quartz.SimpleTrigger
import org.springframework.scheduling.quartz.JobDetailBean
import com.jcatalog.grailsflow.jobs.ProcessActivatorJob
import org.quartz.JobDataMap
import java.text.SimpleDateFormat
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext

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


/**
 * SchedulerOperationsService class is used for operations with scheduler, like
 *  - getting information
 *  - scheduling process
 *  - updating scheduled processes/jobs
 *
 * @author July Antonicheva
 * 
 */

class SchedulerOperationsService implements ApplicationContextAware {
    String JOB_GROUP = "GRAILSFLOW"
    String PROCESS_GROUP = "PROCESS_GROUP"

    def quartzScheduler
    ApplicationContext applicationContext

    public Boolean pauseResumeScheduler() {
        try {
            if (quartzScheduler.isInStandbyMode()) {
                quartzScheduler.start()
            } else {
                quartzScheduler.standby()
            }
        } catch(SchedulerException se) {
            log.error("Errors in pausing scheduler", se)
            return Boolean.FALSE
        }
        return Boolean.TRUE
    }

    public Boolean pauseResumeJob(String jobName, String jobGroup, Boolean shouldBePaused) {
        try {
            if (shouldBePaused) {
                quartzScheduler.pauseJob(jobName, jobGroup)
            } else {
                quartzScheduler.resumeJob(jobName, jobGroup)
            }
        } catch(SchedulerException se) {
            log.error("Errors in pausing job ${jobName} of group ${jobGroup}", se)
            return Boolean.FALSE
        }
        return Boolean.TRUE
    }

    public Map getJobDetails(String jobName, String jobGroup) {
        def jobDetails = [:]
        def trigger = quartzScheduler.getTrigger(jobName, jobGroup)
        if (!trigger) {
            return jobDetails
        }
        jobDetails.job = quartzScheduler.getJobDetail(jobName, jobGroup)
        jobDetails.trigger = trigger

        def calendarTime = Calendar.getInstance()
        calendarTime.time = trigger.startTime

        jobDetails.repeating = (!trigger.repeatInterval || trigger.repeatInterval == 0) ? 0 : trigger.repeatInterval
        jobDetails.customRepeating = jobDetails.repeating
        jobDetails.startDay = calendarTime.time
        jobDetails.startTime_hours = calendarTime.get(Calendar.HOUR_OF_DAY) < 10 ?
            "0${calendarTime.get(Calendar.HOUR_OF_DAY)}" : calendarTime.get(Calendar.HOUR_OF_DAY)
        jobDetails.startTime_minutes = calendarTime.get(Calendar.MINUTE) < 10 ?
            "0${calendarTime.get(Calendar.MINUTE)}" : calendarTime.get(Calendar.MINUTE)

        return jobDetails
    }

    public Boolean updateScheduledJob(String jobName, String jobGroup, Date startDate,
                                      Long repeatInterval) {
        def trigger = quartzScheduler.getTrigger(jobName, jobGroup)
        if (!trigger) {
            return Boolean.FALSE
        }

        trigger.startTime = startDate
        trigger.repeatInterval = repeatInterval
        trigger.repeatCount = repeatInterval == 0 ? 0 : SimpleTrigger.REPEAT_INDEFINITELY
        try {
            quartzScheduler.rescheduleJob(trigger.name, jobGroup, trigger)
        } catch(SchedulerException se) {
            log.error("Errors in rescheduling job", se)
            return Boolean.FALSE
        }
        return Boolean.TRUE
    }

    public Boolean deleteScheduledJob(String jobName, String jobGroup) {
        try {
            quartzScheduler.deleteJob(jobName, jobGroup)
        } catch(SchedulerException se) {
            log.error("Errors in deleting Job ${jobName} of group ${jobGroup}", se)
            return Boolean.FALSE
        }
        return Boolean.TRUE
    }

    public Boolean scheduleProcessJob(String processType, Map variables, String requester,
                                      Date startDate, Long repeatInterval) {
        def job = new JobDetailBean()
        job.jobClass = ProcessActivatorJob.class
        job.group = PROCESS_GROUP

        def jobDataMap = new JobDataMap()
        jobDataMap.variables = variables
        jobDataMap.processManagerService = applicationContext.getBean("processManagerService")
        jobDataMap.processType = processType
        jobDataMap.requester = requester

        job.jobDataMap = jobDataMap
        job.name = "${processType}ProcessStarted_${new Date().time}"

        def trigger = new SimpleTrigger("${job.name}Trigger", PROCESS_GROUP, startDate)
        trigger.repeatInterval = repeatInterval
        trigger.repeatCount = repeatInterval == 0 ? 0 : SimpleTrigger.REPEAT_INDEFINITELY
        try {
            quartzScheduler.scheduleJob(job, trigger)
        } catch(SchedulerException se) {
            log.error("Errors in scheduling Process ${processType}", se)
            return Boolean.FALSE
        }
        return Boolean.TRUE
    }

    public Collection getScheduledJobsInfo() {
        return getScheduledGroupJobsInfo(JOB_GROUP) + getScheduledGroupJobsInfo(PROCESS_GROUP)
    }

    private Collection getScheduledGroupJobsInfo(String group) {
        def result = []
        try {
            quartzScheduler.getJobNames(group)?.each() { jobName ->
                def triggers = quartzScheduler.getTriggersOfJob(jobName, group)
                triggers?.each() { trigger ->
                    def jobDetails = quartzScheduler.getJobDetail(jobName, group)
                    def jobsInfo = [:]
                    jobsInfo.job = jobDetails
                    jobsInfo.trigger = trigger

                    def previousFireTime = trigger.getPreviousFireTime()
                    if (previousFireTime) {
                        jobsInfo.previosFireTime = previousFireTime
                    }

                    def nextFireTime = trigger.getNextFireTime()
                    if (nextFireTime) {
                        jobsInfo.nextFireTime = nextFireTime
                    }

                    def dataMap = jobDetails?.jobDataMap
                    if (quartzScheduler.getTriggerState(trigger.name, trigger.group) == Trigger.STATE_PAUSED) {
                        jobsInfo.paused = Boolean.TRUE
                    }

                    def currentlyExecutingJobs = quartzScheduler.getCurrentlyExecutingJobs()
                    def executingJobsNames = []
                    currentlyExecutingJobs.each() { currentlyExecutingJob ->
                        def context = currentlyExecutingJob
                        executingJobsNames.add(context.getJobDetail().getName())
                    }
                    if (executingJobsNames.contains(jobName)) {
                        jobsInfo.running = Boolean.TRUE
                    }

                    jobsInfo.executionTimeText = dataMap?.get(trigger.name)
                    result << jobsInfo
                }
            }
        } catch(SchedulerException se) {
            log.error("Errors in scheduler jobs retrieving", se)
        }
        return result
    }

    private Map getSchedulerDataMap() {
        def schedulerDetails = [:]
        try {
            def meta = quartzScheduler.getMetaData()
            schedulerDetails.isPersistanceSupported = meta.jobStoreSupportsPersistence()
            schedulerDetails.schedulerName = quartzScheduler.getSchedulerName()
            schedulerDetails.instanceId = quartzScheduler.getSchedulerInstanceId()
            schedulerDetails.runningSince = meta.getRunningSince()
            schedulerDetails.numberOfJobs = meta.getNumberOfJobsExecuted()
            schedulerDetails.version = meta.getVersion()
            schedulerDetails.storeClassName = meta.getJobStoreClass().getName()
            schedulerDetails.poolSize = meta.getThreadPoolSize()
            schedulerDetails.poolClassName = meta.getThreadPoolClass().getName()
        } catch(SchedulerException se) {
            log.error("Errors in retrieving scheduler data map", se)
        }
        return schedulerDetails
    }
    private Map getSchedulerStatus() {
        [ paused: quartzScheduler.isInStandbyMode(), shutdown: quartzScheduler.shutdown]
    }

    private Collection getRunningJobsInfo(timePatterns) {
        def result = []
        try {
            def jobExecutionContexts = quartzScheduler.getCurrentlyExecutingJobs()
            jobExecutionContexts.each() { context ->
                def jobDetails = [:]
                jobDetails.job = context.getJobDetail()
                jobDetails.trigger = context.getTrigger()
                jobDetails.startTime = context.getFireTime()
                jobDetails.runningTime = mathRunningTime(context.getFireTime(), timePatterns)
                jobDetails.running = true
                result.add(jobDetails)
            }
        } catch(SchedulerException se) {
            log.error("Errors in retrieving currently executing jobs", se)
        }
        return result
    }

    private String mathRunningTime(Date startTime, Map timePatterns) {
        long startTimeMilliseconds = startTime.getTime()
        long nowTimeMilliseconds = new Date().getTime()
        String year = DurationFormatUtils.formatPeriod(startTimeMilliseconds,
                nowTimeMilliseconds, "y")
        if (!year.equals("0")) {
            return DurationFormatUtils.formatPeriod(startTimeMilliseconds,
                nowTimeMilliseconds, timePatterns.year)
        } else if (!DurationFormatUtils.formatPeriod(startTimeMilliseconds,
                    nowTimeMilliseconds, "MM").equals("00")) {
            return DurationFormatUtils.formatPeriod(startTimeMilliseconds,
                nowTimeMilliseconds, timePatterns.month);
        } else if (!DurationFormatUtils.formatPeriod(startTimeMilliseconds,
                    nowTimeMilliseconds, "dd").equals("00")) {
            return DurationFormatUtils.formatPeriod(startTimeMilliseconds,
                startTimeMilliseconds, timePatterns.day)
        } else {
            return DurationFormatUtils.formatPeriod(startTimeMilliseconds,
                nowTimeMilliseconds, "HH:mm:ss")
        }
    }
}