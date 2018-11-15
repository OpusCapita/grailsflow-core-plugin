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
package management 

import com.jcatalog.grailsflow.actions.Action
import com.jcatalog.grailsflow.utils.AuthoritiesUtils
import org.apache.commons.logging.LogFactory

/**
 * UpdateAssignees action
 *  - updates assignees of specified nodes
 *  - updates assignees of next nodes (if nodes parameter is null)
 *
 * parameters:
 * nodes -- List or String of comma-separates values  
 * users -- List or String of comma-separates values
 * roles -- List or String of comma-separates values
 * groups -- List or String of comma-separates values
 *
 * @author Maria Voitovich
 */

class UpdateAssigneesAction extends Action {
    public def nodes
    public def roles
    public def users
    public def groups
    public def customAuthorities

    protected static def log = LogFactory.getLog(UpdateAssigneesAction.class)

    def execute() {
       def userAuthorities = AuthoritiesUtils.getUserAuthorities(getCollectionValue(users))
       def roleAuthorities = AuthoritiesUtils.getRoleAuthorities(getCollectionValue(roles))
       def groupAuthorities = AuthoritiesUtils.getGroupAuthorities(getCollectionValue(groups))
       def customAuthorities = AuthoritiesUtils.getCustomAuthorities(getCollectionValue(customAuthorities))
       def authorities = userAuthorities + roleAuthorities + groupAuthorities + customAuthorities
       if (authorities != null) {
         if (nodes) {
           nodes = getCollectionValue(nodes)
           def assigneesMap = [:]
           nodes.each() { node ->
             assigneesMap.put(node, authorities)
           }
           log.debug("Updating assignees for nodes ${nodes}. Set assignees to ${authorities}.")
           actionContext.assignees.putAll(assigneesMap)
         } else {
           log.debug("Updating assignees for next nodes. Set assignees to ${authorities}.")
           actionContext.nextAssignees.clear()
           actionContext.nextAssignees.addAll(authorities)
         }
       } else {
         log.error("Incorrect method signature UpdateAssignees. Assignees (users, nodes, groups) must not be null")
       }
       return null
    }

    /*
     * Evaluates if necessary value of Collection type from String of comma-separated values.
     *
     */
    private def getCollectionValue(def collection) {
        if (collection == null) return [];
        def collectionString = null
        if (collection instanceof Collection){
          return collection;
        } else {
          collectionString = collection.split(",").collect(){ it.trim().inspect() }.join(",")
	        def gs = new GroovyShell()
					return gs.evaluate("[$collectionString]")
        }
    }

}
