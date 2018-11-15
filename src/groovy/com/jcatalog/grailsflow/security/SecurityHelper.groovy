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
 * SecurityHelper interface.
 *
 *
 * @author Stephan Albers
 * @author July Karpey
 */

interface SecurityHelper {

    /**
     * Checks if controller actions are available for calling.
     *
     * @param session
     *
     * @return true if controller actions allowed
     */
    Boolean isControllerAccessible(def session)
    
    /**
     * Returns the username of logged user.
     *
     * @param session
     *
     * @return username or userID
     */
    String getUser(def session)

    /**
     * Returns the possible users associated with logged user (e.g. user substitutes another user).
     * Should be used for determination Authorities of logged user.
     *
     * @param session
     * @return
     */
    List<String> getUsers(def session)

    /**
     * Returns the roles list for logged user.
     *
     * @param session
     *
     * @return list of roles
     */
    List<String> getUserRoles(def session)
        
    /**
     * Returns the groups list for logged user.
     *
     * @param session
     *
     * @return list of groups
     */
    List<String> getUserGroups(def session)    

    /**
     * Returns the custom authorities list for logged user.
     *
     * @param session
     *
     * @return list of custom authorities
     */
    List<String> getCustomUserAuthorities(def session)
}