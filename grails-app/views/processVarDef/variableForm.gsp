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

      <g:render plugin="grailsflowCore" template="/commons/global"/>
      <gf:messageBundle bundle="grailsflow.common" var="common"/>
      <gf:messageBundle bundle="grailsflow.processVariableEditor" var="msgs"/>
      <gf:messageBundle bundle="grailsflow.list" var="listType"/>
      <title>${msgs['grailsflow.title.processVars']}</title>

      <r:script>
        function updateVarView(){
            document.getElementById("objectType").style.display="none"
            ${g.remoteFunction(controller: params['controller'], action: "changeVarInput", id: process.id,
                update: 'variableView', params:"'varID='+document.getElementById('varID').value+'&varType='+document.getElementById('varType').value" )}
        }
      </r:script>
      <r:require modules="grailsflowCalendar"/>
    </head>
    <body>
        <div class="body">
           <b class="header">${msgs['grailsflow.label.processVars']}</b>

           <br/>
           <g:render plugin="grailsflowCore" template="/commons/messageInfo"/>
           <br/><br/>

           <g:form controller="${params['controller']}" method="POST">
              <input type="hidden" name="id" value="${process?.id}"/>
              <input type="hidden" name="varID" id="varID" value="${variable?.id}"/>

              <table id="variableTable" cellspacing=3 class="blockLayout">
                <tr>
                  <td>${msgs['grailsflow.label.name']}</td>
                  <td>
                    <input name="varName" value="${(variable?.name ? variable?.name : params.varName)?.encodeAsHTML()}"/>
                    <g:if test="${variable.id}">
                      &nbsp;&nbsp;
 	                    <g:link controller="${params['controller']}" action="editVariableTranslations" id="${variable.id}"
			                       title="${msgs['grailsflow.command.manageTranslations']}">
			                  ${msgs['grailsflow.command.manageTranslations']}
		                  </g:link>
                    </g:if>
                  </td>
                </tr>
                <tr>
                  <td>${msgs['grailsflow.label.processIdentifier']}</td>
                  <td><g:checkBox name="isProcessIdentifier" value="${variable?.isProcessIdentifier ? variable?.isProcessIdentifier : params?.isProcessIdentifier}"/></td>
                </tr>
                <tr>
                  <td>${msgs['grailsflow.label.required']}</td>
                  <td><g:checkBox name="required" value="${variable?.required ? variable?.required : params.required}"/></td>
                </tr>
                <tr>
                  <td>&nbsp;</td>
                  <td><font class="hint">${msgs['grailsflow.message.varHint']}<br/>
                                         ${msgs['grailsflow.message.spaceHint']} </font></td>
                </tr>
                <tr>
                  <td valign="top">${msgs['grailsflow.label.type']}</td>
                  <td>
                       <g:select value="${(variable?.type && com.jcatalog.grailsflow.model.definition.ProcessVariableDef.types.contains(variable.type)) ? variable.type : (params.varType ? params.varType : 'Object')}"
                       from="${com.jcatalog.grailsflow.model.definition.ProcessVariableDef.types}" name='varType'
                       id="varType" onchange="updateVarView();" ></g:select>
                       <br/><br/>
                       <div id="objectType" style="display: none;">
                         <input name="varObjectType" value="${variable?.type ? variable?.type : params.varObjectType}" size="60"/>
                       </div>
                  </td>
                </tr>
                <tr>
                  <td valign="top">${msgs['grailsflow.label.value']}</td>
                  <td>
                    <div id='variableView'>
                     <g:render plugin="grailsflowCore" template="variableInput" model="[variable: variable, params: params]"/>
                    </div>
                  </td>
                </tr>
                <tr>
                   <td>&nbsp;</td>
                   <td><font class="hint">${msgs['grailsflow.message.valueHint']}<br/>
                                          ${msgs['grailsflow.message.notSuitableValue']}</font></td>
                </tr>
                <tr>
                  <td valign="top">${msgs['grailsflow.label.view']}</td>
                  <td>
                   <gf:customizingTemplate template="variableViewEditor" model="[variable: variable, params: params]"/>
                  </td>
                </tr>
                <tr>
                  <td colspan="2">
                    <g:actionSubmit action="saveVarDef" value="${common['grailsflow.command.apply']}" class="button"/>
                    &nbsp;&nbsp;
                    <g:actionSubmit action="showProcessEditor" value="${common['grailsflow.command.back']}" class="button"/>
                  </td>
                </tr>
              </table>

           </g:form>
        </div>
    </body>
</html>
