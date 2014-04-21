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
import com.jcatalog.grailsflow.utils.ConstantUtils

import com.jcatalog.grailsflow.bean.BeanProxySupport
import com.jcatalog.grailsflow.status.ProcessStatusEnum

/**
 * Tests for BeanProxySupport.
 *
 *
 * @author Maria Voitovich
 */
class BeanProxySupportTests extends GroovyTestCase {

    BasicProcess createProcess() {
        def status = FlowStatus.findByStatusID(ProcessStatusEnum.KILLED.value())
        def process = new BasicProcess()
        process.type = "TestProcess"
        process.status = status
        process.createdOn = new Date()
        process.createdBy = "test"
        process.lastModifiedOn = new Date()
        process.lastModifiedBy = "test"

        def node = new ProcessNode(nodeID:"Test", type:"Wait", status: status, startedOn: new Date())
        process.addToNodes(node)
        return process
    }
        

    void testDomainObjectProxy() {
        def process = createProcess()
        assert process.save(flush: true)

        def proxy = new BeanProxySupport(process)

        assert "TestProcess" == proxy.type

        shouldFail {
          proxy.type = "Harmful process"
        }

        shouldFail {
          proxy.delete()
        }

        def status = proxy.status

        assert ProcessStatusEnum.KILLED.value() == status.statusID

        shouldFail {
          status.statusID = "DEAD"
        }

        shouldFail {
          status.save()
        }

        process.refresh()
        process.delete(flush: true)
    }

}
