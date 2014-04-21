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
 * Status enumeration for representing all possible process status values.
 *
 * "ACTIVATED"  -- the entry was activated (eg. status for started process)
 *
 * "SUSPENDED" -- the entry was suspended (eg. the process node type is 'Wait')
 *
 * "KILLED" -- the entry was killed (eg. the process was killed or deleted by smb. from UI)
 *
 * "COMPLETED"  -- the entry was completed (eg. the process was finished successfully)
 *
 * @author July Antonicheva
 */

enum ProcessStatusEnum {
    ACTIVATED("ACTIVATED"),
    SUSPENDED("SUSPENDED"),
    COMPLETED("COMPLETED"),
    KILLING("KILLING"),
    KILLED("KILLED")

    private final String value

    private ProcessStatusEnum(String value) {
        this.value = value
    }

    Boolean isFinal() {
        value in [ProcessStatusEnum.KILLED.value(), ProcessStatusEnum.COMPLETED.value()]
    }

    String value() { value }
}