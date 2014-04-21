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

package com.jcatalog.grailsflow.cache

import com.jcatalog.grailsflow.engine.ProcessBuilder
import org.christianschenk.simplecache.SimpleCache

/**
 * Implementation of CacheManager using SimpleCache
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class SimpleCacheManager implements CacheManager {
    // 100 days in seconds.
    private static long cacheExpireTimeOut = 8640000

    // cache structure for process builders
    def processesBuilders = new SimpleCache<ProcessBuilder>(cacheExpireTimeOut)

    def get(def processType) {
        return processesBuilders.get(processType)
    }

    def put(def processType, def builder) {
        processesBuilders.put(processType, builder)
    }
    
    def contains(def processType) {
        return processesBuilders.get(processType) != null
    }

    def remove(def processType) {
        processesBuilders.objects?.remove(processType)
    }
}