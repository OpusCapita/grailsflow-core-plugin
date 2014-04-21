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
import com.jcatalog.grailsflow.status.NodeStatusEnum

/**
 * Tests for checking pre-defined and dynamic assignments.
 *
 *
 * @author Maria Voitovich
 */
class TestAssignmentProcessTests extends AbstractProcessTestCase {

    def processWorklistService

    void assertAssignment(String authority, int availableNodes) {
        def authorities = []
        authorities << "ROLE_${authority}".toString()
        def worklist = processWorklistService.getWorklist(authorities, [:], "nodeID", null, null, null)
        assert worklist != null
        assert worklist.size() == availableNodes
    }

    void testDynamicAssignment() {
        def admin = "admin"
        def manager = "manager"
        def employee = "employee"
        def hr_user = "hr-user"

        def processId = startProcess("TestAssignment", admin, ['dynamicAssignee': 'HR_USER'])

        //check process started
        assert processId != null
        assert BasicProcess.get(processId)?.status.statusID == ProcessStatusEnum.ACTIVATED.value()

        // execute start node
        invokeCurrentNodes()

        // check visibility of AdminNode
        assertAssignment('ADMIN', 1)
        assertAssignment('MANAGER', 0)
        assertAssignment('HR_USER', 0)
        assertAssignment('SIMPLE_USER', 0)

        // execute AdminNode        
        executeManualNode(processId, "adminNode", "assign", admin, null)

        // execute assignDynamicNode
        invokeCurrentNodes()

        // check visibility of dynamicAssigneeNode
        assertAssignment('ADMIN', 0)
        assertAssignment('MANAGER', 0)
        assertAssignment('HR_USER', 1)
        assertAssignment('SIMPLE_USER', 0)

        // execute dynamicAssigneeNode
        executeManualNode(processId, "dynamicAssigneeNode", "done", hr_user, null)

        // check visibility of managerNode
        assertAssignment('ADMIN', 0)
        assertAssignment('MANAGER', 1)
        assertAssignment('HR_USER', 0)
        assertAssignment('SIMPLE_USER', 0)

        // execute managerNode
        executeManualNode(processId, "managerNode", "done", manager, null)

		// execute Finish node        
        invokeCurrentNodes()

        //check process finished
        BasicProcess.withSession{ session -> session.clear() }
        assert BasicProcess.get(processId).status.statusID == ProcessStatusEnum.COMPLETED.value()
    }

    void testPredefinedAssignment() {
        def admin = "admin"
        def manager = "manager"
        def employee = "employee"
        def hr_user = "hr-user"

        def processId = startProcess("TestAssignment", admin, null)

        //check process started
        assert BasicProcess.get(processId).status.statusID == ProcessStatusEnum.ACTIVATED.value()

        // execute start node
        invokeCurrentNodes()

        // check visibility of AdminNode
        assertAssignment('ADMIN', 1)
        assertAssignment('MANAGER', 0)
        assertAssignment('HR_USER', 0)
        assertAssignment('SIMPLE_USER', 0)

        // execute AdminNode        
        executeManualNode(processId, "adminNode", "assign", admin, null)
        
        // execute assignDynamicNode
        invokeCurrentNodes()

        // check visibility of dynamicAssigneeNode
        assertAssignment('ADMIN', 1)
        assertAssignment('MANAGER', 1)
        assertAssignment('HR_USER', 1)
        assertAssignment('SIMPLE_USER', 1)

        // execute dynamicAssigneeNode
        executeManualNode(processId, "dynamicAssigneeNode", "done", hr_user, null)
        
        // check visibility of managerNode
        assertAssignment('ADMIN', 0)
        assertAssignment('MANAGER', 1)
        assertAssignment('HR_USER', 0)
        assertAssignment('SIMPLE_USER', 0)

        // execute managerNode
        executeManualNode(processId, "managerNode", "done", manager, null)

		// execute Finish node        
        invokeCurrentNodes()

        //check process finished
        BasicProcess.withSession{ session -> session.clear() }
        assert BasicProcess.get(processId).status.statusID == ProcessStatusEnum.COMPLETED.value()
    }
    
    // Assumes that we have just one active node  
    private getCurrentNode(def processId) {
       BasicProcess.withSession{ session -> session.clear() }
       def process = BasicProcess.get(processId)
       def activeStatus = FlowStatus.findByStatusID(NodeStatusEnum.ACTIVATED.value())
       def node =  process.nodes.find() { it.status == activeStatus }
       return node
    }
    
    void testForwardAssignment() {
        def admin = "admin"
        def manager = "manager"
        def employee = "employee"
        def hr_user = "hr-user"

        def processId = startProcess("TestAssignment", admin, null)

        //check process started
        assert BasicProcess.get(processId)?.status.statusID == ProcessStatusEnum.ACTIVATED.value()

        // execute start node
        invokeCurrentNodes()

        // check visibility of AdminNode
        assertAssignment('ADMIN', 1)
        assertAssignment('MANAGER', 0)
        assertAssignment('HR_USER', 0)
        assertAssignment('SIMPLE_USER', 0)
        
        // forward AdminNode to employee
        def forwardResult = processManagerService.forwardProcessNode(getCurrentNode(processId), ["ROLE_SIMPLE_USER"], admin)
        assert forwardResult == Boolean.TRUE

        // check new visibility of adminNode
        assertAssignment('ADMIN', 0)
        assertAssignment('MANAGER', 0)
        assertAssignment('HR_USER', 0)
        assertAssignment('SIMPLE_USER', 1)

        // execute adminNode        
        executeManualNode(processId, "adminNode", "assign", employee, null)
        
        // execute assignDynamicNode
        invokeCurrentNodes()

        // check visibility of dynamicAssigneeNode
        assertAssignment('ADMIN', 1)
        assertAssignment('MANAGER', 1)
        assertAssignment('HR_USER', 1)
        assertAssignment('SIMPLE_USER', 1)

        // execute dynamicAssigneeNode
        executeManualNode(processId, "dynamicAssigneeNode", "done", hr_user, null)
        
        // check visibility of managerNode
        assertAssignment('ADMIN', 0)
        assertAssignment('MANAGER', 1)
        assertAssignment('HR_USER', 0)
        assertAssignment('SIMPLE_USER', 0)
        
        // forward managerNode to hr_user
        processManagerService.forwardProcessNode(getCurrentNode(processId), ["ROLE_HR_USER"], manager)

        // check new visibility of managerNode
        assertAssignment('ADMIN', 0)
        assertAssignment('MANAGER', 0)
        assertAssignment('HR_USER', 1)
        assertAssignment('SIMPLE_USER', 0)

        // execute managerNode
        executeManualNode(processId, "managerNode", "done", hr_user, null)

        // execute Finish node        
        invokeCurrentNodes()

        //check process finished
        BasicProcess.withSession{ session -> session.clear() }
        assert BasicProcess.get(processId).status.statusID == ProcessStatusEnum.COMPLETED.value()
    }         

}
