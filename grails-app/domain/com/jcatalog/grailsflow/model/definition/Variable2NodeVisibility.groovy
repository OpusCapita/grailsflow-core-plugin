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
package com.jcatalog.grailsflow.model.definition

/**
 * Variable2NodeVisibility class.
 *
 */
class Variable2NodeVisibility {
    Integer visibilityType
    String visibilityDesc // TODO: do we need this?

    static belongsTo = [ node: ProcessNodeDef, variable: ProcessVariableDef ]
    
    static constraints = {
    }

    static mapping = {
      variable cascade: "save-update"
      node cascade: "save-update"
    }

    def beforeDelete = {
      variable?.removeFromVariable2NodesVisibility(this)
      node?.removeFromVariables2NodeVisibility(this)
    }

}
