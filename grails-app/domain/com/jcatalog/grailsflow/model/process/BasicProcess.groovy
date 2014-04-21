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
 * Process class is the base class for all process definitions.
 * All processes supported by the Process Manager can be stored
 * in BasicProcess table according to process definition type.
 *
 * Each process should have definition for processTypeID.
 * The static fields for transitions and nodes should be described too.
 *
 * TODO: Add mechanism of constraints for Process subclasses like Grails has
 * for Domain classes.
 *
 * TODO: Process should contain the list of attachments and possibly
 * the constraints to nodes and variables.
 *
 * TODO: Perhaps BasicProcess should be renamed to Flow
 * (in this case all subclasses should ends with 'Flow' word)
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class BasicProcess {
    String type

    String appGroupID

    FlowStatus status
    String description

    Date createdOn
    String createdBy

    Date lastModifiedOn
    String lastModifiedBy

    Date finishedOn
    String finishedBy
  
    static hasMany = [ nodes: ProcessNode,
                       variables: ProcessVariable,
                       assignees: ProcessAssignee ]

    static constraints = {
        appGroupID(nullable:true)
        description(length:50, nullable:true)
        finishedOn(nullable:true)
        finishedBy(nullable:true)
    }
    
    static mapping = {
        status index: 'IDX_BASIC_PROCESS_1'
        nodes cascade: "all-delete-orphan"
        variables cascade: "all-delete-orphan"
        assignees cascade: "all-delete-orphan"
    }

    def beforeInsert = {
        String applicationID = this.domainClass.grailsApplication
            .mainContext.appExternalID?.toString()
        appGroupID = applicationID
    }
}
