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

import com.jcatalog.grailsflow.model.process.ProcessNode

/**
 * Class for representing data of dynamic variable
 */
class DynamicProcessVariableDetails {

    static final String DYNAMIC_VARIABLE_NAME_SEPARATOR = '_'

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

    /**
     * @return unique identifier of process node for which this dynamic variable is belongs
     */
    Long getNodeKey() {
        Objects.requireNonNull(dynamicName, "Parameter 'dynamicName' can't be null")

        String[] dynamicVariableNameParts = dynamicName.split(DYNAMIC_VARIABLE_NAME_SEPARATOR)
        String rawNodeKey = dynamicVariableNameParts[1]

        return rawNodeKey as Long
    }

    /**
     * @param node instance of process node
     * @return true if current dynamic variable is belongs to the process node, otherwise - returns false value
     */
    boolean isBelongsToProcessNode(ProcessNode node) {
        nodeKey == node?.id
    }
}