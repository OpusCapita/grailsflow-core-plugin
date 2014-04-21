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

package com.jcatalog.grailsflow.status

/**
 * Status enumeration for representing all possible node status values.
 *
 * "ACTIVATED"  -- the entry was activated;
 *
 * "RUNNING"  -- the entry is currently executing;
 *
 * "SUSPENDED" -- the entry was suspended (eg. the node type is 'Wait')
 *
 * "AWAIT_CALLBACK" - the entry was called asynchronous;
 *
 * "PENDING" -- AndJoin node was created but not all incoming transitions are complete
 *
 * "KILLED"  (final) -- the entry was killed;
 *
 * "COMPLETED" (final) -- the entry was completed(eg. the node was finished successfully)
 *
 * "FORWARDED" (final) -- node execution was forwarded to another assignees;
 *
 * "STOPPED" (final) -- node execution was stopped cause another branch already reached OrJoin node;
 *
 * @author July Antonicheva
 */

enum NodeStatusEnum {
    ACTIVATED("ACTIVATED"),
    RUNNING("RUNNING"),
    SUSPENDED("SUSPENDED"),
    AWAIT_CALLBACK("AWAIT_CALLBACK"),
    PENDING("PENDING"),
    COMPLETED("COMPLETED"),
    KILLING("KILLING"),
    KILLED("KILLED"),
    STOPPED("STOPPED"),
    FORWARDED("FORWARDED")

    private final String value

    private NodeStatusEnum(String value) {
        this.value = value
    }

    Boolean isFinal() {
        value in [NodeStatusEnum.KILLED.value(), NodeStatusEnum.COMPLETED.value(),
                  NodeStatusEnum.FORWARDED.value(), NodeStatusEnum.STOPPED.value()]
    }

    String value() { value }
}