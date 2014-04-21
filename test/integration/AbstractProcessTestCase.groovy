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

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

import com.jcatalog.grailsflow.model.definition.*
import com.jcatalog.grailsflow.model.process.*
import org.hibernate.FlushMode
import com.jcatalog.grailsflow.model.graphics.ProcessNodePosition
import com.jcatalog.grailsflow.engine.execution.ExecutionResultEnum

/**
 * Base for testing processes execution
  *
 * @author Maria Voitovich
 */
public abstract class AbstractProcessTestCase extends GroovyTestCase
                                 implements ApplicationContextAware {
    def processManagerService
    def threadRuntimeInfoService
    ApplicationContext appContext
    def sessionFactory
    def transactional = false

    protected void setUp() {
        fullCleanup()
    }

    public static void fullCleanup(){
        try {
            BasicProcess.withNewSession { session->
                ProcessNodeDef.list()*.delete()
                ProcessVariableDef.list()*.delete()

                ProcessNode.list()*.delete()

                ProcessNodePosition.list()*.delete()

                ProcessAssignee.list()*.delete()

                ProcessVariable.list()*.delete()
                BasicProcess.list()*.delete()
                session.flush()
            }
        } catch (Exception e) {}
    }

    protected void tearDown(){
        fullCleanup()
    }

    public def invokeCurrentNodes() {
        def job = appContext.getBean("NodeActivatorJob")
        job.execute()
    }

   /**
    * asserts that error code is EXECUTED_SUCCESSFULLY
    */
    public void executeManualNode(def  processId, String nodeID, String event, String username, Map<String, Object> params) {
        def sendEventResult = processManagerService.sendEvent(processId, nodeID, event, username, params)
        sendEventResult == ExecutionResultEnum.EXECUTED_SUCCESSFULLY.value()
    }

   /**
    *  returns started processID or null if failed
    */
    public def startProcess(String processName, String username, Map<String, Object> params) {
      return processManagerService.startProcess(processName, username, params)
    }

    public void killProcess(def processId) {
      processManagerService.killProcess(processId, "test")
    }

    public void killAllProcesses() {
      BasicProcess.list()?.each() { process ->
        killProcess(process.id)
      }
    }

    def void setApplicationContext(ApplicationContext ctx) throws BeansException {
        appContext = ctx
    }
}