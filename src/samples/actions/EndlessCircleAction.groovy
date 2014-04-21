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
package logging

import com.jcatalog.grailsflow.actions.Action
import org.apache.commons.logging.LogFactory

class EndlessCircleAction extends Action {
    protected static def log = LogFactory.getLog(EndlessCircleAction.class)

    def execute(){
        try {
                            while(1) {
                                sleep(500)
                                println("Action thread is running!")
                            }
                        } catch (InterruptedException ie) {
                            println("Internal thread was interrupted!")
                            throw ie
                        }

    }
}