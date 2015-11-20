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
  <title><g:message code="plugin.grailsflow.title.emailReciever"/></title>
</head>

<body>
<h1><g:message code="plugin.grailsflow.title.emailReciever"/></h1>
<g:render plugin="grailsflowCore" template="/commons/messageInfo"/>

<g:form class="form-horizontal" controller="${params['controller']}" method="GET">
  <div class="row">
    <div class="col-md-6">
      <div class="form-group">
        <label class="col-sm-5 control-label" for="enabled"><g:message code="plugin.grailsflow.label.enabled"/></label>

        <div class="col-sm-7">
          <div class="checkbox">
            <g:checkBox name="enabled" value="${enabled}"/>
          </div>
        </div>
      </div>

      <div class="form-group">
        <label class="col-sm-5 control-label" for="mailAddress"><g:message code="plugin.grailsflow.label.mailAddress"/></label>

        <div class="col-sm-7">
          <input class="form-control" id="mailAddress" name='mailAddress' value="${mailAddress}"/>
        </div>
      </div>

      <div class="form-group">
        <label class="col-sm-5 control-label" for="mailHost"><g:message code="plugin.grailsflow.label.mailHost"/></label>

        <div class="col-sm-7">
          <input class="form-control" id="mailHost" name='mailHost' value="${mailHost}"/>
        </div>
      </div>

      <div class="form-group">
        <label class="col-sm-5 control-label" for="mailAccount"><g:message code="plugin.grailsflow.label.mailAccount"/></label>

        <div class="col-sm-7">
          <input class="form-control" id="mailAccount" name='mailAccount' value="${mailAccount}"/>
        </div>
      </div>

      <div class="form-group">
        <label class="col-sm-5 control-label" for="password"><g:message code="plugin.grailsflow.label.mailPassword"/></label>

        <div class="col-sm-7">
          <input class="form-control" id="password" type="password" name='mailPassword' value="${mailPassword}"/>
        </div>
      </div>
    </div>
  </div>

  <div class="form-submit text-right">
    <g:actionSubmit action="save" value="${g.message(code: 'plugin.grailsflow.command.save')}" class="btn btn-primary"/>
  </div>
</g:form>
</body>
</html>
