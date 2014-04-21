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
package com.jcatalog.grailsflow.actions

import com.jcatalog.grailsflow.process.Link
import com.jcatalog.grailsflow.process.Document

import java.text.SimpleDateFormat

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * ActionUtils is a helper class that gives possibility to convert
 * String object representation to necessary type. Type is represented
 * as a simple class name.
 *
 * Following types are supported:
 * 1. Boolean
 * 2. Date
 * 3. Double
 * 4. Integer
 * 5. Long
 * 6. String
 * 7. Document
 * 8. Link
 * 9. List
 * 10. Map
 *
 * @author Stephan Albers
 * @author July Karpey
 */

class ActionUtils {
    static final Log log = LogFactory.getLog(getClass())
    static final def SUPPORTED_COMPARATORS =  ['>', '<', "=", "!=", "nullable", "contains"]

    def static getValueOfType(def type, String value) {
       if (!value || !type) return null
       Object o = null
       try {
           switch (type) {
              case 'Boolean':
                  o = new Boolean(value)
                  break
              case 'Date':
                  def sdf = new SimpleDateFormat()
                  sdf.applyPattern("yyyy.M.dd")
                  o = sdf.parse(value)
                  break
              case 'Double':
                  o = Double.valueOf(value)
                  break
              case 'Integer':
                  o = Integer.valueOf(value)
                  break
              case 'Long':
                  o = Long.valueOf(value)
                  break
              case 'String':
                  o = String.valueOf(value)
                  break
              case 'Document':
                  o = new Document(documentUrl: value)
                  break
              case 'Map':
                  GroovyShell gs = new GroovyShell()
                  o = gs.evaluate(value)
                  break
              case 'List':
                  if (value.indexOf("[") == -1 && value.indexOf("]") == -1) {
                      o = [value]
                  } else {
                      GroovyShell gs = new GroovyShell()
                      o = gs.evaluate(value)
                  }
                  break
              case 'Link':
                  GroovyShell gs = new GroovyShell()
                  def properties = gs.evaluate(value)
                  o = new Link(path: properties.path, description: properties.description)
                  break                                                                        
              default:
                  log.error("Unknown type is specified.")
           }
       } catch (Exception e) {
           log.error("Error in conversion is occurred: value=${value.inspect()} of type '${type}'", e)
       }
        
        return o
    }
}