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
import com.jcatalog.grailsflow.model.graphics.ProcessNodeDefPosition
import com.jcatalog.grailsflow.model.process.ProcessNode
import com.jcatalog.grailsflow.model.graphics.ProcessNodePosition

/**                                                                                                                                                                                 ModelProcessDefTests
 * Tests for ProcessDef and dependent objects.
 *
 *
 * @author Maria Voitovich
 */
class ModelProcessDefTests extends GroovyTestCase {

    void setUp(){
        fullCleanUp();
    }

    void fullCleanUp(){
        BasicProcess.withSession { session->
            ProcessNodeDefPosition.list()*.delete()
            ProcessNodePosition.list()*.delete()
            ProcessNode.list()*.delete()
            BasicProcess.list()*.delete()
            ProcessDef.list()*.delete()
            session.flush()
        }
    }

    ProcessDef createProcess() {
        def process = new ProcessDef()
        process.processID = "TestProcess"
        process.label = [en: "Test Process"]
        process.description = [en: "Description", de: "Beschreibung"]
        
        return process
    }
        
    void checkProcess () {
        def process = ProcessDef.findWhere(processID: "TestProcess")
        assert process
        assert process.label
        assert 1 == process.label.size()
        assert "Test Process" == process.label['en']
        assert process.description
        assert 2 == process.description.size()
        assert "Description" == process.description['en']
        assert "Beschreibung" == process.description['de']
    }
    
    void createNodes(process) {

        def firstNode = new ProcessNodeDef(nodeID:"Begin",
                            dueDate:20000, type:"Wait", isFinal: false,
                            editorType: ConstantUtils.EDITOR_AUTO)
        def statement1 = new ActionStatement(content: 'return "ok"')
        firstNode.addToActionStatements(statement1)        
        process.addToNodes(firstNode)

        def secondNode = new ProcessNodeDef(nodeID:"End",
            dueDate:20000, type:"Activity", isFinal: true,
            editorType: ConstantUtils.EDITOR_AUTO)
        def statement2 = new ActionStatement(content: "Log(message: 'log message')")
        secondNode.addToActionStatements(statement2)
        process.addToNodes(secondNode)
    }
    
    void checkNodes() { 
        def process = ProcessDef.findWhere(processID: "TestProcess")
        def firstNode = ProcessNodeDef.findWhere(processDef: process, nodeID: "Begin")
        def secondNode = ProcessNodeDef.findWhere(processDef: process, nodeID: "End")
        
        // check process
        assert process.nodes
        assert 2 == process.nodes.size()
        
        // check first node
        assert firstNode
        assert process.nodes.contains(firstNode)
        assert firstNode.actionStatements
        assert 1 == firstNode.actionStatements.size()
        
        // check second node
        assert secondNode
        assert process.nodes.contains(secondNode)
        assert secondNode.actionStatements
        assert 1 == secondNode.actionStatements.size()
    }
    
    void createVariables(process) {
        def variable1 = new ProcessVariableDef(type:"String", name:"string_var")
        def variable2 = new ProcessVariableDef(type:"Integer", name:"integer_var", view: new SimpleView())
        def variable3 =new ProcessVariableDef(type:"Date", name:"date_var", view: new DateView())
        process.addToVariables(variable1)
        process.addToVariables(variable2)
        process.addToVariables(variable3)
    }
        
    void checkVariables(){
        def process = ProcessDef.findWhere(processID: "TestProcess")
        def variable1 = ProcessVariableDef.findWhere(processDef: process, name: "string_var")
        def variable2 = ProcessVariableDef.findWhere(processDef: process, name: "integer_var")
        def variable3 = ProcessVariableDef.findWhere(processDef: process, name: "date_var")

        // check process
        assert process.variables
        assert 3 == process.variables.size()

        // check variable1
        assert variable1
        assert variable1 == process.variables[0]
        assert "String" == variable1.type
        assert !variable1.view

        // check variable2
        assert variable2
        assert variable2 == process.variables[1]
        assert "Integer" == variable2.type
        assert variable2.view
        assert "simpleView" == variable2.view.type

        // check variable3
        assert variable3
        assert variable3 == process.variables[2]
        assert "Date" == variable3.type
        assert variable3.view
        assert "dateView" == variable3.view.type
    }
    
    void createTransitions(process) {
        def firstNode = process.nodes.find() { it.nodeID == "Begin" }
        def secondNode = process.nodes.find() { it.nodeID == "End" }
        def transition = new ProcessTransitionDef()
        transition.event = "ok"
        transition.fromNode = firstNode
        transition.toNodes  = [ secondNode ]
        firstNode.addToTransitions(transition)
    }
    
    void checkTransitions() { 
        def process = ProcessDef.findWhere(processID: "TestProcess")
        def firstNode = ProcessNodeDef.findWhere(processDef: process, nodeID: "Begin")
        def secondNode = ProcessNodeDef.findWhere(processDef: process, nodeID: "End")

        // check process
        assert process.startNode
        assert firstNode == process.startNode
        
        // check firstNode
        assert !firstNode.incomingTransitions
        assert firstNode.transitions
        assert 1 == firstNode.transitions.size()

        // check transition
        def transition = firstNode.transitions.find() { true }
        assert "ok" == transition.event
        assert transition.toNodes
        assert 1 == transition.toNodes.size()
        secondNode == transition.toNodes.find() { true }

        // check secondNode
        assert !secondNode.transitions
        assert secondNode.incomingTransitions
        assert 1 == secondNode.incomingTransitions.size()
        assert transition == secondNode.incomingTransitions.find() { true }
    }
    
    void createVariableVisibilities(process) {
        def firstNode = process.nodes.find() { it.nodeID == "Begin" }
        def variable1 = process.variables.find() { it.name == "string_var" }
        def variable2 = process.variables.find() { it.name == "date_var" }

        def visibility1 = new Variable2NodeVisibility(variable: variable1, visibilityType: 2, visibilityDesc: "WRITE_READ")
        firstNode.addToVariables2NodeVisibility(visibility1)
        variable1.addToVariable2NodesVisibility(visibility1)
        def visibility2 = new Variable2NodeVisibility(variable: variable2, visibilityType: 1, visibilityDesc: "READ_ONLY") 
        firstNode.addToVariables2NodeVisibility(visibility2)
        variable2.addToVariable2NodesVisibility(visibility2)
    }
    
    void checkVariableVisibilities() {
        def process = ProcessDef.findWhere(processID: "TestProcess")
        def firstNode = ProcessNodeDef.findWhere(processDef: process, nodeID: "Begin")
        def variable1 = ProcessVariableDef.findWhere(processDef: process, name: "string_var")
        def variable2 = ProcessVariableDef.findWhere(processDef: process, name: "date_var")
        
        // check node
        assert firstNode.variables2NodeVisibility
        assert 2 == firstNode.variables2NodeVisibility.size()
        def vars = firstNode.variables2NodeVisibility.collect() { it.variable }
        assert vars.contains(variable1)
        assert vars.contains(variable2)
        
        // check variable1
        assert variable1.variable2NodesVisibility
        assert 1 == variable1.variable2NodesVisibility.size()
        assert variable1.variable2NodesVisibility.collect { it.node }.contains(firstNode)

        // check variable2
        assert variable2.variable2NodesVisibility
        assert 1 == variable2.variable2NodesVisibility.size()
        assert variable2.variable2NodesVisibility.collect { it.node }.contains(firstNode)
    }
    
    void deleteProcess() {
        def process = ProcessDef.findWhere(processID: "TestProcess")
        
        process.delete(flush: true)

        assert !ProcessDef.list()
        assert !ProcessNodeDef.list()
        assert !ActionStatement.list()
        assert !ProcessTransitionDef.list()
        assert !Transition2DestinationNode.list()
        assert !ProcessVariableDef.list()
        assert !Variable2NodeVisibility.list()
    }

    void testStepByStepCreation() {
        def process = createProcess()
        assert process.save(flush: true)
        checkProcess()
        
        createVariables(process)
        assert process.save(flush: true)
        checkVariables()
        
        createNodes(process)
        assert process.save(flush: true)
        checkNodes()
        
        createTransitions(process)
        assert process.save(flush: true)
        checkTransitions()
        
        createVariableVisibilities(process)
        assert process.save(flush: true)
        checkVariableVisibilities()
        
        deleteProcess()
    }
    
    void testFullCreation() {
        def process = createProcess()
        createVariables(process)
        createNodes(process)

        // FIXME: Remove validation disabling after GRAILS-4613 will get fixed
        assert process.save(flush: true, validate: false)
        
        createTransitions(process)
        createVariableVisibilities(process)
        
        // FIXME: Remove validation disabling after GRAILS-4613 will get fixed
        assert process.save(flush: true, validate: false)

        checkProcess()
        checkVariables()
        checkNodes()
        checkTransitions()
        checkVariableVisibilities()
        
        deleteProcess()
    }

}
