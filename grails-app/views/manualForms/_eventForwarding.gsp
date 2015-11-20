<!--
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!--

	Template of forwarding UI for running process node.

	Parameters: 
	  * currentAssignees		collection of current node assigneeIDs

-->

    <gf:section sectionId="section_forwarding" title="${g.message(code: 'plugin.grailsflow.label.forwarding')}" selected="false">
        <div class="form-group">
            <label class="col-md-12 control-label">
                <g:message code="plugin.grailsflow.message.forwarding"/>
            </label>
        </div>
        <div class="form-group">
            <div class="col-md-12">
                <g:set var="users" value="${com.jcatalog.grailsflow.utils.AuthoritiesUtils.getUsers(currentAssignees).join(',')}"/>
                <g:set var="roles" value="${com.jcatalog.grailsflow.utils.AuthoritiesUtils.getRoles(currentAssignees).join(',')}"/>
                <g:set var="groups" value="${com.jcatalog.grailsflow.utils.AuthoritiesUtils.getGroups(currentAssignees).join(',')}"/>
                <g:render plugin="grailsflow" template="/common/userRoleInput"
                          model="[ 'usersParameterName': 'processNode_users', 'rolesParameterName': 'processNode_roles', 'groupsParameterName': 'processNode_groups',
                                  'users': users, 'roles': roles, 'groups': groups]"/>
            </div>
        </div>
        <div class="form-group">
            <div class="col-md-12">
                <g:submitButton id="processNode_eventForwarding" name="processNode_eventForwarding" value="${g.message(code: 'plugin.grailsflow.command.assign')}" class="btn btn-primary" />
            </div>
        </div>

    </gf:section>