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

import com.jcatalog.grailsflow.utils.ConstantUtils

/**
 * Tests for ProcessBuilder class.
 *
 * @author Stephan Albers
 * @author July Karpey
 *
 * TODO: add tests for each of SectionBuilders
 */
class ProcessBuilderTests extends GroovyTestCase {
    def processFactory
    
    void testProcessClass() {
        
        // get Process Class for process Script
        Class processClass = processFactory.getProcessClassForName("TestExceptions")

        // checking static fields values for process class
        assert processClass.startNode
        assert processClass.startNode.nodeID == "start"
        assert processClass.finalNodes
        assert processClass.finalNodes.size() == 1
        assert processClass.finalNodes*.nodeID.containsAll(["finish"])
        assert processClass.nodesList
        assert processClass.nodesList*.nodeID == ["start", "throwHandledException", "handleException", "throwUnhandledException", "finish"]
        processClass.nodesList.each () { node->
          assert node.type == "Activity"
        }
    }

}
