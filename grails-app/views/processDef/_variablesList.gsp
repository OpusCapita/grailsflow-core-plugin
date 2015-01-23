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

  function afterMoveUp(json){
    if (!json.orderChanged) {
      alert('Nothing changed')
      // TODO: show errors
      return;
    }
    moveUp(json.oldOrder)
  }

  function afterMoveDown(json){
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


<gf:messageBundle bundle="grailsflow.processVariableEditor" var="variableEditor"/>
<gf:messageBundle bundle="grailsflow.common" var="common"/>

 <gf:section title="${variableEditor['grailsflow.label.processVars']}" selected="true">
 <table class="table table-bordered">
     <thead>
       <th>${variableEditor['grailsflow.label.name']}</th>
       <th>${variableEditor['grailsflow.label.type']}</th>
       <th>${variableEditor['grailsflow.label.value']}</th>
       <th width="10%">${variableEditor['grailsflow.label.processIdentifier']}</th>
       <th width="10%">${variableEditor['grailsflow.label.required']}</th>
       <g:if test="${showOperations}">
         <th width="15%">${common['grailsflow.label.operations']}</th>
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
                 <td style="white-space: nowrap;">
                   <g:remoteLink controller="processVarDef" action="orderMoveUp" id="${variable.id}"
                      onSuccess="afterMoveUp(data);"
                      title="${common['grailsflow.command.up']}"><img alt="${common['grailsflow.command.up']}" src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/editor',file:'move_up.gif')}"/></g:remoteLink>
                   &nbsp;&nbsp;
                   <g:remoteLink controller="processVarDef" action="orderMoveDown" id="${variable.id}"
                      onSuccess="afterMoveDown(data);"
                      title="${common['grailsflow.command.down']}"><img alt="${common['grailsflow.command.down']}" src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/editor',file:'move_down.gif')}"/></g:remoteLink>
                   &nbsp;&nbsp;
                   <g:link controller="processVarDef" action="editVarDef" id="${variable.id}" title="${common['grailsflow.command.edit']}">${common['grailsflow.command.edit']}</g:link>
                   &nbsp;&nbsp;
                   <g:link onclick="return askConfirmation('${common['grailsflow.question.confirm']}');" controller="processVarDef" action="deleteVarDef" id="${variable.id}" title="${common['grailsflow.command.delete']}">${common['grailsflow.command.delete']}</g:link>
                 </td>
               </g:if>
       </tr>
     </g:each>
   </tbody>
 </table>
 <g:actionSubmit action="addVarDef" value="${common['grailsflow.command.add']}" class="btn btn-primary"/>
 </gf:section>
 <br/><br/>
