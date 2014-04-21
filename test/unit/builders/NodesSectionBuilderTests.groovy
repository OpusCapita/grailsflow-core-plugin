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
package builders

import com.jcatalog.grailsflow.builder.NodesSectionBuilder

/**
 * Tests for NodesSectionBuilder class.
 *
 * @author Maria Voitovich
 *
 */
class NodesSectionBuilderTests extends GroovyTestCase {

    void testNodesSectionBuilder() {
      def closure = {
      }
      def nodeSection = buildNodeSection(closure)
      assert null != nodeSection
    }

    void testNodes() {
      def closure
      def testNodesClosure = { testClosure ->
        def nodeSection = buildNodeSection(testClosure)
        return nodeSection.nodes*.nodeID
      }

      closure = {
        node() {
        }
      }
      assert ["node"] == testNodesClosure(closure)

      closure = {
        node1() {
        }
        node2() {
        }
      }
      assert ["node1", "node2"] == testNodesClosure(closure)

      // Order is kept
      closure = {
        node2() {
        }
        node1() {
        }
      }
      assert ["node2", "node1"] == testNodesClosure(closure)
    }

    void testAssignees() {
      def closure
      def allAssignees
      def testAssigneesClosure = { testClosure ->
        def nodeSection = buildNodeSection(testClosure)
        assert nodeSection 
        return nodeSection.assignees
      }

      closure = {
      }
      assert [:] == testAssigneesClosure(closure)

      closure = {
        node() {
        }
      }
      assert [:] == testAssigneesClosure(closure)

      closure = {
        node() {
          assignees()
        }
      }
      assert ["node":[]] == testAssigneesClosure(closure)

      // node assignees
      closure = {
        node() {
          assignees( users: ["user1", "user2"], roles: ["role1", "role2"], groups: ["group1", "group2"] )
        }
      }
      allAssignees = testAssigneesClosure(closure)
      assert allAssignees
      assert 1 == allAssignees.size()
      assert allAssignees["node"]
      def nodeAssignees = allAssignees["node"]*.assigneeID
      assert 6 == nodeAssignees.size()
      assert nodeAssignees.containsAll("USER_user1", "USER_user2")
      assert nodeAssignees.containsAll("ROLE_role1", "ROLE_role2")
      assert nodeAssignees.containsAll("GROUP_group1", "GROUP_group2")

      // process assignees
      closure = {
        assignees( users: ["user1", "user2"], roles: ["role1", "role2"], groups: ["group1", "group2"] )
        node() {
        }
      }
      allAssignees = testAssigneesClosure(closure)
      assert allAssignees
      assert 1 == allAssignees.size()
      assert allAssignees["ProcessMock"]
      def processAssignees = allAssignees["ProcessMock"]*.assigneeID
      assert 6 == processAssignees.size()
      assert processAssignees.containsAll("USER_user1", "USER_user2")
      assert processAssignees.containsAll("ROLE_role1", "ROLE_role2")
      assert processAssignees.containsAll("GROUP_group1", "GROUP_group2")

      // process and node assignees
      closure = {
        assignees( users: ["user1"], roles: ["role1"], groups: ["group1"] )
        node() {
          assignees( users: ["user1", "user2"], roles: ["role1", "role2"], groups: ["group1", "group2"] )
        }
      }
      allAssignees = testAssigneesClosure(closure)
      assert allAssignees
      assert 2 == allAssignees.size()
      // check process assignees
      processAssignees = allAssignees["ProcessMock"]*.assigneeID
      assert 3 == processAssignees.size()
      assert processAssignees.contains("USER_user1")
      assert processAssignees.contains("ROLE_role1")
      assert processAssignees.contains("GROUP_group1")
      assert allAssignees["node"]
      nodeAssignees = allAssignees["node"]*.assigneeID
      assert 6 == nodeAssignees.size()
      assert nodeAssignees.containsAll("USER_user1", "USER_user2")
      assert nodeAssignees.containsAll("ROLE_role1", "ROLE_role2")
      assert nodeAssignees.containsAll("GROUP_group1", "GROUP_group2")


    }

    def buildNodeSection(def closure) {
      def processMock = createProcessMock(closure)
      return new NodesSectionBuilder(processMock)
    }

    def createProcessMock(def closure) {
      def processMock = new ProcessMock()
      processMock.setProcessClosure(closure)
      return processMock
    }

    static class ProcessMock {
      def processClosure

      public void setProcessClosure(def closure) {
        this.processClosure = closure
      }

      static def getSimpleName() {
        return "ProcessMock"
      }

      def getProcessMock() {
        return processClosure
      }
    }

}
