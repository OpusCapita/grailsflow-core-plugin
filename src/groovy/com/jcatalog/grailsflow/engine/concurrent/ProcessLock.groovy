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
 
package com.jcatalog.grailsflow.engine.concurrent;

/**
 * ProcessLock implements lock object for synchronizing actions over process.  
 *
 * @author Maria Voitovich
 */

class ProcessLock {
  private static Map<Long, ProcessLock> _registry = Collections.synchronizedMap([:])
  private Long processID
  
  private ProcessLock(Long processID) {
    this.processID = processID
  }
  
  String toString() {
    return "ProcessLock for process #${this.processID}".toString()
  }
  
  public static synchronized ProcessLock getProcessLock(Long processID) {
    ProcessLock processLock = _registry.get(processID)
    if (processLock == null) {
      processLock = new ProcessLock(processID)
      _registry.put(processID, processLock)
    }
    return processLock
  }

}
