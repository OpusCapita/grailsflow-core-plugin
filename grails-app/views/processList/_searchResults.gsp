<%--
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--%>

<%--
  Template for rendering search results for ProcessList

  parameters:
    - processDetailsList


--%>
<br/>
<h1><g:message code="plugin.grailsflow.label.processList"/></h1>
<table class="table">
<thead>
  <tr>
    <gf:displayHeaders bundle="${processList}" displayParameters="${displayParameters}" labelPrefix="grailsflow."/>
    <th><g:message code="plugin.grailsflow.label.operations"/></th>
  </tr>
</thead>
<tbody>
  <g:each in="${processDetailsList}" var="process">
    <tr>
      <gf:displayCells resultItem="${process}" displayParameters="${displayParameters}"/>
      <td>
        <g:link title="${g.message(code: 'plugin.grailsflow.command.details')}" controller="processDetails" id="${process.id}" >
          <g:message code="plugin.grailsflow.command.details"/>
        </g:link>
      </td>
    </tr>
  </g:each>
</tbody>
</table>