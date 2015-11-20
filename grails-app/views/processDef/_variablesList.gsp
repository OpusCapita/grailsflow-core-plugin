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
      jQuery.ajax({
          url: "${request.contextPath}/processVarDef/orderMoveUp/"+id ,
          success:function(data) {
              afterMoveVarUp(data)
          }
      })
  }

  function orderMoveVarDown(id) {
      jQuery.ajax({
          url: "${request.contextPath}/processVarDef/orderMoveDown/"+id ,
          success:function(data) {
              afterMoveVarDown(data)
          }
      })
  }

  function moveUp(oldOrder) {
    var row = document.getElementById("variable_"+oldOrder)
    var newOrder = oldOrder - 1;
    var previousRow = document.getElementById("variable_"+newOrder)

    // change rows IDs 
    row.id = "variable_"+newOrder
    previousRow.id = "variable_"+oldOrder

    // change rows order
    previousRow.parentNode.insertBefore(row, previousRow);

    // change rows IDs 
    row.className = (newOrder % 2 == 0 ? 'odd' : 'even')
    previousRow.className = (oldOrder % 2 == 0 ? 'odd' : 'even')
  }

  function afterMoveVarUp(json){
    if (!json.orderChanged) {
      alert('Nothing changed')
      // TODO: show errors
      return;
    }
    moveUp(json.oldOrder)
  }

  function afterMoveVarDown(json){
    if (!json.orderChanged) {
      alert('Nothing changed')
      // TODO: show errors
      return;
    }
    // Moving down is moving next row up
    moveUp(eval(json.oldOrder) + 1)
  }
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
       <tr id="variable_${i}" class="${ (i % 2) == 0 ? 'odd' : 'even'}" valign="top">
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
                   <a href="javascript:void(0)" title="${g.message(code: 'plugin.grailsflow.command.up')}" onclick="orderMoveVarUp(${variable.id}); return false;"
                      title="${g.message(code: 'plugin.grailsflow.command.up')}" class="btn btn-sm btn-link">
                     <span class="glyphicon glyphicon-arrow-up"></span>
                   </a>
                   &nbsp;
                   <a href="javascript:void(0)" title="${g.message(code: 'plugin.grailsflow.command.down')}" onclick="orderMoveVarDown(${variable.id}); return false;"
                      title="${g.message(code: 'plugin.grailsflow.command.down')}" class="btn btn-sm btn-link">
                     <span class="glyphicon glyphicon-arrow-down"></span>
                   </a>
                   &nbsp;
                   <g:link controller="processVarDef" action="editVarDef" id="${variable.id}" title="${g.message(code: 'plugin.grailsflow.command.edit')}" class="btn btn-sm btn-default">
                     <span class="glyphicon glyphicon-edit"></span>&nbsp;
                       <g:message code="plugin.grailsflow.command.edit"/>
                   </g:link>
                   <g:link onclick="return askConfirmation('${g.message(code: 'plugin.grailsflow.question.confirm')}');" controller="processVarDef" action="deleteVarDef" id="${variable.id}" title="${g.message(code: 'plugin.grailsflow.command.delete')}" class="btn btn-sm btn-default">
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
