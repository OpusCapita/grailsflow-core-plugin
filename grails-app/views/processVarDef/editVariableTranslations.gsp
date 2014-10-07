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
         <gf:messageBundle bundle="grailsflow.common" var="common"/>
         <gf:messageBundle bundle="grailsflow.processVariableEditor" var="msgs"/>
         <title>${msgs['grailsflow.title.variableTranslations']}</title>
    </head>
    <body>
      <div class="body">
        <b class="header">${msgs['grailsflow.title.variableTranslations']}</b>
        <g:render plugin="grailsflow" template="/commons/messageInfo"/>
        <br/>
        <h2 class="headline">${msgs['grailsflow.label.name']}: ${variable?.name?.encodeAsHTML()}</h2>
        <br/><br/>
        <g:form controller="${params['controller']}">
          <input type="hidden" name="id" value="${variable?.id?.encodeAsHTML()}"/>

          <h2 class="headline">${msgs['grailsflow.label.label']}</h2>
          <g:render plugin="grailsflow" template="/common/translationsEditor"
	            model="[ 'translations': variable?.label, 'parameterName': 'label']"/>
          <br/><br/>
          <h2 class="headline">${msgs['grailsflow.label.description']}</h2>
          <g:render plugin="grailsflow" template="/common/translationsEditor"
	            model="[ 'translations': variable?.description, 'parameterName': 'description', 'textarea': true]"/>

          <div class="buttons">
            <span class="button">
              <g:actionSubmit action="saveVariableTranslations" value="${common['grailsflow.command.apply']}" class="button"/>
            </span>
          </div>
        </g:form>
      </div>
    </body>
</html>
