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
        Template for displaying processVarDef List. 

        Template parameters:

  required:     
                * variables                                             collection of ProcessVariableDef objects

        optional:
          * showOperations                              boolean flag for enabling/disabling operations links in list ("edit", "delete", etc). Default is false
 -->

<r:script>
  function orderMoveVarUp(id) {
    orderMoveVarCommon(id, 'Up', afterMoveVarUp.bind(null, id));
  }

  function orderMoveVarDown(id) {
    orderMoveVarCommon(id, 'Down', afterMoveVarDown.bind(null, id));
  }

  function orderMoveVarCommon(id, direction, callback) {
    let url = '${request.contextPath}/processVarDef/orderMove' + direction + '/' + id;
    fetch(url, {method: 'POST'})
      .then(response => {
        if (!response.ok) {
          throw new Error('${message(code: 'plugin.grailsflow.internalError')}');
        }
        try {
          return response.json();
        } catch (e) {
          throw new Error('${message(code: 'plugin.grailsflow.internalError')}');
        }
      })
      .then(data => {
        if (!data.success) {
          const errorMessage = data.error && data.error.message ? data.error.message : 'Move variable ' + direction + ' failed';
          throw new Error(errorMessage);
        }
        return callback(data);
      })
      .catch(error => {
        showInformationModalDialog('Error', error.message);
      });
  }

  function afterMoveVarUp(id) {
    const row = $("#variable_" + id)
    const prevRow = row.prev();
    prevRow.insertAfter(row);

    adjustVariableRowStyles();
  }

  function afterMoveVarDown(id) {
    const row = $("#variable_" + id)
    const nextRow = row.next();
    nextRow.insertBefore(row);

    adjustVariableRowStyles();
  }

  function deleteVarDef(id) {
    deleteCommon('processVarDef', 'deleteVarDef', 'Delete variable failed', id, function() {
      $('#variable_' + id).remove();
      adjustVariableRowStyles();
    });
  }

  function adjustVariableRowStyles() {
    $('.process-variable').removeClass('odd even first last');
    $('.process-variable:first').addClass('first');
    $('.process-variable:last').addClass('last');
    $('.process-variable:nth-child(odd)').addClass('odd');
    $('.process-variable:nth-child(even)').addClass('even');
  }

  $(function() {
    const processVariables = $('.process-variable');
    processVariables.on('click', '.moveUp', function(el) {
      const row = $(el.delegateTarget);

      if (row.hasClass('first')) {
        return false;
      }

      const id = row.attr('id').replace('variable_', '');
      orderMoveVarUp(id);
      return false;
    });

    processVariables.on('click', '.moveDown', function(el) {
      const row = $(el.delegateTarget);

      if (row.hasClass('last')) {
        return false;
      }

      const id = row.attr('id').replace('variable_', '');
      orderMoveVarDown(id);
      return false;
    });
  });
</r:script>
 
 
<g:set var="showOperations" value="${showOperations != null ? showOperations : false}"/>
 <gf:section title="${g.message(code: 'plugin.grailsflow.label.processVars')}" selected="true">
 <table class="table">
     <thead>
       <th><g:message code="plugin.grailsflow.label.name"/></th>
       <th><g:message code="plugin.grailsflow.label.type"/></th>
       <th><g:message code="plugin.grailsflow.label.value"/></th>
       <th width="10%"><g:message code="plugin.grailsflow.label.processIdentifier"/></th>
       <th width="10%"><g:message code="plugin.grailsflow.label.required"/></th>
       <g:if test="${showOperations}">
         <th width="15%">&nbsp;</th>
       </g:if>
     </thead>
     <tbody>
     <g:each in="${variables}" var="variable" status="i">
       <g:set var="isFirst" value="${i == 0}"/>
       <g:set var="isLast" value="${i == variables.size() - 1}"/>
       <tr id="variable_${variable.id}" class="${ (i % 2) == 0 ? 'odd' : 'even'}${isFirst ? ' first' : ''}${isLast ? ' last' : ''} process-variable" valign="top">
         <td>${variable.name?.encodeAsHTML()}</td>
         <td>${variable.type}</td>
         <td>
           <g:if test="${variable.type == 'Date'}">
             <gf:displayDate value="${variable.defaultValue ? new Date(new Long(variable.defaultValue)) : null}"/>
           </g:if>
           <g:elseif test="${variable.type == 'Link'}">
             <gf:renderLink link="${variable.linkValue}"/>
           </g:elseif>
           <g:else>
               ${variable.value?.encodeAsHTML()}
           </g:else>
         </td>
         <td>
           <g:if test="${variable.isProcessIdentifier && variable.isProcessIdentifier == Boolean.TRUE}">
             <img src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/editor', file:'checked.gif')}"/>
           </g:if>
           <g:else>
              &nbsp;
           </g:else>
         </td>
         <td>
           <g:if test="${variable.required && variable.required == Boolean.TRUE}">
             <img src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/editor', file:'checked.gif')}"/>
           </g:if>
           <g:else>
              &nbsp;
           </g:else>
         </td>
         <g:if test="${showOperations}">
           <td>
             <div class="btn-group input-group-btn">
               <nobr>
                   <a href="javascript:void(0)" title="${g.message(code: 'plugin.grailsflow.command.up')}" class="btn btn-sm btn-link moveUp">
                     <span class="glyphicon glyphicon-arrow-up"></span>
                   </a>
                   &nbsp;
                   <a href="javascript:void(0)" title="${g.message(code: 'plugin.grailsflow.command.down')}" class="btn btn-sm btn-link moveDown">
                     <span class="glyphicon glyphicon-arrow-down"></span>
                   </a>
                   &nbsp;
                   <g:link controller="processVarDef" action="editVarDef" id="${variable.id}" title="${g.message(code: 'plugin.grailsflow.command.edit')}" class="btn btn-sm btn-default">
                     <span class="glyphicon glyphicon-edit"></span>&nbsp;
                       <g:message code="plugin.grailsflow.command.edit"/>
                   </g:link>
                   <g:link onclick="deleteVarDef('${variable.id}');" uri="javascript:void(0)" title="${g.message(code: 'plugin.grailsflow.command.delete')}" class="btn btn-sm btn-default">
                     <span class="glyphicon glyphicon-remove text-danger"></span>&nbsp;
                     <g:message code="plugin.grailsflow.command.delete"/>
                   </g:link>
                 </nobr>
               </div>
           </td>
         </g:if>
       </tr>
     </g:each>
   </tbody>
 </table>
 <div class="form-submit text-right">
   <g:actionSubmit action="addVarDef" value="${g.message(code: 'plugin.grailsflow.command.add')}" class="btn btn-primary"/>
 </div>
 </gf:section>
