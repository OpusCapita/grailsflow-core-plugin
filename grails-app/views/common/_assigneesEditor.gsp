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
<r:script>

  //Asynchronous call for addAssignees
  function callAddAssignees(button) {
    const elements = getFilteredInputs(button.form, button.name);
    const data = new URLSearchParams(convertFormElementsToObject(elements))
    const url = "${g.createLink(controller: controller, action:'addAssignees')}"
    const options = {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
      },
      body: data
    }

    commonCallHandler(url, options, 'Add assignee failed', afterAddAssignees)

    return false;
  }

  function convertFormElementsToObject(elements) {
    return Object.fromEntries($(elements).serializeArray().map(item => [item.name, item.value]))
  }

  function commonCallHandler(url, options, errorMsg, callback) {
    const errorContainer = $('#errorContainer');
    errorContainer.addClass('hide');

    fetch(url, options)
      .then(response => {
        if (!response.ok) {
          throw new Error("Server error");
        }
        return response.json();
      })
      .then(data => {
        if (!data.success) {
          throw new Error(errorMsg);
        }
        callback(data)
      })
      .catch(error => {
        console.error(error);
        errorContainer.removeClass('hide');
      });
  }

  // Callback function for adding assignees
  function afterAddAssignees(json) {
    const type = json.authorityType
    const addedAssignees = json.addedAssignees
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
            removeLink.onclick = callDeleteAssignee.bind(null, parentForm, assignee)
            var divButton = document.createElement('div')
            divButton.className ="form-submit text-right"
            divButton.appendChild(removeLink)
            var linkCell = row.insertCell(1)
            linkCell.appendChild(divButton)
        }
    }
    document.getElementById(assigneesType+"_count").innerHTML="("+table.rows.length+")"
  }

  //Asynchronous call for deleteAssignee
  function callDeleteAssignee(form, assigneeID) {
    if (!askConfirmation('${g.message(code:'plugin.grailsflow.question.confirm')}')) {
        return false;
    }
    const data = {
      authority_type: form.authority_type.value,
      assigneeID: assigneeID
    }
    if (form.ndID) {
      data.ndID = form.ndID.value;
    } else {
      data.id = form.id.value;
    }
    const url = "${g.createLink(controller: controller, action:'deleteAssignee')}?" + new URLSearchParams(data)
    const options = {
      method: 'DELETE'
    }

    commonCallHandler(url, options, 'Delete assignee failed', afterDeleteAssignee)
    return false;
  }

  // Callback function for removing assignees
  function afterDeleteAssignee(json) {
    var type = json.authorityType
    var removedAssignee = json.removedAssignee
    if (!removedAssignee) {
        throw new Error("Assignee not removed");
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

<div class="row">
  <div class="col-md-6">
     <g:render plugin="grailsflow" template="/common/userRoleInput"
       model="[ 'usersParameterName': 'userAssignees', 'rolesParameterName': 'roleAssignees', 'groupsParameterName': 'groupAssignees',
       'usersCount': usersCount, 'rolesCount': rolesCount, 'groupsCount': groupsCount,
       'userElements': 'usersTable', 'roleElements': 'rolesTable', 'groupElements': 'groupsTable']"/>

     <div class="form-submit text-right">
       <input type="button" name="_action_addAssignees" value="${g.message(code:'plugin.grailsflow.command.add')}" class="btn btn-primary"
              onclick="return callAddAssignees(this);"/>
     </div>

     <a class="btn btn-sm btn-default" uri="javascript:void(0)" id="sample_assigee_delete_link" title="${g.message(code:'plugin.grailsflow.command.delete')}" style="display: none">
       <span class="glyphicon glyphicon-remove text-danger"></span>&nbsp;<g:message code="plugin.grailsflow.command.delete"/>
     </a>

     <br/>
     <br/>

<table class="table" id="usersTable" style="${userSelection ? '' : 'display: none'}">
 <thead>
   <tr>
      <th><g:message code="plugin.grailsflow.label.assignee"/></th>
      <th>&nbsp;</th>
    </tr>
 </thead>
 <tbody>
   <g:each in="${users}" var="user" status="i">
        <tr id="usersTable_${user?.encodeAsHTML()}_row" class="${ (i % 2) == 0 ? 'odd' : 'even'}" valign="top">
           <td>${user?.encodeAsHTML()}</td>
           <td>
             <div class="form-submit text-right">
               <g:link class="btn btn-sm btn-default" uri="javascript:void(0)" title="${g.message(code:'plugin.grailsflow.command.delete')}" onclick="return callDeleteAssignee(getAncestorElementOfType(this, 'form'), '${user?.encodeAsJavaScript()?.encodeAsHTML()}');">
                 <span class="glyphicon glyphicon-remove text-danger"></span>&nbsp;${g.message(code: 'plugin.grailsflow.command.delete')}
               </g:link>
             </div>
           </td>
        </tr>
   </g:each>
 </tbody>
</table>
<table class="table" id="rolesTable" ${roleSelection ? '' : 'style="display: none"'}>
 <thead>
   <tr>
      <th><g:message code="plugin.grailsflow.label.assignee"/></th>
      <th>&nbsp;</th>
    </tr>
 </thead>
 <tbody>
   <g:each in="${roles}" var="role" status="i">
        <tr id="rolesTable_${role?.encodeAsHTML()}_row" class="${ (i % 2) == 0 ? 'odd' : 'even'}" valign="top">
           <td>${role?.encodeAsHTML()}</td>
           <td>
             <div class="form-submit text-right">
               <g:link class="btn btn-sm btn-default" uri="javascript:void(0)" title="${g.message(code: 'plugin.grailsflow.command.delete')}" onclick="return callDeleteAssignee(getAncestorElementOfType(this, 'form'), '${role?.encodeAsJavaScript()?.encodeAsHTML()}');">
                 <span class="glyphicon glyphicon-remove text-danger"></span>&nbsp;<g:message code="plugin.grailsflow.command.delete"/>
               </g:link>
             </div>
           </td>
        </tr>
   </g:each>
 </tbody>
</table>
<table class="table" id="groupsTable" ${groupSelection ? '' : 'style="display: none"'}>
<thead>
 <tr>
    <th><g:message code="plugin.grailsflow.label.assignee"/></th>
    <th>&nbsp;</th>
  </tr>
</thead>
<tbody>
 <g:each in="${groups}" var="group" status="i">
      <tr id="groupsTable_${group?.encodeAsHTML()}_row" class="${ (i % 2) == 0 ? 'odd' : 'even'}" valign="top">
         <td>${group?.encodeAsHTML()}</td>
         <td>
           <div class="form-submit text-right">
             <g:link class="btn btn-sm btn-default" uri="javascript:void(0)" title="${g.message(code: 'plugin.grailsflow.command.delete')}" onclick="return callDeleteAssignee(getAncestorElementOfType(this, 'form'), '${group?.encodeAsJavaScript()?.encodeAsHTML()}');">
               <span class="glyphicon glyphicon-remove text-danger"></span>&nbsp;<g:message code="plugin.grailsflow.command.delete"/>
             </g:link>
           </div>
         </td>
      </tr>
 </g:each>
</tbody>
</table>

</div>
</div>
