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

package com.jcatalog.grailsflow.cluster

import com.jcatalog.grailsflow.model.process.BasicProcess

/**
 * GrailsflowLock domain class is used to store locks for executed automatically nodes
 *
 * @author Stephan Albers
 * @author July Antonicheva
 */
class GrailsflowLock {
    String clusterName
    BasicProcess process
    String nodeID
    Date lockedOn

    static constraints = {
        process(unique: true)
    }

    static mapping = {
        process index:'IDX_PROCESS_NODE_LOCK'
        nodeID index:'IDX_PROCESS_NODE_LOCK'
    }

    String toString() {
        return "GrailsflowLock(${this.id}): process[${this.process.id}], nodeID[${this.nodeID}]";
    }
}
