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
package com.jcatalog.grailsflow.extension.email

import com.jcatalog.grailsflow.extension.SendEventParameters
import com.jcatalog.grailsflow.model.process.BasicProcess

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import com.jcatalog.grailsflow.engine.*
import org.springframework.core.io.Resource

import java.util.Properties
import java.util.regex.Pattern

import javax.mail.Folder
import javax.mail.Flags
import javax.mail.Message
import javax.mail.Session
import javax.mail.Store

/**
 * GrailsflowEventEmailProcessor gets ProcessID, NodeID,
 * Event and Process variables from email's body.
 *
 * @author Maria Voitovich
 */
class GrailsflowEventEmailProcessor implements EventEmailProcessor {
    protected Log log = LogFactory.getLog(getClass())
    def processManagerService 

    SendEventParameters parseEventMessage(Message message) {
       def subject = message.subject
       def from = message.from
       log.debug("Processing message: ${subject} \t from ${from}")

       def type = message.contentType
       
       if (!type || !type.contains("text/plain")) {
         log.warn("Message content type should be text/plain")
         return null
       }
       
       String body = message.content
       def sendEventParams = new SendEventParameters()
       
       def processID = getPropertyFromMessage(body, "processID")
       if (processID == null) {
         log.warn("Message does not contain processID")
         return null
       }
       try{ 
         sendEventParams.processID = new Long(processID)
       } catch (Exception e) {
         log.warn("Invalid processID: ${processID}")
         return null
       }
       
       sendEventParams.nodeID = getPropertyFromMessage(body, "nodeID")
       if (!sendEventParams.nodeID) {
         log.warn("nodeID is not specified.")
         return null
       }
       
       sendEventParams.event = getPropertyFromMessage(body, "event")
       if (!sendEventParams.event) {
         log.warn("Event is not specified.")
         return null
       }
       
       // TODO: is there better way to take variables names for process? 
       def basicProcess = BasicProcess.get(sendEventParams.processID)
       if (basicProcess) {
	     def processClass = processManagerService.getProcessClass(basicProcess.type)
	     // collect variables
	     processClass?.variables?.each(){ varDef ->
	       def name = varDef.name
	       sendEventParams.variables[name] = getPropertyFromMessage(body, name)
	     }
	   }
       
       def requester = getPropertyFromMessage(body, "requester")
       sendEventParams.requester = requester != null ? requester : message.from*.address.join(',') 
       if (!sendEventParams.requester){
         log.warn("Unable to determine requester that sends event")
         return null
       }
       
       return sendEventParams
    }
    
    protected String getPropertyFromMessage(String message, String key){
       if (!message) return null
       if (!key) return null
       // start with spaces and/or ">", then "key=", then value and newline at the end 
       String patternString = "^(?:(?:\\s|[>])*)${key}=(.*)\$".toString()
       def parameterPattern = Pattern.compile(patternString, Pattern.MULTILINE)
       // find first matching
       def matcher = parameterPattern.matcher(message) 
       if (matcher.find() ) {
        def value = matcher.group(1)
        log.debug("Found value ${value} for ${key} in message.")
        return value
       } else {
        log.debug("Didn't find value for ${key} in message.")
        return null
       }
    }
    
    void processSendEventResult(Message message, SendEventParameters parameters, int result) {
      log.debug("Sending of event for message ${message.subject} \t from ${message.from} finishes with ${result} error code.")
      return
    }
    
}