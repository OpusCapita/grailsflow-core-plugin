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
  Template for displaying processNodeDef List. 

  Template parameters:

  required: 
    * nodes                 collection of ProcessNodeDef objects

  optional:
    * showOperations        boolean flag for enabling/disabling operations links in list ("edit", "delete", etc). Default is false
 -->

<r:script>
  function orderMoveNodeUp(id) {
      jQuery.ajax({
          url: "${request.contextPath}/processNodeDef/orderMoveUp/"+id ,
          success:function(data) {
              afterMoveNodeUp(data)
          }
      })
  }

  function orderMoveNodeDown(id) {
      jQuery.ajax({
          url: "${request.contextPath}/processNodeDef/orderMoveDown/"+id ,
          success:function(data) {
              afterMoveNodeDown(data)
          }
      })
  }

  function moveNodeUp(oldOrder) {
    var row = document.getElementById("node_"+oldOrder)
    var newOrder = oldOrder - 1;
    var previousRow = document.getElementById("node_"+newOrder)

    // change rows IDs 
    row.id = "node_"+newOrder
    previousRow.id = "node_"+oldOrder

    // change rows order
    previousRow.parentNode.insertBefore(row, previousRow);

    // change rows IDs 
    row.className = (newOrder % 2 == 0 ? 'odd' : 'even')
    previousRow.className = (oldOrder % 2 == 0 ? 'odd' : 'even')
  }

  function afterMoveNodeUp(json){
    if (!json.orderChanged) {
      alert('Nothing changed')
      // TODO: show errors
      return;
    }
    moveNodeUp(json.oldOrder)
  }

  function afterMoveNodeDown(json){
    if (!json.orderChanged) {
      alert('Nothing changed')
      // TODO: show errors
      return;
    }
    // Moving down is moving next row up
    moveNodeUp(eval(json.oldOrder) + 1)
  }
</r:script>
 
 
<g:set var="showOperations" value="${showOperations != null ? showOperations : false}"/>

<gf:section title="${g.message(code: 'plugin.grailsflow.label.processNodes')}" selected="true">
<table class="table">
  <thead>
    <tr>
      <th><g:message code="plugin.grailsflow.label.nodeID"/></th>
      <th><g:message code="plugin.grailsflow.label.type"/></th>
      <th><g:message code="plugin.grailsflow.label.transitions"/></th>
      <g:if test="${showOperations}">
        <th width="15%">&nbsp;</th>
      </g:if>
     </tr>
  </thead>
  <tbody>
    <g:each in="${nodes}" var="node" status="i">
      <tr id="node_${i}"  class="${ (i % 2) == 0 ? 'odd' : 'even'}" valign="top">
        <td>${node.nodeID}</td>
        <td>${node.type}</td>
        <td>
          <table width="100%">
            <tr>
              <td>
                <g:each in="${node.transitions}" var="transition">
                  <g:link controller="processTransitionDef" action="editTransitonDef" id="${transition.id}">
                    <b>${transition.event}</b>
                  </g:link>
                  &nbsp;&nbsp;
                  <g:link title="${g.message(code:'plugin.grailsflow.command.delete')}" onclick="return askConfirmation('${g.message(code: 'plugin.grailsflow.question.confirm')}');" controller="processTransitionDef" action="deleteTransitonDef" id="${transition.id}">
                    <span class="glyphicon glyphicon-remove text-danger"></span>
                  </g:link>
                  <br/>
                  <font class="hint"> >> ${transition.toNodes*.nodeID}</font><br/>
                </g:each>
              </td>
              <td align="right">
                <g:link title="${g.message(code: 'plugin.grailsflow.command.addTransition')}" controller="processTransitionDef" action="addTransitonDef" id="${processDetails.id}" params="[fromNode: node.id]">
                  <span class="glyphicon glyphicon-plus text-success"></span>
                </g:link>
              </td>
            </tr>
          </table>
        </td>
        <g:if test="${showOperations}">
          <td>
            <div class="btn-group input-group-btn">
              <nobr>
                <a href="javascript:void(0)" title="${g.message(code: 'plugin.grailsflow.command.up')}" onclick="orderMoveNodeUp(${node.id}); return false;"
                    title="${g.message(code: 'plugin.grailsflow.command.up')}" class="btn btn-sm btn-link">
                  <span class="glyphicon glyphicon-arrow-up"></span>
                </a>
                &nbsp;
                <a href="javascript:void(0)" title="${g.message(code: 'plugin.grailsflow.command.down')}" onclick="orderMoveNodeDown(${node.id}); return false;"
                    title="${g.message(code: 'plugin.grailsflow.command.down')}" class="btn btn-sm btn-link">
                  <span class="glyphicon glyphicon-arrow-down"></span>
                </a>
                &nbsp;
                <g:link controller="processNodeDef" action="editNodeDef" id="${node.id}" title="${g.message(code: 'plugin.grailsflow.command.edit')}" class="btn btn-sm btn-default">
                  <span class="glyphicon glyphicon-edit"></span>&nbsp;
                  <g:message code="plugin.grailsflow.command.edit"/>
                </g:link>
                <g:link onclick="return askConfirmation('${g.message(code: 'plugin.grailsflow.question.confirm')}');" controller="processNodeDef" action="deleteNodeDef" id="${node.id}" title="${g.message(code: 'plugin.grailsflow.command.delete')}" class="btn btn-sm btn-default">
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
  <g:actionSubmit action="addNodeDef" value="${g.message(code: 'plugin.grailsflow.command.add')}" class="btn btn-primary"/>
</div>
</gf:section>
<br/><br/>
