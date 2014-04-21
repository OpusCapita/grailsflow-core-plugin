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

class ConstantUtils {
    public static int INVISIBLE  = 0
    public static int READ_ONLY = 1
    public static int WRITE_READ = 2
    public static int REQUIRED = 3

    public static String EDITOR_AUTO  = "AUTO"
    public static String EDITOR_MANUAL = "MANUAL"
    
    public static String NODE_TYPE_ACTIVITY = "Activity"
    public static String NODE_TYPE_WAIT = "Wait"
    public static String NODE_TYPE_FORK = "Fork"
    public static String NODE_TYPE_ORJOIN = "OrJoin"
    public static String NODE_TYPE_ANDJOIN = "AndJoin"
    

    static Map getVisibilityTypes() {
	      return [0: "INVISIBLE",
	              1: "READ_ONLY",
	              2: "WRITE_READ",
                  3: "REQUIRED"]
    }

    static String getVisibility(int typeIndex) {
        return ConstantUtils.visibilityTypes[typeIndex]
    }

    static Collection getNodeTypes() {
      return getConstants("NODE_TYPE_").collect() { field ->
        field.get(null)
      }
    }

    static Map<String, String> getEditorTypes() {
       def editorTypes = [:]
       getConstants("EDITOR_").each { field ->
           editorTypes.put(field.get(null), field.name)
       }
	     return editorTypes
    }

    static String getEditorType(String type) {
        return ConstantUtils.editorTypes[type]
    }
    
    static Collection getConstants(String namePreffix) {
      return ConstantUtils.class.declaredFields.findAll { field ->
        !field.synthetic &&
        Modifier.isStatic(field.modifiers) &&
        field.name.startsWith(namePreffix)
        }.sort() { it.name }
    }

}