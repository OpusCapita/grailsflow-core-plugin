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

<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="grailsflow"/>
  <g:render plugin="grailsflowCore" template="/commons/global"/>
  <gf:messageBundle bundle="grailsflow.common" var="common"/>
  <gf:messageBundle bundle="grailsflow.schedulerDetails" var="msgs"/>
  <title>${msgs['grailsflow.title.editScheduledJob']}</title>
</head>

<body>
  <h1>${msgs['grailsflow.label.editScheduledJob']}</h1>

  <g:render plugin="grailsflowCore" template="/commons/messageInfo"/>

  <g:form class="form-horizontal" controller="${params['controller']}">
      <input type="hidden" name="group" value="${jobDetails?.trigger?.group}"/>
      <input type="hidden" name="name" value="${jobDetails?.trigger?.name}"/>

      <h3>${msgs['grailsflow.label.jobParams']}</h3>

      <div class="row">
        <div class="col-md-6">
          <g:render plugin="grailsflowCore" template="jobParametersForm"
                    model="[bean: jobDetails, repeatingInfo: repeatingInfo]"/>
        </div>
      </div>

      <div class="form-submit text-right">
        <g:actionSubmit action="updateJob" onclick="if (!checkRepeatInterval()) return false;"
                        value="${common['grailsflow.command.update']}" class="btn btn-primary"/>
        &nbsp;
      </div>
  </g:form>
</body>
</html>
