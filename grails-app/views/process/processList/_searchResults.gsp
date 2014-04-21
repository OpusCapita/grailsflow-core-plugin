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

<gf:messageBundle bundle="grailsflow.common" var="common"/>
<gf:messageBundle bundle="grailsflow.processDetails" var="processDetails"/>

<r:script>
    function openGraphic(id) {
       window.open("${g.createLink(controller: "process", action:'showGraphic')}?processID="+id, "GraphicProcess", 'width=700, height=500, resizable=yes, scrollbars=yes, status=no')
    }
</r:script>

<g:set var="format" value="${new java.text.SimpleDateFormat('yyyy.MM.dd')}"/>

<br/><h2 class="headline">${processDetails['grailsflow.label.processList']}</h2>
<table class="grid" width="100%">
<thead>
  <tr>
       <th>${processDetails['grailsflow.label.id']}</th>
       <th>${processDetails['grailsflow.label.type']}</th>
       <th>${processDetails['grailsflow.label.status']}</th>
       <th>${processDetails['grailsflow.label.createdOnBy']}</th>
       <th>${processDetails['grailsflow.label.modifiedOnBy']}</th>
       <th>${processDetails['grailsflow.label.finishedOnBy']}</th>
       <th>${processDetails['grailsflow.label.operation']}</th>
  </tr>
</thead>
<tbody>
  <g:each in="${processDetailsList}" var="process">
    <tr>
        <td><g:link controller="${params['controller']}" action="showProcessDetails" id="${process.id}" title="Show Process Details">${process.id}</g:link></td>
        <td><gf:translatedValue translations="${process.label}" default="${process.type}"/></td>
        <td>${process.status?.statusID ? common['grailsflow.label.status.'+process.status?.statusID] : '-'}</td>
        <td><gf:displayDateTime value="${process.createdOn}"/>&nbsp;${process.createdBy}
        </td>
        <td><gf:displayDateTime value="${process.lastModifiedOn}"/>&nbsp;${process.lastModifiedBy}
        </td>
        <td><gf:displayDateTime value="${process.finishedOn}"/>&nbsp;${process.finishedBy}
        </td>
        <td>
          <g:if test="${!process.status.isFinal && (process.status.statusID != com.jcatalog.grailsflow.status.ProcessStatusEnum.KILLING.value())}">
            <g:link onclick="return askConfirmation('${common['grailsflow.question.confirm']}');" controller="${params['controller']}" action="killProcess" id="${process.id}" title="${processDetails['grailsflow.command.kill']}"
               params="['type': params.type, 'statusID': params.statusID,
                'startedFrom': startedFrom, 'finishedFrom': finishedFrom]">
                 ${processDetails['grailsflow.command.kill']}
            </g:link>
            &nbsp;&nbsp;
          </g:if>
          <g:elseif test="${process.status.statusID == com.jcatalog.grailsflow.status.ProcessStatusEnum.KILLING.value()}">
            <g:link controller="${params['controller']}" action="search" params="${params}">
              ${common['grailsflow.command.refresh']}
            </g:link>
            &nbsp;&nbsp;
          </g:elseif>
          <g:link onclick="openGraphic('${process.id}'); return false;"><nobr>${processDetails['grailsflow.command.showGraphic']}</nobr></g:link>
          &nbsp;&nbsp;
          <g:link controller="${params['controller']}" action="exportProcess" params="${['processID': process.id, 'format': 'csv', 'extension': 'csv']}"><nobr>${processDetails['grailsflow.command.exportCSV']}</nobr></g:link>
          &nbsp;&nbsp;
          <g:link controller="${params['controller']}" action="exportProcess" params="${['processID': process.id, 'format': 'excel', 'extension': 'xls']}"><nobr>${processDetails['grailsflow.command.exportExcel']}</nobr></g:link>
        </td>
    </tr>
  </g:each>
</tbody>
</table>