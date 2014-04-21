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

package com.jcatalog.grailsflow.builder;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import com.jcatalog.grailsflow.utils.NameUtils;


/**
 * AbstractSectionBuilder class is base class for building objects from closures of process definition file.
 * The result of builder work is Map of sectionName -> section objects.
 *
 * @author Maria Voitovich
 */
abstract class AbstractSectionBuilder extends BuilderSupport {
    protected Log log = LogFactory.getLog(getClass())
    protected def currentSection

    abstract public List<String> getSupportedSections();

    protected void build(def process) {
        def processClass = process.class

        def processMethodsNames = processClass.getMethods()*.name
        
        def sections = getSupportedSections() 

        sections?.each() { section ->
            def closureName = NameUtils.upCase(section)
            currentSection = section

            try {
                def methodName = "get${closureName}"
                if (processMethodsNames.contains(methodName.toString())) {
                    log.debug("Building objects from ${section} section of process class ${processClass.name}.")
                    def closureMethod = processClass.getMethod(methodName)
                    def closure  = closureMethod.invoke(process, null)
                    if (closure) {
                        closure.setDelegate(this)
                        closure.call()
                    }
                } else {
                  log.debug("Process class ${processClass.name} does not have ${section} section.")
                } 
            } catch (NoSuchMethodException noMethodExc) {
                log.error("Process class ${processClass.name} does not have ${section} section.")
            } catch (Exception e) {
                log.error("Problems occurred while getting process properties", e)
            }
        }
    }

}
