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
    Template for displaying process node information on the Wait node form


	Template parameters:

  required:
		* nodeDetails 		            bean of com.jcatalog.grailsflow.bean.NodeDetails type
		

 -->
    <gf:messageBundle bundle="grailsflow.common" var="common"/>
    <gf:messageBundle bundle="grailsflow.worklist" var="msgs"/>

    <b class="header">${msgs['grailsflow.title.nodeDetails']}</b>
    <br/><br/>

    <table class="standard">
      <tr>
        <td>${msgs['grailsflow.label.processType']}</td>
        <td><gf:translatedValue translations="${nodeDetails.process.label}" default="${nodeDetails.process.type}"/></td>
      </tr>
      <tr>
        <td>${msgs['grailsflow.label.nodeID']}</td>
        <td><gf:translatedValue translations="${nodeDetails.label}" default="${nodeDetails.nodeID}"/></td>
      </tr>
      <g:if test="${nodeDetails.description}">
        <tr>
          <td>${msgs['grailsflow.label.description']}</td>
          <td><gf:translatedValue translations="${nodeDetails.description}" default=""/></td>
        </tr>
      </g:if>
      <g:if test="${isStarted}">
      <tr>
        <td>${msgs['grailsflow.label.caller']}</td>
        <td>${nodeDetails.caller}</td>
      </tr>
      <tr>
        <td>${msgs['grailsflow.label.status']}</td>
        <td>${nodeDetails.status?.statusID ? common['grailsflow.label.status.'+nodeDetails.status?.statusID] : '-'}</td>
      </tr>
      <tr>
        <td>${msgs['grailsflow.label.startedOn']}</td>
        <td><gf:displayDateTime value="${nodeDetails.startedOn}"/></td>
      </tr>
      <tr>
        <td>${msgs['grailsflow.label.dueOn']}</td>
        <td><gf:displayDateTime value="${nodeDetails.dueOn}"/></td>
      </tr>
      </g:if>
    </table>