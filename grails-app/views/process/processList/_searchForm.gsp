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
  Template for rendering search form for ProcessList


-->

<gf:messageBundle bundle="grailsflow.common" var="common"/>
<gf:messageBundle bundle="grailsflow.processDetails" var="processDetails"/>

<r:require modules="grailsflowCalendar" />

<table cellspacing=3 class="blockLayout">
    <tr>
      <td>${processDetails['grailsflow.label.processID']}</td>
      <td><input name="processID" value="${params.processID?.encodeAsHTML()}"/></td>
    </tr>
    <tr>
      <td>${processDetails['grailsflow.label.processType']}</td>
      <td><g:select from="${processClasses}" name='type'
             optionKey="${{it.processType}}" optionValue="${{gf.translatedValue(translations: it.label, default: it.processType)}}"
             value="${params.type}" noSelection="['':'']"></g:select></td>
    </tr>
    <tr>
      <td valign="top">${processDetails['grailsflow.label.statuses']}</td>
      <td>
        <g:select value="${params.statusID}" id="status" multiple="true"
             optionValue="${{common['grailsflow.label.status.'+it]}}"  size="5"
             from="${com.jcatalog.grailsflow.status.ProcessStatusEnum.values()*.value()}" name='statusID'></g:select>

     </td>
    </tr>
    <tr>
      <td>${processDetails['grailsflow.label.startedBy']}</td>
      <td><input name="createdBy" value="${params.createdBy?.encodeAsHTML()}"/></td>
    </tr>
    <tr>
      <td>${processDetails['grailsflow.label.startedFrom']}</td>
      <td>
        <gf:jQueryCalendar property="startedFrom" value="${params.startedFrom?.encodeAsHTML()}" />&nbsp;&nbsp;
        ${processDetails['grailsflow.label.to']}&nbsp;
        <gf:jQueryCalendar property="startedTo" value="${params.startedTo?.encodeAsHTML()}" />
      </td>
    </tr>
    <tr>
      <td>${processDetails['grailsflow.label.modifiedBy']}</td>
      <td><input name="modifiedBy" value="${params.modifiedBy?.encodeAsHTML()}"/></td>
    </tr>
    <tr>
      <td>${processDetails['grailsflow.label.modifiedFrom']}</td>
      <td>
        <gf:jQueryCalendar property="modifiedFrom" value="${params.modifiedFrom?.encodeAsHTML()}" />&nbsp;&nbsp;
        ${processDetails['grailsflow.label.to']}&nbsp;
        <gf:jQueryCalendar property="modifiedTo" value="${params.modifiedTo?.encodeAsHTML()}" />
      </td>
    </tr>
    <tr>
      <td>${processDetails['grailsflow.label.finishedFrom']}</td>
      <td>
        <gf:jQueryCalendar property="finishedFrom" value="${params.finishedFrom?.encodeAsHTML()}" />&nbsp;&nbsp;
        ${processDetails['grailsflow.label.to']}&nbsp;
        <gf:jQueryCalendar property="finishedTo" value="${params.finishedTo?.encodeAsHTML()}" />
      </td>
    </tr>
</table>
