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
 * Process Definition class is used for creating Process Definiton in
 * ProcessEditor - for creating(changing) and storing definition values from UI
 * editors and then generating script.
 *
 *
 * @author Stephan Albers
 * @author July Karpey
 * @author Maria Voitovich
 */
class ProcessDef {

    String processID
    Date validFrom
    Date validTo

    // Variables are ordered
    List variables

    // Nodes are ordered
    List nodes

    Map label = [:]
    Map description = [:]

    static hasMany = [ nodes: ProcessNodeDef,
                       variables: ProcessVariableDef,
                       assignees: ProcessDefAssignee ]

    static constraints = {
      validFrom(nullable:true)
      validTo(nullable:true)
    }

    static mapping = {
      processID index: "IDX_PROCESS_DEF_1"
      nodes cascade: "all,delete-orphan"
      variables cascade: "all,delete-orphan"
      assignees cascade: "all,delete-orphan"
      label indexColumn:[name:"language", type:String, length:5],joinTable:[key:'process_def_id' ,column:'label'],length:255
      description indexColumn:[name:"language", type:String, length:5],joinTable:[key:'process_def_id' ,column:'description'],length:255
    }

    static transients = [ "startNode", "processAssignees" ]

    public ProcessNodeDef getStartNode() {
        return nodes?.get(0)
    }

    public Collection<ProcessDefAssignee> getProcessAssignees() {
      return assignees?.findAll() { it.processNodeDef == null }
    }
}
