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

<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <g:render plugin="grailsflow" template="/commons/global"/>
         <title><g:message code="plugin.grailsflow.title.roles"/></title>
         <r:require modules="jquery" />
         <r:require modules="bootstrap"/>
         <r:script>
           function addRole(role) {
             opener.addRoles(role)
           }
           
           function addRoleAndClose(role) {
             addRole(role);
             window.close;
           }
         </r:script>
         <r:layoutResources/>
    </head>

    <body>
    <div class="container">

	  <h1><g:message code="plugin.grailsflow.label.roles"/></h1>

      <div class="row">
        <div class="col-md-6">
	      <g:form controller="userRoles" method="POST">
	        <table id="rolesTable" class="standard" width="100%">
	          <tr>
	            <th><g:message code="plugin.grailsflow.label.role"/></th>
	          </tr>
	          <g:each var="role" in="${roles}">
	            <tr>
	              <td><a href="#" onclick="addRole(jQuery(this).text())">${role}</a></td>
	            </tr>
	          </g:each>
	        </table>
	      </g:form>
	      <input type="button" class="btn btn-primary" value="${g.message(code: 'plugin.grailsflow.command.close')}" onclick="window.close();"/>
	    </div>
      </div>
    </div>
    <r:layoutResources/>
    </body>
</html>
