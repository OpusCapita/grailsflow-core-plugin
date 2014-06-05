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

package com.jcatalog.grailsflow.demo.jobs

import com.jcatalog.grailsflow.jobs.CallbackJob

import org.quartz.JobDataMap

/**
 * Simple implementation for job class that can be scheduled in process actions.
 * All jobs that are configured in actions should be extended from
 * CallbackJob class.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class PrintMessageJob extends CallbackJob {
    public Object executeAndReturnCallbackInfo(JobDataMap jobDataMap) {
        println("PrintMessageJob is running!")
        sleep(30000)
        return """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
                    <note>
                      <to>Tove</to>
                      <from>Jani</from>
                      <heading>Reminder</heading>
                      <body>Don't forget me this weekend!</body>
                    </note>"""
    }

}