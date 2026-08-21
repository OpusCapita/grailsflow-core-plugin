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
      orderMoveNodeCommon(id, 'Up', afterMoveNodeUp.bind(null, id));
  }

  function orderMoveNodeDown(id) {
      orderMoveNodeCommon(id, 'Down', afterMoveNodeDown.bind(null, id));
  }

  function orderMoveNodeCommon(id, direction, callback) {
    let url = '${request.contextPath}/processNodeDef/orderMove' + direction + '/' + id;
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
          const errorMessage = data.error && data.error.message ? data.error.message : 'Move node ' + direction + ' failed';
          throw new Error(errorMessage);
        }
        return callback(data);
      })
      .catch(error => {
        showInformationModalDialog('Error', error.message);
      });
  }

  function afterMoveNodeUp(id){
    const row = $('#node_' + id);
    const prevRow = row.prev();
    prevRow.insertAfter(row);

    adjustNodeRowStyles();
  }

  function afterMoveNodeDown(id){
    const row = $('#node_' + id);
    const nextRow = row.next();
    nextRow.insertBefore(row);

    adjustNodeRowStyles();
  }

  function adjustNodeRowStyles() {
    $('.process-node').removeClass('odd even first last');
    $('.process-node:first').addClass('first');
    $('.process-node:last').addClass('last');
    $('.process-node:nth-child(odd)').addClass('odd');
    $('.process-node:nth-child(even)').addClass('even');
  }

  function deleteTransitionDef(id) {
    deleteCommon('processTransitionDef', 'deleteTransitionDef', 'Delete transition failed', id, function() {
      $('#transition_' + id).remove();
    });
  }

  function deleteNodeDef(id) {
    deleteCommon('processNodeDef', 'deleteNodeDef', 'Delete node failed', id, function() {
      $('#node_' + id).remove();
      adjustNodeRowStyles();
    });
  }

  function deleteCommon(controller, action, errorMsg, id, callback) {
    if (!askConfirmation('${g.message(code: 'plugin.grailsflow.question.confirm')}')) {
      return;
    }
    let url = '${request.contextPath}/' + controller + '/' + action + '/' + id;
    fetch(url, {method: 'DELETE'})
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
          const errorMessage = data.error && data.error.message ? data.error.message : errorMsg;
          throw new Error(errorMessage);
        }
        callback()
      })
      .catch(error => {
        showInformationModalDialog('Error', error.message);
      });
  }

  $(function() {
    const processNodes = $('.process-node');
    processNodes.on('click', '.moveUp', function(el) {
      const row = $(el.delegateTarget);

      if (row.hasClass('first')) {
        return false;
      }

      const id = row.attr('id').replace('node_', '');
      orderMoveNodeUp(id);
      return false;
    });

    processNodes.on('click', '.moveDown', function(el) {
      const row = $(el.delegateTarget);

      if (row.hasClass('last')) {
        return false;
      }

      const id = row.attr('id').replace('node_', '');
      orderMoveNodeDown(id);
      return false;
    });
  });
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
      <g:set var="isFirst" value="${i == 0}"/>
      <g:set var="isLast" value="${i == nodes.size() - 1}"/>
      <tr id="node_${node.id}" class="${ (i % 2) == 0 ? 'odd' : 'even'}${isFirst ? ' first' : ''}${isLast ? ' last' : ''} process-node" valign="top">
        <td>${node.nodeID}</td>
        <td>${node.type}</td>
        <td>
          <table width="100%">
            <tr>
              <td>
                <g:each in="${node.transitions}" var="transition">
                  <div id="transition_${transition.id}">
                    <g:link controller="processTransitionDef" action="editTransitonDef" id="${transition.id}">
                      <b>${transition.event}</b>
                    </g:link>
                    &nbsp;&nbsp;
                    <g:link title="${g.message(code:'plugin.grailsflow.command.delete')}" onclick="deleteTransitionDef('${transition.id}'); return false" uri="javascript:void(0)">
                      <span class="glyphicon glyphicon-remove text-danger"></span>
                    </g:link>
                    <br/>
                    <span class="hint"> >> ${transition.toNodes*.nodeID}</span>
                  </div>
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
                <a href="javascript:void(0)" title="${g.message(code: 'plugin.grailsflow.command.up')}" class="btn btn-sm btn-link moveUp">
                  <span class="glyphicon glyphicon-arrow-up"></span>
                </a>
                &nbsp;
                <a href="javascript:void(0)" title="${g.message(code: 'plugin.grailsflow.command.down')}" class="btn btn-sm btn-link moveDown">
                  <span class="glyphicon glyphicon-arrow-down"></span>
                </a>
                &nbsp;
                <g:link controller="processNodeDef" action="editNodeDef" id="${node.id}" title="${g.message(code: 'plugin.grailsflow.command.edit')}" class="btn btn-sm btn-default">
                  <span class="glyphicon glyphicon-edit"></span>&nbsp;
                  <g:message code="plugin.grailsflow.command.edit"/>
                </g:link>
                <g:link onclick="deleteNodeDef('${node.id}'); return false" uri="javascript:void(0)" title="${g.message(code: 'plugin.grailsflow.command.delete')}" class="btn btn-sm btn-default">
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
