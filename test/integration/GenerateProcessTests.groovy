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

import com.jcatalog.grailsflow.workarea.*

import com.jcatalog.grailsflow.utils.ConstantUtils

import com.jcatalog.grailsflow.model.definition.*

/**
 * Tests for GenerateProcessService class.
 *
 * GenerateProcessService creates a Groovy class, that represents the
 * executable process based on the process definition. This class can be
 * edited an changed by the user, however Grailsflow can currently not
 * "reengeneer" the changes, when the class is created again.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class GenerateProcessTests extends GroovyTestCase {
    def generateProcessService
    def scriptsProvider
    def processFactory
    def grailsApplication

    void createProcessWithVariablesAndNodes() {
        ProcessDef process = new ProcessDef()
        process.processID = "GeneratedTest"
        process.description = [en: "This proces workflow is generated via generate service"]
        process.save()

        process.addToVariables(new ProcessVariableDef(processDef: process, type:"String", name:"thumbnailPath"))
        process.addToVariables(new ProcessVariableDef(processDef: process, type:"Integer", name:"quantity"))
        process.addToVariables(new ProcessVariableDef(processDef: process, type:"Date", name:"boughtOn"))

        process.addToVariables(new ProcessVariableDef(processDef: process, type:"Link", name:"myLink1", defaultValue: '["path": "http://www.google.com", "description": "Google Search"]'))
        process.addToVariables(new ProcessVariableDef(processDef: process, type:"Link", name:"myLink2", defaultValue: '["description": "Google Search"]'))
        process.addToVariables(new ProcessVariableDef(processDef: process, type:"Link", name:"myLink3"))

        ProcessNodeDef firstNode = new ProcessNodeDef(processDef: process, nodeID:"Begin",
                            dueDate:20000, type:"Wait", isFinal: false,
                            editorType: ConstantUtils.EDITOR_AUTO)
        ProcessNodeDef endNode = new ProcessNodeDef(processDef: process, nodeID:"End",
            dueDate:20000, type:"Activity", isFinal: true,
            editorType: ConstantUtils.EDITOR_AUTO)

        process.addToNodes(firstNode)
        process.addToNodes(endNode)

        firstNode.addToActionStatements(new ActionStatement(content: 'if (quantity > 100) {'))
        firstNode.addToActionStatements(new ActionStatement(content: "thumbnailPath = \"documents/attachments/thumbnail1.jpg\""))
        firstNode.addToActionStatements(new ActionStatement(content: "Log(logMessage: 'Test Action is running thumbnailPath = \$thumbnailPath')"))
        firstNode.addToActionStatements(new ActionStatement(content: "}"))
        firstNode.addToActionStatements(new ActionStatement(content: 'if (thumbnailPath != null) {'))
        firstNode.addToActionStatements(new ActionStatement(content: "Log(logMessage: 'Thumbnail Path value is not null.')"))
        firstNode.addToActionStatements(new ActionStatement(content: "}"))
        firstNode.addToActionStatements(new ActionStatement(content: 'if ("$thumbnailPath".contains("attachments")) {'))
        firstNode.addToActionStatements(new ActionStatement(content: "Log(logMessage: \"Thumbnail Path contains word 'attachments' in its value .\")"))
        firstNode.addToActionStatements(new ActionStatement(content: "}"))

        // save process with variables and nodes
        assert process.save(flush: true)
    }

    void createTransitions() {
        ProcessNodeDef firstNode = ProcessNodeDef.findWhere(nodeID: "Begin")
        ProcessNodeDef secondNode = ProcessNodeDef.findWhere(nodeID: "End")
        ProcessTransitionDef transition = new ProcessTransitionDef()
        transition.event = "ok"
        transition.fromNode = firstNode
        transition.toNodes  = [ secondNode ]
        firstNode.addToTransitions(transition)

        assert transition.save(flush: true)
    }

    void createVariableVisibility() {
        ProcessNodeDef firstNode = ProcessNodeDef.findWhere(nodeID: "Begin")

        ProcessVariableDef variable1 = ProcessVariableDef.findWhere(name: "thumbnailPath")
        Variable2NodeVisibility visibility1 = new Variable2NodeVisibility(visibilityType: 2, visibilityDesc: "WRITE_READ")
        variable1.addToVariable2NodesVisibility(visibility1)
        firstNode.addToVariables2NodeVisibility(visibility1)

        ProcessVariableDef variable2 = ProcessVariableDef.findWhere(name: "quantity")
        Variable2NodeVisibility visibility2 = new Variable2NodeVisibility(visibilityType: 1, visibilityDesc: "READ_ONLY")
        variable2.addToVariable2NodesVisibility(visibility2)
        firstNode.addToVariables2NodeVisibility(visibility2)

        ProcessVariableDef variable3 = ProcessVariableDef.findWhere(name: "boughtOn")
        Variable2NodeVisibility visibility3 = new Variable2NodeVisibility(visibilityType: 0, visibilityDesc: "INVISIBLE")
        variable3.addToVariable2NodesVisibility(visibility3)
        firstNode.addToVariables2NodeVisibility(visibility3)

        assert firstNode.save(flush: true)
    }


    void testProcessGenerator() {

        createProcessWithVariablesAndNodes()
        createTransitions()
        createVariableVisibility()

        ProcessDef process = ProcessDef.findWhere(processID: "GeneratedTest")

        Boolean result = generateProcessService.generateGroovyProcess(process)
        assert result == Boolean.TRUE

        File file = scriptsProvider.getResourceFile("processes/GeneratedTestProcess.groovy")
        assert file.exists()

        // trying to compile generated process groovy script
        GroovyCodeSource groovyClass = new GroovyCodeSource(file)
        Class processClass = grailsApplication.classLoader
            .parseClass(groovyClass, true)
        assert processClass != null

        // Cleanup
        // delete process definition
        ProcessDef.findAllWhere("processID": "GeneratedTest")*.delete()
        // delete process builder from cache
        processFactory.removeProcessClass("GeneratedTest")
        // delete process file
        file.delete();
    }

}
