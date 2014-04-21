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

/**
 * ProcessAssignee class describes the assignee for the process nodes during process execution.
 * When the user starts process, ProcessDefAssignees become initial ProcessAssignee
 * for started process (see also ProcessDefAssignees domain class). When user change assignees of the nodes
 * during process execution (for example using UpdateAssignees() action) ProcessAssignee values for appropriate process are updated.
 *
 * When ProcessManagerService initializes new ProcessNode, current set of ProcessAssignees with appropriate processID and nodeID
 * specifies restricts Nodes available for user (Worklist). 
 *
 * @author Stephan Albers
 * @author Maria Voitovich
 */
class ProcessAssignee {
    String nodeID
    String assigneeID

    static constraints = {
        assigneeID(nullable:false)
        nodeID(nullable:false)
    }
    
    static belongsTo = [ process: BasicProcess ]
    
    static mapping = {
      nodeID index: 'IDX_PROCESS_ASSIGNEE_2'
    }

}
