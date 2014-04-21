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

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import com.jcatalog.grailsflow.worklist.WorklistProvider

import com.jcatalog.grailsflow.model.process.*
import org.hibernate.FlushMode
import com.jcatalog.grailsflow.status.ProcessStatusEnum
import com.jcatalog.grailsflow.status.NodeStatusEnum

/**
 * Tests for checking worklist provider functionality.
 *
 * @author Eugene Parkhomenko
 */
class WorklistProviderTests extends processes.AbstractProcessTestCase {

    def worklistProvider

    void testWorklist() {
        def admin = "admin"
        def manager = "manager"
        def employee = "employee"

        def process1 = processManagerService.startProcess("HolidayRequest", admin,
                                                          ["requesterName": "July"])
        assert process1 != null
        def process2 = processManagerService.startProcess("HolidayRequest", admin, null)
        assert process2 != null
        def basicProcess1 = BasicProcess.get(process1)
        def basicProcess2 = BasicProcess.get(process2)

        // executing scheduler jobs
        def job = appContext.getBean("NodeActivatorJob")
        job.execute()
        assert ProcessNode.findByProcessAndNodeID(basicProcess2, "HolidayRequestForm").status.statusID == NodeStatusEnum.ACTIVATED.value()

        processManagerService.sendEvent(process1, "HolidayRequestForm", "submit", employee)
        processManagerService.sendEvent(process2, "HolidayRequestForm", "submit", employee)
        job.execute()

        // executing ManagerApproveHolidays for process2
        processManagerService.sendEvent(process2, "ManagerApproveHolidays", "approve", manager)
        job.execute()
        assert ProcessNode.findByNodeID("HolidayRequestForm").status.statusID == NodeStatusEnum.COMPLETED.value()
        job.execute()
        assert ProcessNode.findByNodeIDAndProcess("ManagerApproveHolidays", basicProcess2).status.statusID == NodeStatusEnum.COMPLETED.value()

        def authorities = ["ROLE_MANAGER"]
        def varFilter = ['requesterName': 'July']
        def worklist = worklistProvider.getWorklist(authorities, varFilter, "nodeID", null, null, null)
        assert worklist.size() == 1

        authorities = ["ROLE_ADMIN"]
        varFilter = ['requesterName': 'July']
        worklist = worklistProvider.getWorklist(authorities, varFilter, "nodeID", null, null, null)
        assert worklist.size() == 1

        authorities = ["ROLE_ADMIN"]
        varFilter = null
        worklist = worklistProvider.getWorklist(authorities, varFilter, "nodeID", null, null, null)
        assert worklist.size() == 2

        authorities = null
        varFilter = null
        worklist = worklistProvider.getWorklist(authorities, varFilter, "nodeID", null, null, null)
        assert worklist == null

        // executing ManagerApproveHolidays for process1
        processManagerService.sendEvent(process1, "ManagerApproveHolidays", "approve", manager)
        job.execute()
        // executing ApprovedOperation
	    job.execute()
        // executing HRNotification and ApproveNotification
	    job.execute()

        authorities = ["ROLE_ADMIN"]
        varFilter = ['requesterName': 'July']
        worklist = worklistProvider.getWorklist(authorities, varFilter, "nodeID", null, null, null)
        assert worklist.size() == 1

        authorities = ["ROLE_MANAGER"]
        varFilter = ['requesterName': 'July']
        worklist = worklistProvider.getWorklist(authorities, varFilter, "nodeID", null, null, null)
        assert worklist.size() == 0

        // tearDown test
        assert processManagerService.killProcess(process1, admin)== Boolean.TRUE
        basicProcess1.refresh()
        basicProcess2.refresh()

        assert basicProcess1.status.statusID == ProcessStatusEnum.KILLED.value()
        assert basicProcess2.status.statusID == ProcessStatusEnum.SUSPENDED.value()

    }

    void testWorklistProvider() {
        def authorities = ["ROLE_ADMIN"]
        def admin = "admin"

        processManagerService.startProcess("HolidayRequest", admin,
            ["requesterName": "John Smith", 'requesterMail': 'john@support.com'])
        processManagerService.startProcess("HolidayRequest", admin,
            ["requesterName": "Ann Smith", 'requesterMail': 'ann@test.com'])
        processManagerService.startProcess("HolidayRequest", admin,
            ["requesterName": "Ann Smiths", 'requesterMail': 'ann@support.com'])
        processManagerService.startProcess("HolidayRequest", admin,
            ["requesterName": "Alice Adams", 'requesterMail': 'ann@support.com'])

        def varFilter = ["requesterName": ["%Smith%"]]
        def worklist = worklistProvider.getWorklist(authorities, varFilter, "nodeID", null, null, null)
        assert worklist.size() == 3

        varFilter = ["requesterName": ["%Smith%"], "requesterMail": "%@support.com"]
        worklist = worklistProvider.getWorklist(authorities, varFilter, "nodeID", null, null, null)
        assert worklist.size() == 2

        varFilter = ["requesterName": ["%Smith_"], "requesterMail": "%@support.com"]
        worklist = worklistProvider.getWorklist(authorities, varFilter, "nodeID", null, null, null)
        assert worklist.size() == 1

        varFilter = ["requesterName": ["%Smith%", "%Adams"], "requesterMail": "%@support.com"]
        worklist = worklistProvider.getWorklist(authorities, varFilter, "nodeID", null, null, null)
        assert worklist.size() == 3

    }
    
    void testWorklistSorting() {

        def admin = "admin"

        // processes that have no "catalogId" variable
        def process1 = processManagerService.startProcess("HolidayRequest", admin,
                                                          ["requesterName": "name_1"])
        Thread.sleep(1000);
        def process2 = processManagerService.startProcess("HolidayRequest", admin,
                                                          ["requesterName": "name_2"])
        Thread.sleep(1000);
        def process3 = processManagerService.startProcess("HolidayRequest", admin,
                                                          [:]) // "requesterName" variable has value null

        Thread.sleep(1000);
        // processrs that have "catalogId" variable
        def process4 = processManagerService.startProcess("ProcessIdentifierTest", admin,
                                                          [:]) // "catalogId" variable has value null
        Thread.sleep(1000);
        def process5 = processManagerService.startProcess("ProcessIdentifierTest", admin,
                                                          ["productId": "product_1"])
        Thread.sleep(1000);
        def process6 = processManagerService.startProcess("ProcessIdentifierTest", admin,
                                                          ["productId": "product_2"])
        Thread.sleep(1000);

        // executing scheduler jobs
        def job = appContext.getBean("NodeActivatorJob")
        job.execute() // executing start nodes

        def authorities = ["ROLE_ADMIN"]
        def varFilter = [:]
        def worklist = worklistProvider.getWorklist(authorities, varFilter, "startedOn", "asc", null, null).collect() { it.process.id - process1 }
        assert worklist.size() == 6
        assert worklist == [0, 1, 2, 3, 4, 5], 'sorting via startedOn failed'

        worklist = worklistProvider.getWorklist(authorities, varFilter, "startedOn", "desc", null, null).collect() { it.process.id - process1 }
        assert worklist.size() == 6
        assert worklist == [5, 4, 3, 2, 1, 0]

        worklist = worklistProvider.getWorklist(authorities, varFilter, "vars.productId", "asc", null, null).collect() { it.process.id - process1 }
        assert worklist.size() == 6
        assert worklist[0..3].sort() == [0, 1, 2, 3] , 'sorting via vars.productId failed (asc)'
        assert worklist[4..5] == [4, 5], 'sorting via vars.productId failed (asc)'


        worklist = worklistProvider.getWorklist(authorities, varFilter, "vars.productId", "desc", null, null).collect() { it.process.id - process1 }
        assert worklist.size() == 6
        assert worklist[0..1] == [5,4], 'sorting via vars.productId failed (desc)'
        assert worklist[2..5].sort() == [0, 1, 2, 3], 'sorting via vars.productId failed (desc)'

        // [0, 1, 2, 3, 4, 5] with offset 2
        worklist = worklistProvider.getWorklist(authorities, varFilter, "startedOn", "asc", null, 2).collect() { it.process.id - process1 }
        assert worklist.size() == 4
        assert worklist == [2, 3, 4, 5]

        // [5, 4, 3, 0, 1, 2] with offset 2
        worklist = worklistProvider.getWorklist(authorities, varFilter, "startedOn", "desc", null, 2).collect() { it.process.id - process1 }
        assert worklist.size() == 4
        assert worklist == [3, 2, 1, 0]

        // [0, 1, 2, 3, 4, 5] with maxResult 5
        worklist = worklistProvider.getWorklist(authorities, varFilter, "startedOn", "asc", 5, null).collect() { it.process.id - process1 }
        assert worklist.size() == 5
        assert worklist == [0, 1, 2, 3, 4]

        // [5, 4, 3, 2, 1, 0] with maxResult 5
        worklist = worklistProvider.getWorklist(authorities, varFilter, "startedOn", "desc", 5, null).collect() { it.process.id - process1 }
        assert worklist.size() == 5
        assert worklist == [5, 4, 3, 2, 1]

        // [0, 1, 2, 3, 4, 5] with offset 2 and maxResult 3
        worklist = worklistProvider.getWorklist(authorities, varFilter, "startedOn", "asc", 3, 2).collect() { it.process.id - process1 }
        assert worklist.size() == 3
        assert worklist == [2, 3, 4]

        // [5, 4, 3, 2, 1, 0] with offset 2 and maxResult 3
        worklist = worklistProvider.getWorklist(authorities, varFilter, "startedOn", "desc", 3, 2).collect() { it.process.id - process1 }
        assert worklist.size() == 3
        assert worklist == [3, 2, 1]

        // tearDown test
        processManagerService.killProcess(process1, admin)
        processManagerService.killProcess(process2, admin)
        processManagerService.killProcess(process3, admin)
        processManagerService.killProcess(process4, admin)
        processManagerService.killProcess(process5, admin)
        processManagerService.killProcess(process6, admin)
    }
}