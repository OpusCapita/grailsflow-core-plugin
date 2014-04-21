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

/**
 * TranslationsSectionBuilder class is used to get process, nodes and variables translations 
 * from translations closure of process definition file.
 * The result of TranslationsSectionBuilder work is Map of objectName -> translations
 *
 * TODO: separate labels and descriptions
 * 
 * @author Maria Voitovich
 */
class TranslationsSectionBuilder extends AbstractSectionBuilder {
    private def translations 
    
    public def getTranslations(){
      return translations
    }

    // TODO: rename section to translations
    public List<String> getSupportedSections() {
      return [ "descriptions" ]
    }
    
    public TranslationsSectionBuilder(def process) {
        translations = [:]
        build(process)
    }
    
    protected Object createNode(Object name, Map attributes) {
        log.debug("Building labels and descriptions for process, node or variable with name ${name} from translations ${attributes}")
        translations[name] = attributes
        return null
    }

    // TODO: use this method to separate labels and descriptions
    private void addTranslation(Map map, String keyPrefix, String key, String value) {
      if (key.startsWith(keyPrefix)) {
        def lang = StingUtils.substringAfter(key, keyPrefix);
        if (lang && lang.length > 0 && lang.length < 3) {
          log.debug("Adding translation ${value} for language ${lang} to ${keyPreffix}")
          map.put(lang, value)
        } else {
          log.warn("Incorrect language ${lang} for translation key ${key}. Translation is skipped.")
        }
      }
    }

    protected Object createNode(Object name, Map attributes, Object value) {
      log.warn("Incorrect DSL syntax at node ${name} with attributes ${attributes} and value ${value.inspect()}")
      return null
    }

    protected void setParent(Object parent, Object child) {
      log.warn("Set parent for ${parent} and child ${child} should not occur for ${this.class.name}")
    }

    protected Object createNode(Object name) {
      log.warn("Incorrect DSL syntax at node ${name}")
      return null
    }

    protected Object createNode(Object name, Object value){
      log.warn("Incorrect DSL syntax at node ${name} with value ${value}")
      return null
    }

}
