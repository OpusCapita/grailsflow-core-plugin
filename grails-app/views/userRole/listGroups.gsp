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
         <title>${msgs['grailsflow.title.groups']}</title>
         <r:require modules="jquery" />
         <r:script>
           function addGroup(group) {
             opener.addGroups(group)
           }
           
           function addGroupAndClose(group) {
             addGroup(group);
             window.close;
           }
         </r:script>
         <r:layoutResources/>
    </head>

    <body>
	    <div class="body">
	       <b class="header">${msgs['grailsflow.label.groups']}</b>
	       <br/>

	       <g:form controller="userGroups" method="POST">
	        <table id="groupsTable" cellspacing="3" class="blockLayout" width="100%">
	          <tr>
	            <th>${msgs['grailsflow.label.group']}</th>
	          </tr>
	          <g:each var="group" in="${groups}">
	            <tr>
	              <td><a href="#" onclick="addGroup(jQuery(this).text())">${group}</a></td>
	            </tr>
	          </g:each>
	        </table>
	      </g:form>
	      <input type="button" class="button" value="${common['grailsflow.command.close']}" onclick="window.close();"/>
	    </div>
        <r:layoutResources/>
    </body>
</html>
