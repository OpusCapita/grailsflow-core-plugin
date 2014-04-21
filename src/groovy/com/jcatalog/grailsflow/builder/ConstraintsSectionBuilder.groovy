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

import java.text.SimpleDateFormat
import java.text.ParseException

/**
 * ConstraintsSectionBuilder class is used to get process, nodes and variables translations 
 * from constraints closure of process definition file.
 * The result of ConstraintsSectionBuilder work is Map of variableName -> constraints
 *
 * 
 * @author Maria Voitovich
 */
class ConstraintsSectionBuilder extends AbstractSectionBuilder {
    private def constraints 
    
    public def getConstraints(){
      return constraints
    }

    public List<String> getSupportedSections() {
      return [ "constraints" ]
    }
    
    public ConstraintsSectionBuilder(def process) {
        constraints = [:]
        build(process)
    }
    
    protected Object createNode(Object name, Map attributes) {
        log.debug("Building constraints for variable ${name} from constraints ${attributes}")
        constraints[name] = [:]

        def sdf = new SimpleDateFormat('yyyy.MM.dd')

        attributes.each() { key, value ->
          if (key == 'required') {
            constraints[name].put(key, Boolean.valueOf(value))
          } else if (key == 'validFrom' || key == 'validTo') {
            try {
              constraints[name].put(key, sdf.parse(value))
            } catch (ParseException e) {
              log.error("Cannot parse date ${value?.inspect()}. Date should be in 'yyyy.MM.dd' format")
            }
          } else {
            log.warn("Incorrect DSL syntax at node ${name}. Attribute ${key} is not allowed.")
          }
        }
        return null
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
