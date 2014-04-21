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

package com.jcatalog.grailsflow.model.view
 
/**
 * ExternalSearchObject view displays link to external search page for selecting variable value that is Domain object.
 * 
 * - searchUrl is URL that will be called to open search page.
 *    To select object search page should call window.opener.callbackSearch${params.callbackFunctionName}() function
 *   and pass here Map of propertyName -> propertyValue values (for object identity 'ident' key should be used). 
 *
 * - displayKey is the name of property that will be used to represent object on the UI
 * - additionalFields is string of comma-separated names of object properties that will be additionally shown on the UI  
 *   
 *
 * @author Maria Voitovich
 */
class ExternalSearchObjectView extends VariableView {
  String displayKey
  String searchUrl
  String additionalFields

  static constraints = {
    displayKey(nullable: true)
    additionalFields(nullable: true)
  }

}
