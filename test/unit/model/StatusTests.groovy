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
package model

import com.jcatalog.grailsflow.model.process.FlowStatus

/**
 * Tests of FlowStatus comparing
 *
 * @author Maria Voitoich
 */
class StatusTests extends GroovyTestCase {

  void testCompareStatuses() {
    def status1 = new FlowStatus(statusID: "ONE")
    def status2 = new FlowStatus(statusID: "ONE")
    def status3 = new FlowStatus(statusID: "TWO")
    def obj = new Object()
    
    assert true == (status1==status2)
    assert false == (status1!=status2)

    assert true == (status1!=status3)
    assert false == (status1==status3)
    
    assert false == (status1==obj)
    assert true == (status1!=obj)
  }

}