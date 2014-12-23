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


<gf:messageBundle bundle="grailsflow.processNodeEditor" var="nodeEditor"/>
<gf:messageBundle bundle="grailsflow.common" var="common"/>


<gf:section title="${nodeEditor['grailsflow.label.processNodes']}" selected="true">
<table class="table table-bordered">
  <thead>
    <tr>
      <th>${nodeEditor['grailsflow.label.nodeID']}</th>
      <th>${nodeEditor['grailsflow.label.type']}</th>
      <th>${nodeEditor['grailsflow.label.transitions']}</th>
      <g:if test="${showOperations}">
        <th width="15%">${common['grailsflow.label.operations']}</th>
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
                  <g:link onclick="return askConfirmation('${common['grailsflow.question.confirm']}');" controller="processTransitionDef" action="deleteTransitonDef" id="${transition.id}">
                    <img alt="${common['grailsflow.command.delete']}" src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/editor',file:'delete.gif')}"/>
                  </g:link>
                  <br/>
                  <font class="hint"> >> ${transition.toNodes*.nodeID}</font><br/>
                </g:each>
              </td>
              <td align="right">
                <g:link controller="processTransitionDef" action="addTransitonDef" id="${processDetails.id}" params="[fromNode: node.id]">
                  <img alt="${nodeEditor['grailsflow.command.addTransition']}" src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/editor',file:'add.gif')}"/>
                </g:link>
              </td>
            </tr>
          </table>
        </td>
        <g:if test="${showOperations}">
          <td>
            <g:remoteLink controller="processNodeDef" action="orderMoveUp" id="${node.id}"
                onSuccess="afterMoveNodeUp(data);"
                title="${common['grailsflow.command.up']}"><img alt="${common['grailsflow.command.up']}" src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/editor',file:'move_up.gif')}"/></g:remoteLink>
            &nbsp;&nbsp;
            <g:remoteLink controller="processNodeDef" action="orderMoveDown" id="${node.id}"
                onSuccess="afterMoveNodeDown(data);"
                title="${common['grailsflow.command.down']}"><img alt="${common['grailsflow.command.down']}" src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/editor',file:'move_down.gif')}"/></g:remoteLink>
            &nbsp;&nbsp;
            <g:link controller="processNodeDef" action="editNodeDef" id="${node.id}" title="${common['grailsflow.command.edit']}">${common['grailsflow.command.edit']}</g:link>
            &nbsp;&nbsp;
            <g:link onclick="return askConfirmation('${common['grailsflow.question.confirm']}');" controller="processNodeDef" action="deleteNodeDef" id="${node.id}" title="${common['grailsflow.command.delete']}">${common['grailsflow.command.delete']}</g:link>
          </td>
        </g:if>
      </tr>
    </g:each>
  </tbody>
</table>
<g:actionSubmit action="addNodeDef" value="${common['grailsflow.command.add']}" class="btn btn-primary"/>
</gf:section>
<br/><br/>
