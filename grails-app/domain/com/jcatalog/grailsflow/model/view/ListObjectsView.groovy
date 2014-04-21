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
 * ListObjects view displays select box for selecting variable value that is Domain object.
 *  
 * - displayKey is the name of property that will be used to represent object on the UI
 * - restriction is 'where' condition (without 'where' word) that will be used for selecting list of Domain objects.
 *
 * @author Maria Voitovich
 */
class ListObjectsView extends VariableView {
  String displayKey
  String restriction
  
  static constraints = {
    displayKey(nullable: true)
    restriction(nullable: true)
  }

}
