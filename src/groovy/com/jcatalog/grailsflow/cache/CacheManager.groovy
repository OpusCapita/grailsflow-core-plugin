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

/**
 * CacheManager interface
 *
 * @author Stephan Albers
 * @author July Karpey
 */
interface CacheManager {

    /*
     * Puts cached object to the cache
     */
    def put(def key, def value)

    /*
     * Gets cached object by key
     */
    def get(def key)

    /*
     * Returns true if the cache contains value
     */
    def contains(def value)

    /*
     * Removes value from cached objects
     */
    def remove(def value)

}