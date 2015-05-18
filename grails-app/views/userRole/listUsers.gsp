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
         <gf:messageBundle bundle="grailsflow.common" var="common"/>
         <gf:messageBundle bundle="grailsflow.userRoles" var="msgs"/>
         <title>${msgs['grailsflow.title.users']}</title>
         <r:require modules="jquery" />
         <r:script>
           function addUser(user) {
             opener.addUsers(user)
           }
           
           function addUserAndClose(user) {
             addUser(user);
             window.close;
           }
         </r:script>
         <r:layoutResources/>
    </head>

    <body>
    <div class="container">
      <h1>${msgs['grailsflow.label.users']}</h1>

      <div class="row">
        <div class="col-md-6 col-xs-6 col-lg-6 col-sm-6">
	       <g:form controller="userRoles" method="POST">
	        <table id="usersTable" class="table table-bordered">
	          <tr>
	            <th>${msgs['grailsflow.label.user']}</th>
	          </tr>
	          <g:each var="user" in="${users}">
	            <tr>
	              <td><a href="#" onclick="addUser(jQuery(this).text())">${user}</a></td>
	            </tr>
	          </g:each>
	        </table>
	      </g:form>
	      <input type="button" class="btn btn-primary" value="${common['grailsflow.command.close']}" onclick="window.close();"/>
	    </div>
      </div>
    </div>
    <r:layoutResources/>
    </body>
</html>
