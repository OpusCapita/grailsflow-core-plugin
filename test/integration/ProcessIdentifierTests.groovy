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

/**
 * Tests for checking worklist Process Identifier functionality.
 *
 * @author July Karpey
 */
class ProcessIdentifierTests extends processes.AbstractProcessTestCase {

    void testWorklist() {

        def admin = "admin"
        def process1, process2

        def variables = ["catalogId": "C100", "productId": "100P100", "quantity": "100"]
        if (processManagerService.checkProcessIdentifier("ProcessIdentifierTest", variables)) {
            process1 = processManagerService.startProcess("ProcessIdentifierTest", admin,
                                                          variables)
        }
        assert process1 != null

        if (processManagerService.checkProcessIdentifier("ProcessIdentifierTest", variables)) {
            process2 = processManagerService.startProcess("ProcessIdentifierTest", admin,
                                                          variables)
        }
        assert process2 == null

        variables =  ["catalogId": "C100", "productId": "100P200"]
        if (processManagerService.checkProcessIdentifier("ProcessIdentifierTest", variables)) {
            process2 = processManagerService.startProcess("ProcessIdentifierTest", admin,
                                                         variables)
        }

        assert process2 != null

        variables =  ["catalogId": "C200", "productId": "100P100"]
        if (processManagerService.checkProcessIdentifier("ProcessIdentifierTest", variables)) {
            process2 = processManagerService.startProcess("ProcessIdentifierTest", admin,
                                                          variables)
        }
        assert process2 != null

        processManagerService.sendEvent(process1, "AssignValues", "okay", admin)
        processManagerService.sendEvent(process1, "CheckValues", "okay", admin)
        processManagerService.sendEvent(process1, "CompleteProcess", null, admin)

        def processObject = BasicProcess.get(process1)
        processObject.refresh()
        assert processObject.status.statusID == ProcessStatusEnum.COMPLETED.value()

        variables = ["catalogId": "C100", "productId": "100P100"]
        if (processManagerService.checkProcessIdentifier("ProcessIdentifierTest", variables)) {
            process2 = processManagerService.startProcess("ProcessIdentifierTest", admin,
                                                          variables)
        }
        assert process2 != null

    }

}
