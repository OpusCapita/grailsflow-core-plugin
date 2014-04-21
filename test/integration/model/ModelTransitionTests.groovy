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

/**
 * Tests for ProcessTransitionDef and dependent objects.
 *
 *
 * @author Maria Voitovich
 */
class ModelTransitionTests extends GroovyTestCase {

    void createProcessWithNodes() {
        def process = new ProcessDef()
        process.processID = "TestProcess"
        
        process.save(flush: true)

        def firstNode = new ProcessNodeDef(nodeID:"Begin",
                            dueDate:20000, type:"Wait", isFinal: false,
                            editorType: ConstantUtils.EDITOR_AUTO)
        process.addToNodes(firstNode)

        def secondNode = new ProcessNodeDef(nodeID:"End",
            dueDate:20000, type:"Activity", isFinal: true,
            editorType: ConstantUtils.EDITOR_AUTO)
        process.addToNodes(secondNode)
        
        assert firstNode.save(flush: true)
        assert secondNode.save(flush: true)
    }

    void createTransitions() {
        def firstNode = ProcessNodeDef.findWhere(nodeID: "Begin")
        def secondNode = ProcessNodeDef.findWhere(nodeID: "End")
        def transition = new ProcessTransitionDef()
        transition.event = "ok"
        transition.fromNode = firstNode
        transition.toNodes  = [ secondNode ]
        firstNode.addToTransitions(transition)

        assert transition.save(flush: true)
    }
    
    void cleanup() {
        def process = ProcessDef.findWhere(processID: "TestProcess")
        process.delete(flush: true)
    }
    
    void deleteNode(def node) {
        node.removeFromAssociations()
        node.delete(flush: true)
    }

    void deleteTransition(def transition){
        transition.removeFromAssociations()
        transition.delete(flush: true)
    }

    void testDeletionOfFromNode() {
        createProcessWithNodes()
        createTransitions()
        
        def fromNode = ProcessNodeDef.findWhere(nodeID: "Begin")
        //deleteNodeAssociations(fromNode)
        
        //fromNode = ProcessNodeDef.findWhere(nodeID: "Begin")
        deleteNode(fromNode)

        assert !ProcessTransitionDef.list()
        assert !Transition2DestinationNode.list()
        def nodes = ProcessNodeDef.list() 
        assert nodes
        assert 1 == nodes.size()
        
        cleanup()
    }
    
    void testDeletionOfDestinationNode() {
        createProcessWithNodes()
        createTransitions()

        def toNode = ProcessNodeDef.findWhere(nodeID: "End")
        //deleteNodeAssociations(toNode)
        
        //toNode = ProcessNodeDef.findWhere(nodeID: "End")
        deleteNode(toNode)
        
        //assert !ProcessTransitionDef.list()
        assert !Transition2DestinationNode.list()
        def nodes = ProcessNodeDef.list() 
        assert nodes
        assert 1 == nodes.size()
        cleanup()
    }
    
    void testDeletionOfTransition() {
        createProcessWithNodes()
        createTransitions()

        def transitions = ProcessTransitionDef.list()
        assert 1 == transitions.size()
        def transition = ProcessTransitionDef.list().find() { true }
        deleteTransition(transition)

        assert !ProcessTransitionDef.list()
        assert !Transition2DestinationNode.list()
        def nodes = ProcessNodeDef.list() 
        assert nodes
        assert 2 == nodes.size()
        cleanup()
    }
    

}
