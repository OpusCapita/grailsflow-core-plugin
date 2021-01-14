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

import com.jcatalog.grailsflow.model.process.ProcessNode
import com.jcatalog.grailsflow.utils.AuthoritiesUtils

/**
 * Default implementation of SecurityHelper interface.
 *
 * @author Maria Voitovich
 */

class GrailsflowSecurityHelper implements SecurityHelper {

    /**
     * Default implementation allows all controllers for any user
     * @param session
     * @return true
     */
    Boolean isControllerAccessible(def session){
      return Boolean.TRUE
    }
    
    /**
     * Always return "grailsflow" as name of logged user.
     * @param session
     * @return "grailsflow"
     */
    String getUser(def session) {
      return "grailsflow";
    }

    /**
     * By default we return only logged user
     *
     * @param session
     * @return
     */
    List<String> getUsers(Object session) {
        return [getUser(session)]
    }

    /**
     * Always return ["GRAILSFLOW"] as roles of logged user.
     * @param session
     * @return ["GRAILSFLOW"]
     */
    List<String> getUserRoles(def session) {
      def roles = ["GRAILSFLOW"]
      return (List<String>)roles;
    }

    /**
     * Always return ["Grailsflow"] as groups of logged user.
     * @param session
     * @return ["Grailsflow"]
     */
    List<String> getUserGroups(def session) {
      def groups = ["Grailsflow"]
      return (List<String>)groups;
    }

    /**
     * Return custom authorities of logged user.
     * @param session
     * @return list of custom user authorities
     */
    List<String> getCustomUserAuthorities(def session) {
        return [];
    }

    @Override
    boolean hasNonAssigneeUserAccessToProcessNode(ProcessNode processNode) {
        return false
    }
}