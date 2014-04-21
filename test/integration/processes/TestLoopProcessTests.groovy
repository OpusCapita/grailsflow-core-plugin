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

package processes;

import com.jcatalog.grailsflow.model.process.*
import com.jcatalog.grailsflow.status.ProcessStatusEnum

/**
 * Tests for checking loops handling.
 *
 *
 * @author Maria Voitovich
 */
class TestLoopProcessTests extends AbstractProcessTestCase {

    void testProcess() {

        def activeStatus = FlowStatus.findByStatusID(ProcessStatusEnum.ACTIVATED.value())

        def processId = startProcess("TestLoop", "test", [:])

        //check process started
        assert processId != null
        def process = BasicProcess.get(processId)
        assert process
        assert activeStatus == process.status

        def activeNodes
        def activeNode

        for (int i=7; i>0; --i) {
          activeNodes = ProcessNode.findAllWhere(process: process, status: activeStatus)
          assert 1 == activeNodes.size()
          activeNode = activeNodes.get(0)
          assert "loop" == activeNode.nodeID

          // execute loop node
          invokeCurrentNodes()

        }

        activeNodes = ProcessNode.findAllWhere(process: process, status: activeStatus)
        assert 1 == activeNodes.size()
        activeNode = activeNodes.get(0)
        assert "finish" == activeNode.nodeID

        // execute finish node
        invokeCurrentNodes()

        activeNodes = ProcessNode.findAllWhere(process: process, status: activeStatus)
        assert 0 == activeNodes.size()

        //check process is killed
        process.refresh()
        assert process.status.statusID == ProcessStatusEnum.COMPLETED.value()

        assert 8 == process.nodes.size()
    }


}
