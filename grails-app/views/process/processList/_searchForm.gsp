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

<r:require modules="grailsflowCalendar"/>

<div class="form-horizontal">
  <div class="row">
    <div class="col-md-6 col-lg-6 col-xs-6">
      <div class="form-group">
        <label class="col-sm-4 col-xs-4 col-md-4 col-lg-4  control-label" for="processID">${processDetails['grailsflow.label.processID']}</label>
        <div class="col-sm-8 col-md-8 col-lg-8 col-xs-8">
          <input class="form-control" id="processID" name="processID" value="${params.processID?.encodeAsHTML()}"/>
        </div>
      </div>
      <div class="form-group">
        <label class="col-sm-4 col-xs-4 col-md-4 col-lg-4  control-label" for="type">${processDetails['grailsflow.label.processType']}</label>
        <div class="col-sm-8 col-md-8 col-lg-8 col-xs-8">
          <g:select from="${processClasses}" name='type' class="form-control"
                    optionKey="${{ it.processType }}"
                    optionValue="${{ gf.translatedValue(translations: it.label, default: it.processType) }}"
                    value="${params.type}" noSelection="['': '']"></g:select>
        </div>
      </div>
      <div class="form-group">
        <label class="col-sm-4 col-xs-4 col-md-4 col-lg-4  control-label" for="status">${processDetails['grailsflow.label.statuses']}</label>
        <div class="col-sm-8 col-md-8 col-lg-8 col-xs-8">
          <g:select value="${params.statusID}" id="status" multiple="true" class="form-control"
                    optionValue="${{ common['grailsflow.label.status.' + it] }}" size="5"
                    from="${com.jcatalog.grailsflow.status.ProcessStatusEnum.values()*.value()}" name='statusID'></g:select>
        </div>
      </div>

    </div>

    <div class="col-md-6 col-lg-6 col-xs-6">
      <div class="form-group">
        <label class="col-sm-4 col-xs-4 col-md-4 col-lg-4 control-label" for="createdBy">${processDetails['grailsflow.label.startedBy']}</label>
        <div class="col-sm-8 col-md-8 col-lg-8 col-xs-8">
          <input class="form-control" id="createdBy" name="createdBy" value="${params.createdBy?.encodeAsHTML()}"/>
        </div>
      </div>
      <div class="form-group">
        <label class="col-sm-4 col-xs-4 col-md-4 col-lg-4 control-label">${processDetails['grailsflow.label.startedFrom']}</label>
        <div class="col-sm-8 col-md-8 col-lg-8 col-xs-8">
          <p style="white-space: nowrap;"><gf:jQueryCalendar property="startedFrom" value="${params.startedFrom?.encodeAsHTML()}" />&nbsp;&nbsp;
          ${processDetails['grailsflow.label.to']}</p>
          <gf:jQueryCalendar property="startedTo" value="${params.startedTo?.encodeAsHTML()}" />
        </div>
      </div>
      <div class="form-group">
        <label class="col-sm-4  col-xs-4 col-md-4 col-lg-4 control-label" for="modifiedBy">${processDetails['grailsflow.label.modifiedBy']}</label>
        <div class="col-sm-8 col-md-8 col-lg-8 col-xs-8">
          <input class="form-control" id="modifiedBy" name="modifiedBy" value="${params.modifiedBy?.encodeAsHTML()}"/>
        </div>
      </div>
      <div class="form-group">
        <label class="col-sm-4 col-xs-4 col-md-4 col-lg-4 control-label">${processDetails['grailsflow.label.modifiedFrom']}</label>
        <div class="col-sm-8 col-md-8 col-lg-8 col-xs-8">
          <p style="white-space: nowrap;"><gf:jQueryCalendar property="modifiedFrom" value="${params.modifiedFrom?.encodeAsHTML()}" />&nbsp;&nbsp;
          ${processDetails['grailsflow.label.to']}</p>
          <gf:jQueryCalendar property="modifiedTo" value="${params.modifiedTo?.encodeAsHTML()}" />
        </div>
      </div>
      <div class="form-group">
        <label class="col-sm-4  col-xs-4 col-md-4 col-lg-4 control-label">${processDetails['grailsflow.label.finishedFrom']}</label>
        <div class="col-sm-8 col-md-8 col-lg-8 col-xs-8">
          <p style="white-space: nowrap;"><gf:jQueryCalendar property="finishedFrom" value="${params.finishedFrom?.encodeAsHTML()}" />&nbsp;&nbsp;
          ${processDetails['grailsflow.label.to']}</p>
          <gf:jQueryCalendar property="finishedTo" value="${params.finishedTo?.encodeAsHTML()}" />
        </div>
      </div>
    </div>
  </div>
</div>
