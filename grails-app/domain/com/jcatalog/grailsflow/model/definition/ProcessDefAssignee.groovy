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
 * ProcessDefAssignee class describes the assignee specified for the process and process nodes in process definitions.
 * When the user starts process, ProcessDefAssignees become initial ProcessAssignee
 * of appropriate BasicProcess. (see also ProcessAssignee domain class)
 *
 * @author Stephan Albers
 * @author Maria Voitovich
 */
class ProcessDefAssignee {
    String assigneeID
    ProcessNodeDef processNodeDef

    static constraints = {
        assigneeID(nullable:false)
        processNodeDef(nullable:true)
    }

    static mapping = {
      processDef index: "IDX_PROCESS_DEF_ASSIGNEE_1"
      assigneeID index: 'IDX_PROCESS_DEF_ASSIGNEE_2'
      processNodeDef index: 'IDX_PROCESS_DEF_ASSIGNEE_3'
    }

    static belongsTo = [ processDef: ProcessDef ]

    def beforeDelete = {
      if (processNodeDef) {
        processNodeDef.removeFromAssignees(this)
      }
    }

}
