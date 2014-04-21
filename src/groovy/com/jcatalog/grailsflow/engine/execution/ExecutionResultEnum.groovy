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
 
package com.jcatalog.grailsflow.engine.execution;

/**
 * ExecutionResultEnum represents results of sending event.
 *
 *
 * Result codes of sending event (return INT value):
 * return 0 - event was sent successfully
 * return 1 - process with processID was not found
 * return 2 - process does not have a node with nodeID
 * return 3 - node has no appropriate transition for this event
 * return 4 - there is no node with the requested nodeID and processKey
 *            or the node was completed already
 * return 5 - if impossible to get process instance from builder object
 * return 6 - process/node was killed
 * return 7 - node execution failed with exception
 * return 8 - impossible to create new thread for execution
 * return 9 - execution was interrupted by kill operation
 *
 * @author July Antonicheva
 */

enum ExecutionResultEnum {
    EXECUTED_SUCCESSFULLY(0),
    NO_PROCESS_FOR_PROCESSID(1),
    NO_NODEID_IN_PROCESS(2),
    NO_TRANSITION_FOR_EVENT(3),
    NO_NODE_OR_NODE_COMPLETED(4),
    NO_PROCESS_DEFINITION(5),
    PROCESS_KILLED(6),
    FAILED_WITH_EXCEPTION(7),
    NO_THREAD_FOR_EXECUTION(8),
    INTERRUPTED_BY_KILLING(9)

    private final Integer value

    private ExecutionResultEnum(Integer value) {
        this.value = value
    }

    Integer value() { value }
}
