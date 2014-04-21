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

import java.util.Properties
import java.util.regex.Pattern

import javax.mail.Folder
import javax.mail.Flags
import javax.mail.Message
import javax.mail.Session
import javax.mail.Store

import org.apache.commons.lang.BooleanUtils

import com.jcatalog.grailsflow.extension.email.EventEmailProcessor

/**
 * EmailEventReceiverJob class gets Events sent to the Nodes via Email.
 *
 * Checks configured email account, gets new emails. 
 * Passes message to EventEmailProcessor.parseEventMessage to get parameters for sendEvent().
 * Calls sendEvent() with received parameters.
 * Passes result of event to EventEmailProcessor.processSendEventResult()
 *
 * @author Maria Voitovich
 */
class EmailEventReceiverJob {
    static triggers = {
        custom name: 'eventsEmailCheck', triggerClass: com.jcatalog.grailsflow.scheduling.triggers.ConfigurableSimpleTrigger
    }
    def grailsApplication
    def concurrent = false
    String group = "GRAILSFLOW"

    // Collaborators 
    def processManagerService
    EventEmailProcessor eventEmailProcessor

    def execute(){
        String enabled = grailsApplication.config.grailsflow.events.mail.enabled
        if (enabled != null && enabled instanceof String) {
            enabled = BooleanUtils.toBoolean(enabled)
        }
        if (!enabled) {
            log.debug("Events email processing is disabled. Skipping.")
            return;
        }
        if (eventEmailProcessor == null ){
            log.error("eventEmailProcessor bean is not configured. Skipping event email processing.")
            return;
        }
      
        log.debug("Checking for new events sent by email")

        // Getting email account configuration
        String mailHost = grailsApplication.config.grailsflow.events.mail.host ?: null
        String mailAccount = grailsApplication.config.grailsflow.events.mail.account ?: null
        String mailPassword = grailsApplication.config.grailsflow.events.mail.password ?: null
      
        if (!(mailHost && mailAccount && mailPassword)) {
            log.warn("Mail account parameters are not specified")
            return
        }

        // Get session
        Properties props = new Properties()
        Session session = Session.getDefaultInstance(props, null)

        // Get the store
        Store store = session.getStore("pop3")
        try {
            store.connect(mailHost, mailAccount, mailPassword)
        } catch (Exception e){
            log.error("Unable to connect to mail account ${mailAccount} on host ${mailHost}. Please check account parameters.")
            return
        }

        // Get inbox folder
        Folder folder = store.getFolder("INBOX")
        folder.open(Folder.READ_WRITE)

        log.debug("Processing INBOX folder")

        // Get messages
        Message[] messages = folder.getMessages()
        Integer n = messages.length

        // there are unread messages
        if (n > 0) {
            log.debug("$n messages found")
            messages.each() { message ->
                log.debug("Processing message: ${message.getSubject()} \t from ${message.getFrom()}")
                def sendEventParams = eventEmailProcessor.parseEventMessage(message)
                if (!sendEventParams) {
                    log.debug("eventEmailProcessor does not return parameters for sending event. Event won't be send.")
                    return
                }

                log.debug("Sending event ${sendEventParams.event} to node ${sendEventParams.nodeID} of process #${sendEventParams.processID}")
                def errorCode = processManagerService.sendEvent(sendEventParams)
                if (errorCode != 0){
				    log.warn("Unable to invoke event ${sendEventParams.event} over node ${sendEventParams.nodeID}. Error code ${errorCode} returned.")
                }
                eventEmailProcessor.processSendEventResult(message, sendEventParams, errorCode)

                // delete processed message
                message.setFlag(Flags.Flag.DELETED, true);
            }
        } else {
            log.debug("No new event messages were found")
        }

        // Close connection
        folder.close(true);
        store.close();
			
        log.debug("Finished checking for new events sent by email")
    }
   
}