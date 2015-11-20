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
      <title><g:message code="plugin.grailsflow.title.processVars"/></title>

      <r:script>
        function updateVarView(){
            document.getElementById("objectType").style.display="none"
            var varID = document.getElementById('varID').value
            var varType = document.getElementById('varType').value
            ${g.remoteFunction(controller: params['controller'], action: "changeVarInput", id: process.id,
                update: 'variableView', params:"'varID='+varID+'&varType='+varType" )}
        }
      </r:script>
      <r:require modules="grailsflowDatepicker"/>
    </head>
    <body>
      <h1><g:message code="plugin.grailsflow.label.processVars"/></h1>

      <g:render plugin="grailsflow" template="/commons/messageInfo"/>

      <g:form controller="${params['controller']}" method="POST">
        <input type="hidden" name="id" value="${process?.id}"/>
        <input type="hidden" name="varID" id="varID" value="${variable?.id}"/>
        <div class="row">
          <div class="col-md-8">
            <div class="form-horizontal">

              <div class="form-group">
                <label class="col-md-2  control-label" for="varName">
                  <g:message code="plugin.grailsflow.label.name"/>
                </label>
                <div class="col-md-10">
                  <input id="varName" name="varName" value="${(variable?.name ? variable?.name : params.varName)?.encodeAsHTML()}" class="form-control"/>
                  <g:if test="${variable.id}">
                    &nbsp;&nbsp;
                    <g:link controller="${params['controller']}" action="editVariableTranslations" id="${variable.id}"
                                title="${g.message(code: 'grailsflow.command.manageTranslations')}">
                      <g:message code="plugin.grailsflow.command.manageTranslations"/>
                    </g:link>
                  </g:if>
                </div>
              </div>

              <div class="form-group">
                <label class="col-md-2 control-label" for="isProcessIdentifier">
                  <g:message code="plugin.grailsflow.label.processIdentifier"/>
                </label>
                <div class="col-md-10">
                  <g:checkBox id="isProcessIdentifier" name="isProcessIdentifier" value="${variable?.isProcessIdentifier ? variable?.isProcessIdentifier : params?.isProcessIdentifier}"/>
                </div>
              </div>

              <div class="form-group">
                <label class="col-md-2 control-label" for="required">
                  <g:message code="plugin.grailsflow.label.required"/>
                </label>
                <div class="col-md-10">
                   <g:checkBox id="required" name="required" value="${variable?.required ? variable?.required : params.required}"/>
                </div>
              </div>

              <div class="form-group">
                <label class="col-md-2 control-label" for="hint">
                  &nbsp;
                </label>
                <div class="col-md-10">
                  <p class="hint" id="hint"><g:message code="plugin.grailsflow.message.varHint"/><br/>
                  <g:message code="plugin.grailsflow.message.spaceHint"/> </p>
                </div>
              </div>

              <div class="form-group">
                <label class="col-md-2  control-label" for="objectType">
                  &nbsp;  <g:message code="plugin.grailsflow.label.type"/>
                </label>
                <div class="col-md-10">
                  <g:select value="${(variable?.type && com.jcatalog.grailsflow.model.definition.ProcessVariableDef.types.contains(variable.type)) ? variable.type : (params.varType ? params.varType : 'Object')}"
                                  from="${com.jcatalog.grailsflow.model.definition.ProcessVariableDef.types}" name='varType'  class="form-control"
                                  id="varType" onchange="updateVarView();" ></g:select>
                  <br/><br/>
                  <div id="objectType" style="display: none;">
                    <input name="varObjectType" value="${variable?.type ? variable?.type : params.varObjectType}" size="60"/>
                  </div>
                </div>
              </div>

              <div class="form-group">
                <label class="col-md-2 control-label" for="variableView">
                  <g:message code="plugin.grailsflow.label.value"/>
                </label>
                <div class="col-md-10">
                  <div id='variableView'>
                    <g:render plugin="grailsflow" template="variableInput" model="[variable: variable, params: params]"/>
                  </div>
                </div>
              </div>

              <div class="form-group">
                <label class="col-md-2 control-label" for="valueHint">
                  &nbsp;
                </label>
                <div class="col-md-10">
                  <p class="hint" id="valueHint"><g:message code="plugin.grailsflow.message.valueHint"/><br/>
                  <g:message code="plugin.grailsflow.message.notSuitableValue"/></p>
                </div>
              </div>

              <div class="form-group">
                <label class="col-md-2 control-label" for="viewEditor">
                  <g:message code="plugin.grailsflow.label.view"/>
                </label>
                <div class="col-md-10">
                  <gf:customizingTemplate id="viewEditor" template="variableViewEditor" model="[variable: variable, params: params]"/>
                </div>
              </div>

              <div class="form-group">
                <div class="form-submit text-right">
                  <g:actionSubmit action="showProcessEditor" value="${g.message(code: 'plugin.grailsflow.command.back')}" class="btn btn-link"/>
                  <g:actionSubmit action="saveVarDef" value="${g.message(code: 'plugin.grailsflow.command.apply')}" class="btn btn-primary"/>
                </div>
              </div>

            </div>

          </div>
        </div>
      </g:form>
    </body>
</html>
