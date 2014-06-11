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
        Template for selecting users/roles/groups. 

        Template parameters:

  required:     
                * usersParameterName            'name' attribute for user's input
                * rolesParameterName            'name' attribute for roles's input
                * groupsParameterName   'name' attribute for group's input

        optional:
          * switchParameterName         'name' attribute for radio-button that switches user/role/group selection. default value is 'authority_type'.
                * users                                                                 initial value for user's input. comma-separated values
                * usersCount                                            users count. if not specified then count of "users" parameter items used
                * roles                                                                 initial value for role's input
                * rolesCount                                            roles count. if not specified then count of "roles" parameter items used
    * groups                initial value for group's input
    * groupsCount           groups count. if not specified then count of "groups" parameter items used
                * userElements                                  comma-separated IDs of elements that should be visible when USER selection is enabled 
                * roleElements                                  comma-separated IDs of elements that should be visible when ROLE selection is enabled
                * groupElements         comma-separated IDs of elements that should be visible when GROUP selection is enabled
 -->

    <g:set var="users_count" value="${usersCount != null ? usersCount : (users ? users.split(',').size() : 0)}"/>
    <g:set var="roles_count" value="${rolesCount != null ? rolesCount : (roles ? roles.split(',').size() : 0)}"/>
    <g:set var="groups_count" value="${groupsCount != null ? groupsCount : (groups ? groups.split(',').size() : 0)}"/>
    <g:set var="select_users" value="${users_count != 0 || roles_count == 0 && groups_count == 0 }"/>
    <g:set var="select_roles" value="${ !select_users && roles_count != 0 }"/>
    <g:set var="select_groups" value="${ !select_users && !select_roles }"/>

    <g:set var="authority_type" value="${switchParameterName ? switchParameterName : 'authority_type'}"/>

    <r:script>
      function switchAuthoritiesType(selectedType) {
        document.getElementById('div_users').style.display = selectedType == 'users' ? '' : 'none'
        document.getElementById('div_roles').style.display = selectedType == 'roles' ? '' : 'none'
        document.getElementById('div_groups').style.display = selectedType == 'groups' ? '' : 'none'
        
        <g:if test="${usersCount == null}"> // recalculate count for hiding section if count is not passed as parameter
                if (selectedType != 'users') {
                  var users = document.getElementById('input_users').value.trim()
                  var usersCount = users.length > 0 ? users.split(",").length : 0;
                  document.getElementById("users_count").innerHTML="("+usersCount+")"
                }
        </g:if>
        <g:if test="${rolesCount == null}">  // recalculate count for hiding section if count is not passed as parameter
                if (selectedType != 'roles') {
                  var roles = document.getElementById('input_roles').value.trim();
                  var rolesCount = roles.length > 0 ? roles.split(",").length : 0;
                  document.getElementById("roles_count").innerHTML="("+rolesCount+")"
                }
        </g:if>
        <g:if test="${groupsCount == null}">  // recalculate count for hiding section if count is not passed as parameter
          if (selectedType != 'groups') {
            var groups = document.getElementById('input_groups').value.trim();
            var groupsCount = groups.length > 0 ? groups.split(",").length : 0;
            document.getElementById("groups_count").innerHTML="("+groupsCount+")"
          }
        </g:if>

        // show/hide user elements
        <g:each var="elementId" in="${userElements?.split(',')}">
          document.getElementById('${elementId.trim()}').style.display = selectedType == 'users' ? '' : 'none'
        </g:each>

        // show/hide role elements
        <g:each var="elementId" in="${roleElements?.split(',')}">
          document.getElementById('${elementId.trim()}').style.display = selectedType == 'roles' ? '' : 'none'
        </g:each>

        // show/hide group elements
        <g:each var="elementId" in="${groupElements?.split(',')}">
          document.getElementById('${elementId.trim()}').style.display = selectedType == 'groups' ? '' : 'none'
        </g:each>
      }

      function clearUsers() {
        document.getElementById('input_users').value="";
      }
      
      function addUsers(users) {
        var count = addItems('input_users', users);
        <g:if test="${usersCount == null}"> // recalculate count for section if count is not passed as parameter
          document.getElementById("users_count").innerHTML="("+count+")"
        </g:if>
      }

      function replaceUsers(users) {
        clearUsers();
        addUsers(roles);
      }

      function openUserList(){
        window.open("${g.createLink(controller: 'userRole', action:'listUsers')}", "ParamsWindow", 'width=450, height=400, resizable=yes, scrollbars=yes, status=no')
      }

      function clearRoles() {
        document.getElementById('input_roles').value="";
      }

      function addRoles(roles) {
        var count = addItems('input_roles', roles);
        <g:if test="${rolesCount == null}"> // recalculate count for section if count is not passed as parameter
          document.getElementById("roles_count").innerHTML="("+count+")"
        </g:if>
      }

      function replaceRoles(roles) {
        clearRoles();
        addRoles(roles);
      }

      function openRoleList(){
        window.open("${g.createLink(controller: 'userRole', action:'listRoles')}", "ParamsWindow", 'width=450, height=400, resizable=yes, scrollbars=yes, status=no')
      }
      
      function clearGroups() {
        document.getElementById('input_groups').value="";
      }
      
      function addGroups(groups) {
        var count = addItems('input_groups', groups);
        <g:if test="${groupsCount == null}"> // recalculate count for section if count is not passed as parameter
          document.getElementById("groups_count").innerHTML="("+count+")"
        </g:if>
      }

      function replaceGroups(groups) {
        clearGroups();
        addGroups(groups);
      }

      function openGroupList(){
        window.open("${g.createLink(controller: 'userRole', action:'listGroups')}", "ParamsWindow", 'width=450, height=400, resizable=yes, scrollbars=yes, status=no')
      }      
      
    </r:script>
    
    <gf:messageBundle bundle="grailsflow.userRoles" var="userRoles"/>
    
        <input type="radio" name="authority_type" value="users"
           onclick="if (this.checked) switchAuthoritiesType('users');" ${select_users ? 'checked' : ''}/>
    ${userRoles['grailsflow.label.users']} <span id="users_count">(${users_count})</span>
        &nbsp;&nbsp;&nbsp;
        <input type="radio" name="authority_type" value="roles"
           onclick="if (this.checked) switchAuthoritiesType('roles');" ${select_roles ? 'checked' : ''}/>
    ${userRoles['grailsflow.label.roles']}  <span id="roles_count">(${roles_count})</span>
    &nbsp;&nbsp;&nbsp;
    <input type="radio" name="authority_type" value="groups"
           onclick="if (this.checked) switchAuthoritiesType('groups');" ${select_groups ? 'checked' : ''}/>
    ${userRoles['grailsflow.label.groups']}  <span id="groups_count">(${groups_count})</span>
        <br/>

    <div id="div_users">
      <input type="text" id="input_users" name="${usersParameterName}" size="40" value="">&nbsp;
      <a href="#" onclick="openUserList();" class="image">
        <img src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/editor',file:'add.gif')}" title="${userRoles['grailsflow.title.findUsers']}" style="margin: 3px;"/>
      </a>&nbsp;
    </div>

    <div id="div_roles" style="display: none">
      <input type="text" id="input_roles" name="${rolesParameterName}" size="40" value="">&nbsp;
      <a href="#" onclick="openRoleList();" class="image">
        <img title="${userRoles['grailsflow.title.findRoles']}" src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/editor',file:'add.gif')}" style="margin: 3px;"/>
      </a>&nbsp;
    </div>
    <div id="div_groups" style="display: none">
      <input type="text" id="input_groups" name="${groupsParameterName}" size="40" value="">&nbsp;
      <a href="#" onclick="openGroupList();" class="image">
        <img title="${userRoles['grailsflow.title.findGroups']}" src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/editor',file:'add.gif')}" style="margin: 3px;"/>
      </a>&nbsp;
    </div>

