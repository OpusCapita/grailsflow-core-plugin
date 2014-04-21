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
package com.jcatalog.grailsflow.engine

import org.springframework.beans.factory.InitializingBean

/**
 * ProcessFactory that caches Process classes
 *
 */
class GrailsflowCachingProcessFactory extends GrailsflowProcessFactory implements InitializingBean {
    def cacheManager

    public void afterPropertiesSet() throws Exception {
      super.afterPropertiesSet()
      if (cacheManager == null) {
          throw new Exception("CacheManager property must be set for ${this.getClass()} bean.")
      }
    }

    def getProcessClassForName(def processType) {
      def processClass
      synchronized(cacheManager) {
        try{
            // get Builder from cache
            processClass = cacheManager.get(processType)

            // if Builder is not in cache get Builder instance and put it to cache
            if (!processClass) {
                log.debug("Process processClass for process type ${processType} not found in cache. Compiling new instance from sources.")
                processClass = super.getProcessClassForName(processType)
                if (processClass != null) {
                    cacheManager.put(processType, processClass)
                    log.debug("Process class for process type ${processType} is stored in cache.")
                }
            } else {
               log.debug("Process class for process type ${processType} loaded from cache")
            }
        }catch (Throwable ex){
            log.error("Unexpected exception occurred in synchronized block! Please, contact to administrator. ", ex)
        }
      }
      return processClass
    }

    def removeProcessClass(def processType) {
      synchronized(cacheManager) {
        try{
            // remove processClass from cache
           cacheManager.remove(processType)
        }catch (Throwable ex){
           log.error("Unexpected exception occurred in synchronized block! Please, contact to administrator. ", ex)
        }    
      }
      super.removeProcessClass(processType)
    }

}