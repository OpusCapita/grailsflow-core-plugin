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

import com.jcatalog.grailsflow.model.process.*
import com.jcatalog.grailsflow.status.ProcessStatusEnum
import com.jcatalog.grailsflow.process.ProcessSearchParameters

/**
 * Tests for checking process provider functionality.
 *
 * TODO: adjust test
 *
 * @author July Karpey
 */
class ProcessProviderTests extends processes.AbstractProcessTestCase {
    def processProvider

    void testVariablesFilter() {

        def admin = "admin"

        def process1 = processManagerService.startProcess("HolidayRequest", admin,
                                                          ["requesterName": "Name1", 'requesterMail': 'mail1@test.com'])
        def process2 = processManagerService.startProcess("HolidayRequest", admin,
                                                          ["requesterName": "Name2", 'requesterMail': 'mail1@test.com'])
        def process3 = processManagerService.startProcess("HolidayRequest", admin,
                                                          ["requesterName": "Name3", 'requesterMail': 'mail1@test.com'])
        /*
         * Since first node is of 'Wait' type processStatus is 'SUSPENDED', nodeStatus is 'ACTIVATED'
         */

        ProcessSearchParameters searchParameters = new ProcessSearchParameters()

        def processesList = processProvider.getProcessList(searchParameters)
        assert processesList.size() == 3

        searchParameters.startedBy = "admin"
        searchParameters.statusID =  ProcessStatusEnum.SUSPENDED.value()
        processesList = processProvider.getProcessList(searchParameters)
        assert processesList.size() == 3

        searchParameters.variablesFilter = [:]
        processesList = processProvider.getProcessList(searchParameters)
        assert processesList.size() == 3

        searchParameters.variablesFilter = ["requesterName": ["Name1", "Name2", "Name3"]]
        processesList = processProvider.getProcessList(searchParameters)
        assert processesList.size() == 3

        searchParameters.variablesFilter = ["requesterName": ["Name1", "Name2"]]
        processesList = processProvider.getProcessList(searchParameters)
        assert processesList.size() == 2

        searchParameters.variablesFilter = ["requesterName": "Name1"]
        processesList = processProvider.getProcessList(searchParameters)
        assert processesList.size() == 1
        // method should not change varFilter (see GFW-87)
        assert searchParameters.variablesFilter == ["requesterName": "Name1"]

        searchParameters.variablesFilter = ["requesterMail": "mail1@test.com"]
        processesList = processProvider.getProcessList(searchParameters)
        assert processesList.size() == 3
        // method should not change varFilter (see GFW-87)
        assert searchParameters.variablesFilter == ["requesterMail": "mail1@test.com"]

        searchParameters.variablesFilter  = ["requesterName": ["Name1", "Name2"], "requesterMail": "mail1@test.com"]
        processesList = processProvider.getProcessList(searchParameters)
        assert processesList.size() == 2
        // method should not change varFilter (see GFW-87)
        assert searchParameters.variablesFilter == ["requesterName": ["Name1", "Name2"], "requesterMail": "mail1@test.com"]

        // tearDown test
        processManagerService.killProcess(process1, admin)
        processManagerService.killProcess(process2, admin)
        processManagerService.killProcess(process3, admin)

        // GFW-315
        def process4 = processManagerService.startProcess("HolidayRequest", admin,
                                                          ["requesterName": "John Smith", 'requesterMail': 'john@support.com'])
        def process5 = processManagerService.startProcess("HolidayRequest", admin,
                                                          ["requesterName": "Ann Smith", 'requesterMail': 'ann@test.com'])
        def process6 = processManagerService.startProcess("HolidayRequest", admin,
                                                          ["requesterName": "Ann Smiths", 'requesterMail': 'ann@support.com'])
        def process7 = processManagerService.startProcess("HolidayRequest", admin,
                                                          ["requesterName": "Alice Adams", 'requesterMail': 'ann@support.com'])
        searchParameters.variablesFilter = ["requesterName": ["%Smith%"]]
        processesList = processProvider.getProcessList(searchParameters)
        assert processesList.size() == 3

        searchParameters.variablesFilter = ["requesterName": ["%Smith%"], "requesterMail": "%@support.com"]
        processesList = processProvider.getProcessList(searchParameters)
        assert processesList.size() == 2

        searchParameters.variablesFilter = ["requesterName": ["%Smith_"], "requesterMail": "%@support.com"]
        processesList = processProvider.getProcessList(searchParameters)
        assert processesList.size() == 1

        searchParameters.variablesFilter = ["requesterName": ["%Smith%", "%Adams"], "requesterMail": "%@support.com"]
        processesList = processProvider.getProcessList(searchParameters)
        assert processesList.size() == 3

        processManagerService.killProcess(process4, admin)
        processManagerService.killProcess(process5, admin)
        processManagerService.killProcess(process6, admin)
        processManagerService.killProcess(process7, admin)
    }

}