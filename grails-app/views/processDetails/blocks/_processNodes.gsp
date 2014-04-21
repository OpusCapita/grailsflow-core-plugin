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
  Template for displaying ProcessVariables on the ProcessDetails UI

  Template parameters:

  required: 
    * nodes              List of nodes objects

--%>

<gf:messageBundle bundle="grailsflow.common" var="common"/>
<gf:messageBundle bundle="grailsflow.processDetails" var="msgs"/>

<gf:section title="${msgs['grailsflow.label.processNodes']}" selected="true">
 <table class="standard" width="100%">
   <thead>
     <tr>
        <th>${msgs['grailsflow.label.nodeID']}</th>
        <th>${msgs['grailsflow.label.description']}</th>
        <th>${msgs['grailsflow.label.status']}</th>
        <th>${msgs['grailsflow.label.caller']}</th>
        <th>${msgs['grailsflow.label.startedOn']}</th>
        <th>${msgs['grailsflow.label.finishedOn']}</th>
        <th>${msgs['grailsflow.label.dueOn']}</th>
      </tr>
   </thead>
   <tbody>
     <g:each in="${nodes}">
          <tr>
             <td><gf:translatedValue translations="${it.label}" default="${it.nodeID}"/></td>
             <td><gf:translatedValue translations="${it.description}" default=""/></td>
             <td>${it.status?.statusID ? common['grailsflow.label.status.'+it.status?.statusID] : '-'}</td>
             <td>${it.caller}</td>
             <td><gf:displayDateTime value="${it.startedOn}"/></td>
             <td><gf:displayDateTime value="${it.finishedOn}"/></td>
             <td><gf:displayDateTime value="${it.dueOn}"/></td>
          </tr>
     </g:each>
   </tbody>
 </table>
</gf:section>
<br/><br/>
