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
package com.jcatalog.grailsflow.process.script

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.springframework.beans.factory.InitializingBean
import com.jcatalog.grailsflow.workarea.ScriptsProvider

import java.util.regex.Pattern

class GrailsflowProcessScriptProvider implements ProcessScriptProvider, InitializingBean {
    static final String PROCESS_SUFFIX = "Process.groovy"
    static final Pattern PROCESS_SCRIPT_FILENAME_PATTERN = ~/(.+)${PROCESS_SUFFIX}/
    protected Log log = LogFactory.getLog(getClass())
    ScriptsProvider scriptsProvider
    String processesPath

    public void afterPropertiesSet() throws Exception {
      if (processesPath == null) {
          throw new Exception("processesPath property must be set for GrailsflowProcessScriptProvider.")
      }
      if (scriptsProvider == null) {
          log.error("scriptsProvider bean is not configured in Spring context.")
          throw new Exception("scriptsProvider property must be set for GrailsflowProcessScriptProvider.")
      }
    }

    public Collection<String> listProcessScripts() {
      def paths = scriptsProvider.getResourceFile(processesPath)
      if (!paths.exists()) {
          paths.mkdirs()
          return []
      }
      def types = []
      if (paths.isDirectory()) {
          paths.eachFileMatch(PROCESS_SCRIPT_FILENAME_PATTERN) { file ->
            if (!file.isDirectory()) {
              def matcher = file.name =~ PROCESS_SCRIPT_FILENAME_PATTERN
              types << matcher[0][1]
            }
          }
      } else {
        log.error("Process folder '${processesPath}' is not a directory.")
      }
      return types
    }

    private File getProcessScriptFile(String name) {
      def paths = scriptsProvider.getResourceFile(processesPath)
      def scriptFileName = name+PROCESS_SUFFIX
      return new File(paths, scriptFileName.toString())

    }

    public ProcessScript readProcessScript(String processType) {
      try {
        def scriptFile = getProcessScriptFile(processType)
        if (!scriptFile.exists()) {
            log.error("Script File ${scriptFile.name} was not found.")
            return null
        }
        def source = scriptFile.getText("UTF-8")
        def date = new Date(scriptFile.lastModified())
        return new ProcessScript(processType, source, date)
      } catch (Exception e){
        log.error("Cannot read process script for type ${processType}", e)
        return null
      }
    }

    boolean writeProcessScript(String processType, String source) {
      try {
        def scriptFile = getProcessScriptFile(processType)
        scriptFile.write(source, "UTF-8")
        return true
      } catch (Exception e){
        log.error("Cannot write process script for type ${processType}", e)
        return false
      }
    }

    boolean deleteProcessScript(String processType) {
      def scriptFile = getProcessScriptFile(processType)
      return scriptFile.delete()
    }

}