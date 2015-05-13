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
      <gf:messageBundle bundle="grailsflow.list" var="listType"/>
      <title>${msgs['grailsflow.title.processVars']}</title>

      <r:script>
        function updateVarView(){
            document.getElementById("objectType").style.display="none"
            var varID = document.getElementById('varID').value
            var varType = document.getElementById('varType').value
            ${g.remoteFunction(controller: params['controller'], action: "changeVarInput", id: process.id,
                update: 'variableView', params:"'varID='+varID+'&varType='+varType" )}
        }
      </r:script>
      <r:require modules="grailsflowCalendar"/>
    </head>
    <body>
      <div class="row">
        <div class="col-md-12 col-xs-12 col-lg-12">
          <h3>${msgs['grailsflow.label.processVars']}</h3>
        </div>
      </div>

      <div class="row">
        <div class="col-md-12 col-xs-12 col-lg-12">
           <g:render plugin="grailsflow" template="/commons/messageInfo"/>
        </div>
      </div>
      <br/>

      <g:form controller="${params['controller']}" method="POST">
        <input type="hidden" name="id" value="${process?.id}"/>
        <input type="hidden" name="varID" id="varID" value="${variable?.id}"/>
        <div class="row">
          <div class="col-md-12 col-xs-12 col-lg-12">
            <div class="form-horizontal">

              <div class="form-group">
                <label class="col-sm-2 col-xs-2 col-md-2 col-lg-2  control-label" for="varName">
                  ${msgs['grailsflow.label.name']}
                </label>
                <div class="col-sm-10 col-md-10 col-lg-10">
                  <input id="varName" name="varName" value="${(variable?.name ? variable?.name : params.varName)?.encodeAsHTML()}"/>
                  <g:if test="${variable.id}">
                    &nbsp;&nbsp;
                    <g:link controller="${params['controller']}" action="editVariableTranslations" id="${variable.id}"
                                title="${msgs['grailsflow.command.manageTranslations']}">
                            ${msgs['grailsflow.command.manageTranslations']}
                    </g:link>
                  </g:if>
                </div>
              </div>

              <div class="form-group">
                <label class="col-sm-2 col-xs-2 col-md-2 col-lg-2  control-label" for="isProcessIdentifier">
                  ${msgs['grailsflow.label.processIdentifier']}
                </label>
                <div class="col-sm-10 col-md-10 col-lg-10">
                  <g:checkBox id="isProcessIdentifier" name="isProcessIdentifier" value="${variable?.isProcessIdentifier ? variable?.isProcessIdentifier : params?.isProcessIdentifier}"/>
                </div>
              </div>

              <div class="form-group">
                <label class="col-sm-2 col-xs-2 col-md-2 col-lg-2  control-label" for="required">
                  ${msgs['grailsflow.label.required']}
                </label>
                <div class="col-sm-10 col-md-10 col-lg-10">
                   <g:checkBox id="required" name="required" value="${variable?.required ? variable?.required : params.required}"/>
                </div>
              </div>

              <div class="form-group">
                <label class="col-sm-2 col-xs-2 col-md-2 col-lg-2  control-label" for="hint">
                  &nbsp;
                </label>
                <div class="col-sm-10 col-md-10 col-lg-10">
                  <p class="hint" id="hint">${msgs['grailsflow.message.varHint']}<br/>
                         ${msgs['grailsflow.message.spaceHint']} </p>
                </div>
              </div>

              <div class="form-group">
                <label class="col-sm-2 col-xs-2 col-md-2 col-lg-2  control-label" for="objectType">
                  &nbsp;  ${msgs['grailsflow.label.type']}
                </label>
                <div class="col-sm-10 col-md-10 col-lg-10">
                  <g:select value="${(variable?.type && com.jcatalog.grailsflow.model.definition.ProcessVariableDef.types.contains(variable.type)) ? variable.type : (params.varType ? params.varType : 'Object')}"
                                  from="${com.jcatalog.grailsflow.model.definition.ProcessVariableDef.types}" name='varType'
                                  id="varType" onchange="updateVarView();" ></g:select>
                  <br/><br/>
                  <div id="objectType" style="display: none;">
                    <input name="varObjectType" value="${variable?.type ? variable?.type : params.varObjectType}" size="60"/>
                  </div>
                </div>
              </div>

              <div class="form-group">
                <label class="col-sm-2 col-xs-2 col-md-2 col-lg-2  control-label" for="variableView">
                  ${msgs['grailsflow.label.value']}
                </label>
                <div class="col-sm-10 col-md-10 col-lg-10">
                  <div id='variableView'>
                    <g:render plugin="grailsflow" template="variableInput" model="[variable: variable, params: params]"/>
                  </div>
                </div>
              </div>

              <div class="form-group">
                <label class="col-sm-2 col-xs-2 col-md-2 col-lg-2  control-label" for="valueHint">
                  &nbsp;
                </label>
                <div class="col-sm-10 col-md-10 col-lg-10">
                  <p class="hint" id="valueHint">${msgs['grailsflow.message.valueHint']}<br/>
                            ${msgs['grailsflow.message.notSuitableValue']}</p>
                </div>
              </div>

              <div class="form-group">
                <label class="col-sm-2 col-xs-2 col-md-2 col-lg-2  control-label" for="viewEditor">
                  ${msgs['grailsflow.label.view']}
                </label>
                <div class="col-sm-10 col-md-10 col-lg-10">
                  <gf:customizingTemplate id="viewEditor" template="variableViewEditor" model="[variable: variable, params: params]"/>
                </div>
              </div>

              <div class="form-group">
                <div class="col-sm-12 col-md-12 col-lg-12">
                  <g:actionSubmit action="saveVarDef" value="${common['grailsflow.command.apply']}" class="btn btn-primary"/>
                  &nbsp;&nbsp;
                  <g:actionSubmit action="showProcessEditor" value="${common['grailsflow.command.back']}" class="btn btn-default"/>
                </div>
              </div>

            </div>

          </div>
        </div>
      </g:form>
    </body>
</html>
