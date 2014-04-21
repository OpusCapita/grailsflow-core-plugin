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

package com.jcatalog.grailsflow.actions

import com.jcatalog.grailsflow.jobs.CallbackJob

import org.quartz.*
import org.springframework.scheduling.quartz.JobDetailBean

/**
 * An AsynchronousAction abstract class represents asynchronous action
 * entity that can be executed during process flow. Action Builder
 * knows how to deal with such Actions. The class contains common methods
 * and variables for implemented actions of such type.
 *
 * All Actions that are available for process/node definition
 * should extend Action (or AsynchronousAction) class and implements
 * its abstract methods.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
abstract class AsynchronousAction extends Action {
    protected String resultVarName
    protected Long delay

    protected String group = "GFW_GROUP"

    abstract Class getScheduledJobClass()

    def executeAsynchronously() {
        def parameters = new JobDataMap()
        parameters.putAll(executionContext)
        parameters.put("resultVarName", resultVarName)
        
        // we need to execute standard action part
        def result = execute()
        parameters.put("result", result)

        // update parameters with action variables
        def updatedParameters = updateActionVariables()
        updatedParameters.each() {
            parameters.put(it.key, it.value)
        }

        // configure clientExecutor parameter 
        parameters.clientExecutor = getObjectByName("clientExecutor")

        // schedule job
        // create and schedule job
        def scheduler = getObjectByName("quartzScheduler")
        if(scheduler) {
            def job = new JobDetailBean()

            def jobClass = getScheduledJobClass()
            if (CallbackJob.class.isAssignableFrom(jobClass)) {
                job.jobClass = jobClass
                job.group = group
            } else {
                 log.error("Scheduled Job should be the instance of GrailsflowCallbackJob class!")
                 return 
            }

            def startDate = new Date()

            job.setJobDataMap(parameters)
            job.name = "${getScheduledJobClass().simpleName}_${startDate.time}"
            
            def jobTime = delay ? new Date(startDate.time + delay) : new Date(startDate.time)
            SimpleTrigger trigger = new SimpleTrigger("${job.name}Trigger", group, jobTime)

            try{
                scheduler.scheduleJob(job, trigger)
            } catch(Exception ex){
                log.error("Error in scheduling Job", ex)
            }
        }
    }

    private def updateActionVariables() {
        def actionVars = [:]
        // get action variables by reflection
        this.getClass().fields.each {
            if (it.toString().indexOf(" static ") == -1) {
                actionVars[it.name] = this.getClass().getField(it.name).get(this)
            }
        }
                
        return actionVars
    }
}