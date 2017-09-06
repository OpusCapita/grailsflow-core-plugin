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

package com.jcatalog.grailsflow.bean

/**
 * Class for representing data of dynamic variable
 */
class DynamicProcessVariableDetails {

    // variable name
    String dynamicName

    // variable value with type one of com.jcatalog.grailsflow.model.process.ProcessVariable.getTypes()
    def value

    // nodeID of related process node
    String createdOnNodeID

    // node owner
    String createdBy

    // node finished on
    Date createdOn
}