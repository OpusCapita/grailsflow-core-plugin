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

import com.jcatalog.grailsflow.engine.concurrent.ProcessLock

/**
 * Tests for synchronization with ProcessLock instances.
 *
 * @author Maria Voitoich
 */
class ProcessLockTests extends GroovyTestCase {

  void testConcurrentAccess() {
    def processes = [:]
    def processManager = [
      startProcess: { processID ->
        def process = new ArrayList()
        process << "start"
        processes[processID] = process
      },
      sendEvent: { processID, eventID ->
        synchronized(ProcessLock.getProcessLock(processID)) {
          processes[processID] << "${eventID}_start".toString()
          def duration = eventID.size() * 100
          sleep(duration)
          processes[processID] << "${eventID}_finish".toString()
          return
        }
      }
    ]

    def processID = new Long(1)
    processManager.startProcess(processID)
    
    def thread1 = [
      run: {
        processManager.sendEvent(processID, "very_slow_event")
      }
    ] as Thread

    def thread2 = [
      run: {
        processManager.sendEvent(processID, "quick")
      }
    ] as Thread
    
    thread1.start()
    sleep(100)
    thread2.start()
    
    thread1.join()
    thread2.join()
    
    assert ["start", "very_slow_event_start", "very_slow_event_finish", "quick_start", "quick_finish"] == processes[new Long(1)]
  }

}