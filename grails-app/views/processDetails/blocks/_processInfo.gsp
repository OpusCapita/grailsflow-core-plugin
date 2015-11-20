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
  Template for displaying process info on the ProcessDetails UI

  Template parameters:

    - processDetails    processDetails
--%>
<table class="table">
   <tr>
     <td>${g.message(code: 'plugin.grailsflow.label.id')}</td>
     <td>${processDetails?.id}</td>
   </tr>
   <tr>
     <td>${g.message(code: 'plugin.grailsflow.label.type')}</td>
     <td><gf:translatedValue translations="${processDetails?.label}" default="${processDetails?.type}"/></td>
   </tr>
   <tr>
     <td>${g.message(code: 'plugin.grailsflow.label.status')}</td>
     <td>${processDetails?.status?.statusID ? g.message(code: 'plugin.grailsflow.label.status.'+processDetails?.status?.statusID) : '-'}</td>
   </tr>
   <tr>
     <td>${g.message(code:'plugin.grailsflow.label.createdBy')}</td>
     <td>${processDetails?.createdBy}</td>
   </tr>
   <tr>
     <td>${g.message(code: 'plugin.grailsflow.label.createdOn')}</td>
     <td><gf:displayDateTime value="${processDetails?.createdOn}"/></td>
   </tr>
</table>
<br/>