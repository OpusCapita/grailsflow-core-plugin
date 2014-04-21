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

import org.apache.commons.lang.BooleanUtils

/**
 * Web editor of mail account configuration for EmailEventReceiverJob
 *
 * @author Maria Voitovich
 */
class EmailEventReceiverJobController extends GrailsFlowSecureController {
    private static final String RESOURCE_BUNDLE = "grailsflow.emailReceiver"

    def index = {
        redirect(action: emailConfiguration)
    }

    def emailConfiguration = {
        def enabled = grailsApplication.config.grailsflow.events.mail.enabled
        if (enabled != null && enabled instanceof String) {
            enabled = BooleanUtils.toBoolean(enabled)
        }
        def mailAddress = grailsApplication.config.grailsflow.events.mail.address
        def mailHost = grailsApplication.config.grailsflow.events.mail.host
        def mailAccount = grailsApplication.config.grailsflow.events.mail.account
        def mailPassword = grailsApplication.config.grailsflow.events.mail.password

        render(view: 'emailConfiguration',
            model:  [enabled: enabled, mailAddress: mailAddress,
                    mailHost: mailHost, mailAccount: mailAccount, mailPassword: mailPassword])
    }

    def save = {
        grailsApplication.config.grailsflow.events.mail.enabled = params.enabled
        grailsApplication.config.grailsflow.events.mail.address = params.mailAddress
        grailsApplication.config.grailsflow.events.mail.host = params.mailHost
        grailsApplication.config.grailsflow.events.mail.account = params.mailAccount
        grailsApplication.config.grailsflow.events.mail.password = params.mailPassword

        redirect(action: "emailConfiguration")
    }

    def back = {
        redirect(controller: "schedulerDetails", action: "showSchedulerDetails")
    }


}
