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
 * ProcessTypeLock implements lock object for synchronizing actions over process.
 *
 * @author Maria Voitovich
 */

class ProcessTypeLock {
  private static Map<String, ProcessTypeLock> _registry = Collections.synchronizedMap([:])
  private String processType
  
  private ProcessTypeLock(String processType) {
    this.processType = processType
  }
  
  String toString() {
    return "ProcessTypeLock for process type '${this.processType}'".toString()
  }
  
  public static synchronized ProcessTypeLock getLock(String processType) {
    ProcessTypeLock processTypeLock = _registry.get(processType)
    if (processTypeLock == null) {
      processTypeLock = new ProcessTypeLock(processType)
      _registry.put(processType, processTypeLock)
    }
    return processTypeLock
  }

}
