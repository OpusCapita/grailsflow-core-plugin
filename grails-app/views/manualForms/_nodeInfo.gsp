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

    <h1><g:message code="plugin.grailsflow.title.nodeDetails"/></h1>

    <div class="form-group">
      <div class="row">
        <div class="col-md-2">
          <g:message code="plugin.grailsflow.label.processType"/>
        </div>
        <div class="col-md-10">
          <gf:translatedValue translations="${nodeDetails.process.label}" default="${nodeDetails.process.type}"/>
        </div>
      </div>
    </div>
    <div class="form-group">
      <div class="row">
        <div class="col-md-2">
          <g:message code="plugin.grailsflow.label.nodeID"/>
        </div>
        <div class="col-md-10">
            <gf:translatedValue translations="${nodeDetails.label}" default="${nodeDetails.nodeID}"/>
        </div>
      </div>
    </div>
    <g:if test="${nodeDetails.description}">
      <div class="form-group">
        <div class="row">
          <div class="col-md-2">
            <g:message code="plugin.grailsflow.label.description"/>
          </div>
          <div class="col-md-10">
            <gf:translatedValue translations="${nodeDetails.description}" default=""/>
          </div>
        </div>
      </div>
    </g:if>
    <g:if test="${isStarted}">
      <div class="form-group">
        <div class="row">
          <div class="col-md-2">
            <g:message code="plugin.grailsflow.label.caller"/>
          </div>
          <div class="col-md-10">
            ${nodeDetails.caller}
          </div>
        </div>
      </div>
      <div class="form-group">
        <div class="row">
          <div class="col-md-2">
            <g:message code="plugin.grailsflow.label.status"/>
          </div>
          <div class="col-md-10">
            ${nodeDetails.status?.statusID ? g.message(code: 'plugin.grailsflow.label.status.'+nodeDetails.status?.statusID) : '-'}
          </div>
        </div>
      </div>
      <div class="form-group">
        <div class="row">
          <div class="col-md-2">
            <g:message code="plugin.grailsflow.label.startedOn"/>
          </div>
          <div class="col-md-10">
            <gf:displayDateTime value="${nodeDetails.startedOn}"/>
          </div>
        </div>
      </div>
      <div class="form-group">
        <div class="row">
          <div class="col-md-2">
            <g:message code="plugin.grailsflow.label.dueOn"/>
          </div>
          <div class="col-md-10">
            <gf:displayDateTime value="${nodeDetails.dueOn}"/>
          </div>
        </div>
      </div>
    </g:if>

