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

<gf:messageBundle bundle="grailsflow.common" var="common"/>
<gf:messageBundle bundle="grailsflow.processList" var="processList"/>

<br/>
<h2 class="headline">${processList['grailsflow.label.processList']}</h2>
<table class="grid" width="100%">
<thead>
  <tr>
    <gf:displayHeaders bundle="${processList}" displayParameters="${displayParameters}"/>
    <th>${common['grailsflow.label.operations']}</th>
  </tr>
</thead>
<tbody>
  <g:each in="${processDetailsList}" var="process">
    <tr>
      <gf:displayCells resultItem="${process}" displayParameters="${displayParameters}"/>
      <td>
        <g:link title="${processList['grailsflow.command.details']}" controller="processDetails" id="${process.id}" >
           ${processList['grailsflow.command.details']}
        </g:link>
      </td>
    </tr>
  </g:each>
</tbody>
</table>