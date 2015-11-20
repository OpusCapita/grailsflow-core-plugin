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
  Template for displaying ProcessVariables on the ProcessDetails UI

  Template parameters:

  required: 
    * nodes              List of nodes objects

 -->

<h3><g:message code="plugin.grailsflow.label.processNodes"/></h3>
<div class="table-responsive">
 <table class="table">
   <thead>
     <tr>
        <th><g:message code="plugin.grailsflow.label.nodeID"/></th>
        <th><g:message code="plugin.grailsflow.label.description"/></th>
        <th><g:message code="plugin.grailsflow.label.status"/></th>
        <th><g:message code="plugin.grailsflow.label.caller"/></th>
        <th><g:message code="plugin.grailsflow.label.startedOn"/></th>
        <th><g:message code="plugin.grailsflow.label.startedExecutionOn"/></th>
        <th><g:message code="plugin.grailsflow.label.finishedOn"/></th>
        <th><g:message code="plugin.grailsflow.label.dueOn"/></th>
      </tr>
   </thead>
   <tbody>
     <g:each in="${nodes}">
          <tr>
             <td><gf:translatedValue translations="${it.label}" default="${it.nodeID}"/></td>
             <td><gf:translatedValue translations="${it.description}" default=""/></td>
             <td>${it.status?.statusID ? g.message(code: 'plugin.grailsflow.label.status.'+it.status?.statusID) : '-'}</td>
             <td>${it.caller}</td>
             <td><gf:displayDateTime value="${it.startedOn}"/></td>
             <td><gf:displayDateTime value="${it.startedExecutionOn}"/></td>
             <td><gf:displayDateTime value="${it.finishedOn}"/></td>
             <td><gf:displayDateTime value="${it.dueOn}"/></td>
          </tr>
     </g:each>
   </tbody>
 </table>
</div>