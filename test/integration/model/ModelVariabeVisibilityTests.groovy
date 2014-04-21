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

package model;

import com.jcatalog.grailsflow.model.definition.*
import com.jcatalog.grailsflow.model.view.*
import com.jcatalog.grailsflow.utils.ConstantUtils
import com.jcatalog.grailsflow.model.process.BasicProcess
import com.jcatalog.grailsflow.model.process.ProcessNode
import com.jcatalog.grailsflow.model.graphics.ProcessNodePosition
import com.jcatalog.grailsflow.model.process.ProcessAssignee
import com.jcatalog.grailsflow.model.process.ProcessVariable

/**
 * Tests for Variable2NodeVisibility and dependent objects.
 *
 *
 * @author Maria Voitovich
 */
class ModelVariabeVisibilityTests extends GroovyTestCase {

    protected void setUp() {
        BasicProcess.withSession { session->
            ProcessNode.list()*.delete()

            ProcessNodePosition.list()*.delete()
            ProcessNodeDef.list()*.delete();

            ProcessAssignee.list()*.delete()

            ProcessVariable.list()*.delete()
            BasicProcess.list()*.delete()
            session.flush()
        }
    }


    void createProcessWithNodeAndVariable() {
        def process = new ProcessDef()
        process.processID = "TestProcess"
        process.save()
        
        def firstNode = new ProcessNodeDef(nodeID:"Begin",
                            dueDate:20000, type:"Wait", isFinal: false,
                            editorType: ConstantUtils.EDITOR_AUTO)
        process.addToNodes(firstNode)

        def variable1 = new ProcessVariableDef(type:"String", name:"string_var")
        process.addToVariables(variable1)
        
        assert process.save(flush: true)
    }

    void createVariableVisibility() {
        def firstNode = ProcessNodeDef.findWhere(nodeID: "Begin")
        def variable1 = ProcessVariableDef.findWhere(name: "string_var")

        def visibility = new Variable2NodeVisibility(visibilityType: 1, visibilityDesc: "desc")
        visibility.variable = variable1
        variable1.addToVariable2NodesVisibility(visibility)
        visibility.node = firstNode
        firstNode.addToVariables2NodeVisibility(visibility)

        assert firstNode.save(flush: true)
    }
    
    void cleanup() {
        def process = ProcessDef.findWhere(processID: "TestProcess")
        process.delete(flush: true)
    }
    
    void deleteNode(def node) {
        node.removeFromAssociations()
        node.delete(flush: true)
    }

    void deleteVariable(def variable){
        variable.removeFromAssociations()
        variable.delete(flush: true)
    }

    void testDeletionOfNode() {
        createProcessWithNodeAndVariable()
        createVariableVisibility()
        
        def fromNode = ProcessNodeDef.findWhere(nodeID: "Begin")
        deleteNode(fromNode)
        
        assert !ProcessNodeDef.list()
        assert !Variable2NodeVisibility.list()
        def variables = ProcessVariableDef.list() 
        assert variables
        assert 1 == variables.size()
        cleanup()
    }
    
    void testDeletionOfVariable() {
        createProcessWithNodeAndVariable()
        createVariableVisibility()

        def variable = ProcessVariableDef.findWhere(name: "string_var")
        deleteVariable(variable)
        
        assert !ProcessVariableDef.list()
        assert !Variable2NodeVisibility.list()
        def nodes = ProcessNodeDef.list() 
        assert nodes
        assert 1 == nodes.size()
        cleanup()
    }

}
