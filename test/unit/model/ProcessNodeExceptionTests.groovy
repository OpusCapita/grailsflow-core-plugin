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

import com.jcatalog.grailsflow.model.process.ProcessNodeException

/**
 * Tests of ProcessNodeException data truncation
 *
 * @author Maria Voitoich
 */
class ProcessNodeExceptionTests extends GroovyTestCase {

  void testExceptionMessage() {
    def exception

    // no message
    exception = new ProcessNodeException(new Exception())
    assert null == exception.message

    // short message
    exception = new ProcessNodeException(new Exception("message"))
    assert "message" == exception.message

    // message size is 255
    exception = new ProcessNodeException(new Exception("a"*2000))
    assert "a"*2000 == exception.message

    // message size is more then 255
    exception = new ProcessNodeException(new Exception("a"*3000))
    assert "a"*2000 == exception.message
  }

}