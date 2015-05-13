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
  Template for displaying processAssignees form.

  Template parameters:

  required:
    * assignees             collection of ProcessDefAssignees objects
    * controller            name of controller to process assignees management

 -->

<gf:messageBundle bundle="grailsflow.common" var="common"/>
<gf:messageBundle bundle="grailsflow.processNodeEditor" var="nodeEditor"/>

<r:script>

  //Asynchronous call for addAssignees
  function callAddAssignees(button) {
    var elements = getFilteredInputs(button.form, button.name);
    var parameters = jQuery(elements).serializeArray();
    jQuery.getJSON("${g.createLink(controller: controller, action:'addAssignees')}",
            parameters, function(data, textStatus, jqXHR){afterAddAssignees(data, textStatus, jqXHR);});
    return false;
  }

  // Callback function for adding assignees
  function afterAddAssignees(json, textStatus, jqXHR) {
        var type = json.authorityType
        var addedAssignees = json.addedAssignees
            if (!addedAssignees || addedAssignees.length == 0 || addedAssignees == '') {
              alert("There are no new assignees specified. Please fill assignees input!")
              return;
            }
            switch (type) {
              case "users":
                addAssignees("users", addedAssignees)
                clearUsers()
                break;
              case "roles":
                addAssignees("roles", addedAssignees)
                clearRoles()
                break;
              case "groups":
                addAssignees("groups", addedAssignees)
                clearGroups()
                break;
              default:
                break;
            }
  }

  function addAssignees(assigneesType, assignees) {
    var assigneesTableID = assigneesType+"Table"
    var table = document.getElementById(assigneesTableID)
    var parentForm = getAncestorElementOfType(table, "form")
    if ( table.tBodies.length != 0 ) {
      table = table.tBodies[0]
    }
    for (i=0; i<assignees.length; ++i) {
        var assignee = assignees[i]

        if (assignee != '') {
            // add new row
            var rowsCount = table.rows.length
            var row = table.insertRow(rowsCount)
            row.id = assigneesTableID+'_'+assignee+'_row'
            row.className = (rowsCount % 2 == 0 ? 'odd' : 'even')

            jQuery(row.insertCell(0)).text(assignee);

            // remove link
            var removeLink = cloneSampleElement('sample_assigee_delete_link')
            removeLink.onclick = getDeleteAssigneeFunction(parentForm, assignee)
            var linkCell = row.insertCell(1)
            linkCell.appendChild(removeLink)
        }
    }
    document.getElementById(assigneesType+"_count").innerHTML="("+table.rows.length+")"
  }

  function getDeleteAssigneeFunction(form, assigneeID) {
    return function () {
      return callDeleteAssignee(form, assigneeID)
    }
  }

  //Asynchronous call for deleteAssignee
  function callDeleteAssignee(form, assigneeID) {
    if (askConfirmation('${common['grailsflow.question.confirm']}')) {
      var elements = getFilteredInputs(form)
      var assigneeElement = document.createElement("input")
      assigneeElement.name = "assigneeID"
      assigneeElement.value = assigneeID
      elements.push( assigneeElement )
      var parameters = jQuery(elements).serializeArray();
      jQuery.getJSON("${g.createLink(controller: controller, action:'deleteAssignee')}",
        parameters,
        function(data, textStatus, jqXHR){afterDeleteAssignee(data, textStatus, jqXHR);}
      )
    }
    return false;
  }

  // Callback function for removing assignees
  function afterDeleteAssignee(json, textStatus, jqXHR) {
    var type = json.authorityType
    var removedAssignee = json.removedAssignee
    if (!removedAssignee) {
      // TODO: show errors
      return;
    }
    switch (type) {
      case "users":
        removeAssignee("users", removedAssignee)
        break;
      case "roles":
        removeAssignee("roles", removedAssignee)
        break;
      case "groups":
        removeAssignee("groups", removedAssignee)
        break;
      default:
        break;
    }
  }

  function removeAssignee(assigneesType, assignee) {
    var assigneesTableID = assigneesType+"Table"
    var table = document.getElementById(assigneesTableID)
    var row = document.getElementById(assigneesTableID+'_'+assignee+'_row')
    table.deleteRow(row.rowIndex)
    if ( table.tBodies.length != 0 ) {
      table = table.tBodies[0]
    }
    document.getElementById(assigneesType+"_count").innerHTML="("+table.rows.length+")"
  }
</r:script>


<g:set var="assignees" value="${assignees?.assigneeID}"/>
<g:set var="users" value="${com.jcatalog.grailsflow.utils.AuthoritiesUtils.getUsers(assignees)}"/>
<g:set var="roles" value="${com.jcatalog.grailsflow.utils.AuthoritiesUtils.getRoles(assignees)}"/>
<g:set var="groups" value="${com.jcatalog.grailsflow.utils.AuthoritiesUtils.getGroups(assignees)}"/>
<g:set var="usersCount" value="${users.size()}"/>
<g:set var="rolesCount" value="${roles.size()}"/>
<g:set var="groupsCount" value="${groups.size()}"/>
<g:set var="userSelection" value="${usersCount != 0 || rolesCount == 0 && groupsCount == 0 }"/>
<g:set var="roleSelection" value="${ !userSelection && rolesCount != 0 }"/>
<g:set var="groupSelection" value="${ !userSelection && !roleSelection }"/>

<g:render plugin="grailsflow" template="/common/userRoleInput"
   model="[ 'usersParameterName': 'userAssignees', 'rolesParameterName': 'roleAssignees', 'groupsParameterName': 'groupAssignees',
       'usersCount': usersCount, 'rolesCount': rolesCount, 'groupsCount': groupsCount,
       'userElements': 'usersTable', 'roleElements': 'rolesTable', 'groupElements': 'groupsTable']"/>
<input type="button" name="_action_addAssignees" value="${common['grailsflow.command.add']}" class="button"
  onclick="return callAddAssignees(this);"/>

<a href="${g.createLink(controller: controller, action: 'deleteAssignee')}" id="sample_assigee_delete_link" title="${common['grailsflow.command.delete']}" style="display: none">${common['grailsflow.command.delete']}</a>

<br/>
<br/>

<table class="standard" id="usersTable" ${userSelection ? '' : 'style="display: none"'}>
 <thead>
   <tr>
      <th>${nodeEditor['grailsflow.label.assignee']}</th>
      <th>${common['grailsflow.label.operations']}</th>
    </tr>
 </thead>
 <tbody>
   <g:each in="${users}" var="user" status="i">
        <tr id="usersTable_${user?.encodeAsHTML()}_row" class="${ (i % 2) == 0 ? 'odd' : 'even'}" valign="top">
           <td>${user?.encodeAsHTML()}</td>
           <td>
             <g:link onclick="return callDeleteAssignee(getAncestorElementOfType(this, 'form'), '${user?.encodeAsJavaScript()?.encodeAsHTML()}');">
               ${common['grailsflow.command.delete']}
             </g:link>
           </td>
        </tr>
   </g:each>
 </tbody>
</table>
<table class="standard" id="rolesTable" ${roleSelection ? '' : 'style="display: none"'}>
 <thead>
   <tr>
      <th>${nodeEditor['grailsflow.label.assignee']}</th>
      <th>${common['grailsflow.label.operations']}</th>
    </tr>
 </thead>
 <tbody>
   <g:each in="${roles}" var="role" status="i">
        <tr id="rolesTable_${role?.encodeAsHTML()}_row" class="${ (i % 2) == 0 ? 'odd' : 'even'}" valign="top">
           <td>${role?.encodeAsHTML()}</td>
           <td>
             <g:link onclick="return callDeleteAssignee(getAncestorElementOfType(this, 'form'), '${role?.encodeAsJavaScript()?.encodeAsHTML()}');">
               ${common['grailsflow.command.delete']}
             </g:link>
           </td>
        </tr>
   </g:each>
 </tbody>
</table>
<table class="standard" id="groupsTable" ${groupSelection ? '' : 'style="display: none"'}>
<thead>
 <tr>
    <th>${nodeEditor['grailsflow.label.assignee']}</th>
    <th>${common['grailsflow.label.operations']}</th>
  </tr>
</thead>
<tbody>
 <g:each in="${groups}" var="group" status="i">
      <tr id="groupsTable_${group?.encodeAsHTML()}_row" class="${ (i % 2) == 0 ? 'odd' : 'even'}" valign="top">
         <td>${group?.encodeAsHTML()}</td>
         <td>
           <g:link onclick="return callDeleteAssignee(getAncestorElementOfType(this, 'form'), '${group?.encodeAsJavaScript()?.encodeAsHTML()}');">
             ${common['grailsflow.command.delete']}
           </g:link>
         </td>
      </tr>
 </g:each>
</tbody>
</table>

