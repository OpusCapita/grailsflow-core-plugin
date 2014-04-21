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
package com.jcatalog.grailsflow.model.process

import org.hibernate.type.YesNoType

/**
 * Status class describes the current condition of entry.
 *
 * The following values of 'statusID' are supported:
 *    statusID = "ACTIVATED" - the entry was activated
 *                             (eg. status for started process
 *                              or status for current process node)
 *    statusID = "SUSPENDED" - the entry was suspended
 *                             (eg. the process node type is 'Wait')
 *    statusID = "KILLED" - the entry was killed
 *                          (eg. the process was killed or deleted by smb. from UI)
 *    statusID = "COMPLETED" - the entry was completed
 *                             (eg. the process was finished successfully)
 *    statusID = "AWAIT_CALLBACK" - the entry was called asynchronous
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class FlowStatus {
    String statusID
    String description
    boolean isFinal = false

    static mapping = {
      statusID index: 'IDX_FLOW_STATUS_1'
      columns {
          isFinal type:YesNoType
      }
    }

    boolean equals(Object o) {
      if (o == null) return false
      if (!(o instanceof FlowStatus)) return false
      return this.statusID.equals(o.statusID)
    }
}
