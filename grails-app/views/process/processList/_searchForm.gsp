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

<r:require modules="grailsflowDatepicker"/>

<div class="form-horizontal">
  <div class="row">
    <div class="col-md-6">
      <div class="form-group">
        <label class="col-md-4 control-label" for="processID"><g:message code="plugin.grailsflow.label.processID"/></label>
        <div class="col-md-8">
          <input class="form-control" id="processID" name="processID" value="${params.processID?.encodeAsHTML()}"/>
        </div>
      </div>
      <div class="form-group">
        <label class="col-md-4 control-label" for="type"><g:message code="plugin.grailsflow.label.processType"/></label>
        <div class="col-md-8">
          <g:select from="${processClasses}" name='type' class="form-control"
                    optionKey="${{ it.processType }}"
                    optionValue="${{ gf.translatedValue(translations: it.label, default: it.processType) }}"
                    value="${params.type}" noSelection="['': '']"></g:select>
        </div>
      </div>
      <div class="form-group">
        <label class="col-md-4 control-label" for="status"><g:message code="plugin.grailsflow.label.statuses"/></label>
        <div class="col-md-8">
          <g:select value="${params.statusID}" id="status" multiple="true" class="form-control"
                    optionValue="${{ g.message(code: 'plugin.grailsflow.label.status.' + it) }}" size="5"
                    from="${statuses ? statuses*.value() : com.jcatalog.grailsflow.status.ProcessStatusEnum.values()*.value()}" name='statusID'></g:select>
        </div>
      </div>

    </div>

    <div class="col-md-6">
      <div class="form-group">
        <label class="col-md-4 control-label" for="createdBy"><g:message code="plugin.grailsflow.label.startedBy"/></label>
        <div class="col-md-8">
          <input class="form-control" id="createdBy" name="createdBy" value="${params.createdBy?.encodeAsHTML()}"/>
        </div>
      </div>
      <div class="form-group">
        <label class="col-md-4 control-label"><g:message code="plugin.grailsflow.label.startedRange"/></label>
        <div class="col-md-8">
          <gf:dateRangePicker fromId = "startedFrom" toId="startedTo" fromValue="${params.startedFrom?.encodeAsHTML()}"
              toValue="${params.startedTo?.encodeAsHTML()}"
              fromLabel="${g.message(code: 'plugin.grailsflow.label.from')}" toLabel="${g.message(code: 'plugin.grailsflow.label.to')}"/>
        </div>
      </div>
      <div class="form-group">
        <label class="col-md-4 control-label" for="modifiedBy"><g:message code="plugin.grailsflow.label.modifiedBy"/></label>
        <div class="col-md-8">
          <input class="form-control" id="modifiedBy" name="modifiedBy" value="${params.modifiedBy?.encodeAsHTML()}"/>
        </div>
      </div>
      <div class="form-group">
        <label class="col-md-4 control-label"><g:message code="plugin.grailsflow.label.modifiedRange"/></label>
        <div class="col-md-8">
          <gf:dateRangePicker fromId = "modifiedFrom" toId="modifiedTo" fromValue="${params.modifiedFrom?.encodeAsHTML()}"
              toValue="${params.modifiedTo?.encodeAsHTML()}"
              fromLabel="${g.message(code: 'plugin.grailsflow.label.from')}" toLabel="${g.message(code: 'plugin.grailsflow.label.to')}"/>
        </div>
      </div>
      <div class="form-group">
        <label class="col-md-4 control-label"><g:message code="plugin.grailsflow.label.finishedRange"/></label>
        <div class="col-md-8">
          <gf:dateRangePicker fromId = "finishedFrom" toId="finishedTo" fromValue="${params.finishedFrom?.encodeAsHTML()}"
              toValue="${params.finishedTo?.encodeAsHTML()}"
              fromLabel="${g.message(code: 'plugin.grailsflow.label.from')}" toLabel="${g.message(code: 'plugin.grailsflow.label.to')}"/>
        </div>
      </div>
    </div>
  </div>
</div>
