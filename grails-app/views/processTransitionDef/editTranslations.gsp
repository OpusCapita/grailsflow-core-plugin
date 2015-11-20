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
         <meta name="layout" content="grailsflow" />
         <g:render plugin="grailsflow" template="/commons/global"/>
         <title><g:message code="plugin.grailsflow.title.eventTranslations"/></title>
    </head>
    <body>
      <h1><g:message code="plugin.grailsflow.label.eventTranslations"/></h1>

      <g:render plugin="grailsflow" template="/commons/messageInfo"/>

      <div class="row">
        <div class="col-md-6">
          <h4><g:message code="plugin.grailsflow.label.event"/>: ${transition.event?.encodeAsHTML()}</h4>

          <g:form controller="${params['controller']}">
            <input type="hidden" name="id" value="${transition.id?.encodeAsHTML()}"/>
            <h4><g:message code="plugin.grailsflow.label.label"/></h4>
            <g:render plugin="grailsflow" template="/common/translationsEditor"
	            model="[ 'translations': transition.label, 'parameterName': 'label']"/>
            <br/>
            <div class="form-submit text-right">
              <g:actionSubmit action="saveTranslations" value="${g.message(code: 'plugin.grailsflow.command.apply')}" class="btn btn-primary"/>
            </div>
          </g:form>
        </div>
      </div>
    </body>
</html>
