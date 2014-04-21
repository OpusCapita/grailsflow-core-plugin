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
package com.jcatalog.grailsflow.model.graphics

import com.jcatalog.grailsflow.model.process.BasicProcess

/**
 * ProcessNodePosition class represents node on graphical diagram.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class ProcessNodePosition {
    String  nodeID
    String  actionType
    String  knotType
    String  statusType
    Long    dueDate

    Integer startX
    Integer startY
    Integer width
    Integer height
    
    static belongsTo = [ process: BasicProcess ]

    static constraints = {
        statusType(nullable:true)
        knotType(nullable:true)
        dueDate(nullable:true)
        startX(nullable:true)
        startY(nullable:true)
        width(nullable:true)
        height(nullable:true)
    }

    static mapping = {
      nodeID index: 'IDX_PROCESS_NODE_POSITION_1'
    }    

}