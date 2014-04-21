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

import javax.mail.Message


/**
 * EventEmailParser is common interface for parsing email to get parameters for sending event to the process.
 *
 * @author Maria Voitovich
 */
interface EventEmailProcessor {
    
    SendEventParameters parseEventMessage(Message message)
    
    void processSendEventResult(Message message, SendEventParameters parameters, int result)
  
}