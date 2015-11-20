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
  Template for rendering search results for ProcessList

  parameters:
    - processDetailsList  


-->

<r:script>
    function openGraphic(id) {
       window.open("${g.createLink(controller: "process", action:'showGraphic')}?processID="+id, "GraphicProcess", 'width=800, height=500, resizable=yes, scrollbars=yes, status=no')
    }
</r:script>

<g:set var="format" value="${new java.text.SimpleDateFormat('yyyy.MM.dd')}"/>

<g:if test="${processDetailsList}">
  <div class="table-responsive">
  <table class="table" width="100%">
  <thead>
    <tr>
         <th><g:message code="plugin.grailsflow.label.id"/></th>
         <th><g:message code="plugin.grailsflow.label.type"/></th>
         <th><g:message code="plugin.grailsflow.label.status"/></th>
         <th><g:message code="plugin.grailsflow.label.createdOnBy"/></th>
         <th><g:message code="plugin.grailsflow.label.modifiedOnBy"/></th>
         <th><g:message code="plugin.grailsflow.label.finishedOnBy"/></th>
         <th>&nbsp;</th>
    </tr>
  </thead>
  <tbody>
    <g:each in="${processDetailsList}" var="process">
      <tr>
          <td><g:link controller="${params['controller']}" action="showProcessDetails" id="${process.id}" title="Show Process Details">${process.id}</g:link></td>
          <td><gf:translatedValue translations="${process.label}" default="${process.type}"/></td>
          <td>${process.status?.statusID ? g.message(code: 'plugin.grailsflow.label.status.'+process.status?.statusID) : '-'}</td>
          <td><gf:displayDateTime value="${process.createdOn}"/>&nbsp;${process.createdBy}
          </td>
          <td><gf:displayDateTime value="${process.lastModifiedOn}"/>&nbsp;${process.lastModifiedBy}
          </td>
          <td><gf:displayDateTime value="${process.finishedOn}"/>&nbsp;${process.finishedBy}
          </td>
          <td class="text-right">
            <div class="btn-group input-group-btn form-submit text-right">
              <nobr>
              <g:if test="${!process.status.isFinal && (process.status.statusID != com.jcatalog.grailsflow.status.ProcessStatusEnum.KILLING.value())}">
                <g:link class="btn btn-sm btn-default" onclick="return askConfirmation('${g.message(code: 'plugin.grailsflow.question.confirm')}');" controller="${params['controller']}" action="killProcess" id="${process.id}" title="${g.message(code: 'plugin.grailsflow.command.kill')}"
                   params="['type': params.type, 'statusID': params.statusID,
                    'startedFrom': startedFrom, 'finishedFrom': finishedFrom]">
                  <g:message code="plugin.grailsflow.command.kill"/>
                </g:link>
              </g:if>
              <g:elseif test="${process.status.statusID == com.jcatalog.grailsflow.status.ProcessStatusEnum.KILLING.value()}">
                <g:link class="btn btn-sm btn-default" controller="${params['controller']}" action="search" params="${params}">
                  <g:message code="plugin.grailsflow.command.refresh"/>
                </g:link>
              </g:elseif>
              <g:elseif test="${process.status.isFinal && returnPage == 'deleteProcesses'}">
                  <g:link class="btn btn-sm btn-default" controller="${params['controller']}" action="deleteProcess" params="${['processId': process.id, 'returnPage': returnPage]}">
                    <span class="glyphicon glyphicon-remove text-danger"></span>&nbsp;<g:message code="plugin.grailsflow.command.delete"/>
                  </g:link>
              </g:elseif>
              <g:link class="btn btn-sm btn-default" onclick="openGraphic('${process.id}'); return false;"><g:message code="plugin.grailsflow.command.showGraphic"/></g:link>
              <g:link class="btn btn-sm btn-default" controller="${params['controller']}" action="exportProcess" params="${['processID': process.id, 'format': 'csv', 'extension': 'csv']}"><g:message code="plugin.grailsflow.command.exportCSV"/></g:link>
              <g:link class="btn btn-sm btn-default" controller="${params['controller']}" action="exportProcess" params="${['processID': process.id, 'format': 'excel', 'extension': 'xls']}"><g:message code="plugin.grailsflow.command.exportExcel"/></g:link>
             </nobr>
            </div>
          </td>
      </tr>
    </g:each>
  </tbody>
  </table>
  </div>
</g:if>