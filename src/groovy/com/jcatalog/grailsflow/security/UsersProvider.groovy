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

package com.jcatalog.grailsflow.security

/**
 * UsersProvider is interface for getting all available users
 *
 * @author Maria Voitovich
 */

interface UsersProvider {

    /**
     * Returns the usernames of all available users.
     *
     * @return list of usernames
     */
    Collection<String> getUsers()

}