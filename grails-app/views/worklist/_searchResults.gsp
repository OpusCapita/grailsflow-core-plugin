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
  Template for rendering search results for Worklist

  parameters:
    - nodeDetailsList


--%>

<gf:messageBundle bundle="grailsflow.common" var="common"/>
<gf:messageBundle bundle="grailsflow.worklist" var="worklist"/>

<br/>
<h1>${worklist['grailsflow.label.worklist']}</h1>
<table class="table">
<thead>
  <tr>
    <g:if test="${displayParameters}">
      <gf:displayHeaders bundle="${worklist}" labelPrefix="label." displayParameters="${displayParameters}"/>
    </g:if>
    <th>&nbsp;</th>
  </tr>
</thead>
<tbody>
  <g:each in="${nodeDetailsList}" var="node">
    <tr>
      <g:if test="${displayParameters}">
        <gf:displayCells resultItem="${node}" displayParameters="${displayParameters}"/>
      </g:if>
      <td>
        <g:link title="${worklist['grailsflow.command.details']}" controller="process" action="showNodeDetails" id="${node.id}" >
           ${worklist['grailsflow.command.details']}
        </g:link>
      </td>
    </tr>
  </g:each>
</tbody>
</table>