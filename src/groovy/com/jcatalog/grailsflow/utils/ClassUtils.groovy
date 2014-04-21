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
package com.jcatalog.grailsflow.utils;

import java.lang.reflect.Modifier

class ClassUtils {

    def static getDomainClassProperties(def domainClass) {
  		return domainClass?.declaredFields.findAll { field ->
  		  !field.synthetic &&
				!Modifier.isStatic(field.modifiers) &&
				!Modifier.isTransient(field.modifiers) &&
				!['id', 'version', 'errors'].contains(field.name)
			  }.collect() { it.name }
    }

    // TODO: reimplement this method
    def static getAllDomainClassProperties(def domainClass) {
        def properties = new HashSet()
        def clazz = domainClass
        while(clazz && clazz != Object.class) {
          properties.addAll(clazz.declaredFields.findAll { field ->
                  !field.synthetic &&
                  !Modifier.isStatic(field.modifiers)
                }.collect() { it.name }
          )
          try {
            def transients = clazz.transients
            if (transients) {
              properties.addAll(transients)
            }
          } catch (MissingPropertyException e) {
            // do nothing
          }
          clazz = clazz.getSuperclass()
        }
        return properties
    }

    def static getActionClassProperties(def actionClass) {
        def properties = new HashSet()
        def clazz = actionClass
        while(clazz && clazz != Object.class) {
          properties.addAll(clazz.declaredFields.findAll { field ->
                  !Modifier.isStatic(field.modifiers) &&
                   Modifier.isPublic(field.modifiers)       
              }.collect() { it.name }
          )
          clazz = clazz.getSuperclass()
        }
        return properties

    }

}