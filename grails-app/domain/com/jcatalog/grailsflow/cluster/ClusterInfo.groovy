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

package com.jcatalog.grailsflow.cluster


/**
 * ClusterInfo domain class is used to store information about cluster
 *
 * @author Stephan Albers
 * @author July Antonicheva
 */
class ClusterInfo {
    String clusterName
    String description
    Date lastCheckedOn
    Long repeatInterval

    static constraints = {
        clusterName(unique: true)
        description(nullable: true, blank: true)
    }

}
