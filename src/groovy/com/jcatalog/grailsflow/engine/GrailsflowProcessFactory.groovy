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
package com.jcatalog.grailsflow.engine

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.springframework.beans.factory.InitializingBean

import com.jcatalog.grailsflow.process.script.ProcessScript

class GrailsflowProcessFactory implements ProcessFactory, InitializingBean {
    protected Log log = LogFactory.getLog(getClass())
    def processScriptProvider

    public void afterPropertiesSet() throws Exception {
      if (processScriptProvider == null) {
          throw new Exception("processScriptProvider property must be set for ${this.getClass()} bean.")
      }
    }

    
    Collection<String> getProcessTypes() {
        return processScriptProvider.listProcessScripts()
    }

    def getProcessClassForName(def processType) {
        def builder = new ProcessBuilder(getProcessScript(processType))
        if (builder.processClass != null && builder.errors.size() == 0) {
          log.debug("Process class for process type ${processType} is build successfully.")
          return builder.processClass 
        } else {
          log.error("Cannot create processClass for process type ${processType}.")
          builder.errors.each() {
            log.error(it)
          }
          return null
        }
    }

    def removeProcessClass(def processType) {
       // do nothing
    }
    
    protected ProcessScript getProcessScript(def processType) {
        return processScriptProvider.readProcessScript(processType) 
    } 

}