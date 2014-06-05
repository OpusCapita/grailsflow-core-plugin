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
import org.codehaus.groovy.grails.commons.ApplicationAttributes

import org.apache.commons.lang.BooleanUtils
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.Trigger

import static org.quartz.TriggerKey.triggerKey

/**
 * Bootstrap class which pauses trigger if it's autoStartup config parameter is set to false.
 *
 * @author Maria Voitovich
 *
 */
class GrailsflowSchedulerBootStrap {
    static String JOB_GROUP = "GRAILSFLOW"
    static final String AUTO_START = "autoStart"

    def grailsApplication

    def init = { servletContext ->
        def ctx = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT)
        def quartzScheduler = ctx?.quartzScheduler
        
        if (!quartzScheduler) {
          log.error("quartzScheduler bean not found in application context. Please configure it for using scheduled jobs.")
          return
        }

        quartzScheduler.getJobKeys(GroupMatcher.jobGroupEquals(JOB_GROUP))?.each() { jobKey ->
            List<Trigger> triggers = quartzScheduler.getTriggersOfJob(jobKey)
            triggers?.each() { Trigger trigger ->
            def config = grailsApplication.config.grailsflow.scheduler.get(trigger.key.name)
            if (config instanceof ConfigObject) {
              Map properties = config.flatten()
              def autoStart = properties.get(AUTO_START)
              if (autoStart != null && (autoStart instanceof String || autoStart instanceof Boolean)) {
                autoStart = BooleanUtils.toBoolean(autoStart)
              } else {
                autoStart = Boolean.TRUE
              }
              if (!autoStart) {
                quartzScheduler.pauseTrigger(triggerKey(trigger.key.name, trigger.key.group))
              }
            }
            
          }
        }
    }
    
    def destroy = {}
}