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

import com.jcatalog.grailsflow.model.definition.*
import com.jcatalog.grailsflow.model.process.*
import com.jcatalog.grailsflow.status.NodeStatusEnum
import com.jcatalog.grailsflow.status.ProcessStatusEnum

/**
 * Tests for HolidayRequestProcess workflow.
 * Also we check assigneers for next nodes in workflow.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class HolidayRequestProcessTests extends AbstractProcessTestCase {
                                 
    def generateProcessService

    void testDefinitionBuilder() {

        // get process class for process Script
        def processClass = getProcessManagerService().getProcessClass("HolidayRequest")
        def processDef = generateProcessService
                             .buildProcessDefinition(processClass)

        assert processDef
        assert processDef.processID.equals("HolidayRequest") == true
        //assert processDef.description.equals("This process manages the request and approval of holidays for employees.") == true
        assert processDef.nodes.size() == 9
        assert processDef.variables.size() == 10
        assert processDef.description.size() == 2
        //assert processDef.transitions.size()== 10
        assert processDef.startNode.nodeID.equals("HolidayRequestForm") == true
    }

    void testFlowAndAssigners() {

      // initialize system and demo-data information
      def admin = "admin"
      def manager = "manager"
      def employee = "employee"
      def hr_user = "hr-user"

      def activeStatus = FlowStatus.findByStatusID(NodeStatusEnum.ACTIVATED.value())

      def processId = startProcess("HolidayRequest", employee, null)
      
      def basicProcess = BasicProcess.get(processId)

      // executing current nodes
      invokeCurrentNodes()

      // executing HolidayRequestForm
      executeManualNode(processId, "HolidayRequestForm", "submit", employee, null)

      // ManagerApproveHolidays 'Wait' node gets activated
      def activeNodes = ProcessNode.findAllWhere(process: basicProcess, status: activeStatus)
      assert 1 == activeNodes.size()
      def activeNode = activeNodes.get(0)
      assert "ManagerApproveHolidays" == activeNode.nodeID
      // check assignees for ManagerApproveHolidays
      def assignees = ProcessAssignee.findAllWhere(process: basicProcess, nodeID: activeNode.nodeID)*.assigneeID
      assert 2 == assignees.size()
      assert true == assignees.contains("ROLE_MANAGER")
      assert true == assignees.contains("ROLE_ADMIN")
      
      // executing ManagerApproveHolidays
      executeManualNode(processId, "ManagerApproveHolidays", "approve", manager, null)
      
      // executing ApprovedOperation
      invokeCurrentNodes()
      
      // HRNotification and ApproveNotification nodes get activated
      activeNodes = ProcessNode.findAllWhere(process: basicProcess, status: activeStatus)
      assert 2 == activeNodes.size()
      def activeNodesIDs = activeNodes.collect() { it.nodeID }
      assert true == activeNodesIDs.contains("HRNotification")
      assert true == activeNodesIDs.contains("ApproveNotification")

      // executing HRNotification and ApproveNotification
      invokeCurrentNodes()

      // HRBook 'Wait' node gets activated
      activeNodes = ProcessNode.findAllWhere(process: basicProcess, status: activeStatus)
      assert 1 == activeNodes.size()
      activeNode = activeNodes.get(0)
      assert "HRBook" == activeNode.nodeID
      // check assignees for HRBook
      assignees = ProcessAssignee.findAllWhere(process: basicProcess, nodeID: activeNode.nodeID)*.assigneeID
      assert 2 == assignees.size()
      assert true == assignees.contains("ROLE_HR_USER")
      assert true == assignees.contains("ROLE_ADMIN")

      // executing HRBook
      executeManualNode(processId, "HRBook", "save", hr_user, null)
      
      // ApproveFinished 'AndJoin' node gets activated
      activeNodes = ProcessNode.findAllWhere(process: basicProcess, status: activeStatus)
      assert 1 == activeNodes.size()
      activeNode = activeNodes.get(0)
      assert "ApproveFinished" == activeNode.nodeID

      // executing ApproveFinished
      invokeCurrentNodes()
      
      // Finish node gets activated
      activeNodes = ProcessNode.findAllWhere(process: basicProcess, status: activeStatus)
      assert 1 == activeNodes.size()
      activeNode = activeNodes.get(0)
      assert "Finish" == activeNode.nodeID
      
      // executing Finish
      invokeCurrentNodes()

      basicProcess.refresh()
      assert basicProcess.status.statusID == ProcessStatusEnum.COMPLETED.value()
    }

 }
