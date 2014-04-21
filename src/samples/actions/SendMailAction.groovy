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
package mail

import com.jcatalog.grailsflow.actions.Action
import org.apache.commons.logging.LogFactory

class SendMailAction extends Action {
    public String mailFrom
    public String mailTo
    public String subject
    public String message

    protected static def log = LogFactory.getLog(SendMailAction.class)

    def execute() {
        log.debug("Sending mail")
        log.debug("From: $mailFrom")
        log.debug("To: $mailTo")
        log.debug("Subject: $subject")
        if (message) {
          log.debug("Message: $message")
        }

        def mailService = getObjectByName("mailService")
        
        def messageText = message

        if (mailService) {
          mailService.sendMail {
		        from "$mailFrom" 
						to "$mailTo"
						subject "$subject"
						html "$messageText"
				   }
        } else {
            log.error("Mail plugin is not installed. Unable to send email.")
        }
        return "okay"
    }

}