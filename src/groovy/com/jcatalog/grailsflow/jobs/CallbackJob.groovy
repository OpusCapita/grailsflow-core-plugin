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
 
package com.jcatalog.grailsflow.jobs

import org.springframework.scheduling.quartz.QuartzJobBean
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobDataMap
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.apache.commons.httpclient.methods.StringRequestEntity

/**
 * CallbackJob class contains common mechanizm for preparing callback
 * request information and executing call.
 * All custom Quartz jobs should extend it and implement abstract
 * methods.
 * The callback information is in XML view. It contains node execution
 * result for following transitions, values for process variables, and
 * also some kind of information from custom Job (or answer from WS server).
 * This information is included in [CDATA[]] block, because we do not
 * know which type of information is returned (probably XML) and
 * we need to escape special characters.
 *
 *
 * @author Stephan Albers
 * @author July Karpey
 */
abstract class CallbackJob extends QuartzJobBean {
    private static final Log log = LogFactory.getLog(getClass())
    
    def clientExecutor
    
    String callbackRelativeUrl = "/process/extendedSendEvent"

    abstract public Object executeAndReturnCallbackInfo(JobDataMap jobDataMap)
    
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        // define clientExecutor
        if (ctx.jobDataMap.clientExecutor) {
            clientExecutor = ctx.jobDataMap.clientExecutor
            ctx.jobDataMap.remove(clientExecutor)
        }

        def siteBase = ctx.jobDataMap.siteBase
        def url = siteBase + callbackRelativeUrl
        
        // preparing return data
        StringBuffer xmlDocument = new StringBuffer()
        xmlDocument.append("<document>")
        if (ctx && ctx.jobDataMap) {
            ctx.jobDataMap.each {
                xmlDocument.append("<parameter>")
                xmlDocument.append("<name>${it.key}</name>")
                xmlDocument.append("<value>${it.value?.toString()}</value>")
                xmlDocument.append("</parameter>")
            }
        }

        // enhance parameters with additional information
        def result = executeAndReturnCallbackInfo(ctx.jobDataMap)

        xmlDocument.append("<actionresult><![CDATA[${result?.toString()}]]></actionresult>")
        xmlDocument.append("</document>")

        log.debug("Prepared document: $xmlDocument")

        if (clientExecutor) {
            clientExecutor
                .executeCall(siteBase, url, new StringRequestEntity(xmlDocument.toString(), "text/xml", "utf-8"))
        } else {
            log.debug("No ClientExecutor is configured")
        }
        
        return
    }
}