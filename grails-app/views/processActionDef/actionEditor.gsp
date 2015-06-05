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
<%@ page import="com.jcatalog.grailsflow.utils.ConstantUtils;" %>

<html xmlns="http://www.w3.org/1999/html">
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="grailsflow" />
         <g:render plugin="grailsflow" template="/commons/global"/>
         <gf:messageBundle bundle="grailsflow.common" var="common"/>
         <gf:messageBundle bundle="grailsflow.processAction" var="msgs"/>
         <title>${msgs['grailsflow.title.actionEditor']}</title>

         <r:script>
           function pasteCode(textToPaste, movePosition) {
             var textArea = document.getElementById("actionsCode");
             var pos = textArea.selectionStart;
             var beginning = textArea.value.slice(0, pos);
             var ending = textArea.value.slice(textArea.selectionEnd);
             textArea.value = beginning + textToPaste + ending;
             textArea.selectionStart = pos + movePosition;
             textArea.selectionEnd = pos + movePosition;
             textArea.focus()
           }

           function pasteVariable() {
             var element = document.getElementById("variable");
             var text = element.value;
             if (text != '') {
               pasteCode(text, text.length)
             }
           }

           function pasteAction(text) {
             if (text != '') {
               pasteCode(text, text.length)
             }
           }

           function openActionParametersEditor() {
             var element = document.getElementById("action");
             var name = element.value;
             window.open("${g.createLink(cantroller: 'processActionDef', action:'actionParametersEditor', params: [nodeID: processNodeDef?.id])}"+"&actionName="+name,
               "ParamsWindow", 'width=560, height=400, resizable=yes, scrollbars=yes, status=no')
           }

         </r:script>

    </head>
    <body>
      <h1>${msgs['grailsflow.label.actionEditor']}</h1>

      <g:render plugin="grailsflowCore" template="/commons/messageInfo"/>

      <g:if test="${processNodeDef?.type == ConstantUtils.NODE_TYPE_WAIT}">
        <div class="alert-warning">*${msgs["grailsflow.message.action.warning"]}</div>
      </g:if>

      <g:form controller="${params['controller']}" method="POST">
        <input type="hidden" name="nodeID" value="${processNodeDef?.id}"/>

        <div class="row">
          <div class="col-md-10">
            <div class="form-group">
              <div class="row">
                <div class="col-md-2">
                  ${msgs['grailsflow.label.processType']}
                </div>
                <div class="col-md-10">
                  ${processNodeDef?.processDef?.processID}
                </div>
              </div>
            </div>
            <div class="form-group">
              <div class="row">
                <div class="col-md-2">
                  ${msgs['grailsflow.label.nodeID']}
                </div>
                <div class="col-md-10">
                  ${processNodeDef?.nodeID}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="row">
          <div class="col-md-8">
            <div class="form-group">
              <div class="row">
                <div class="col-md-3">
                  ${msgs['grailsflow.label.variables']}
                </div>
                <div class="col-md-3" style="white-space: nowrap;">
                  <g:select name="variable" id="variable" noSelection="${['':'']}" from='${variables}' class="form-control" ></g:select>
                  <a href="#" title="${msgs['grailsflow.command.pasteVariable']}" onClick="pasteVariable()">
                    <span class="glyphicon glyphicon-plus text-success"></span>
                  </a
                </div>
                <div class="col-md-2">
                  ${msgs['grailsflow.label.actions']}
                </div>
                <div class="col-md-4" style="white-space: nowrap;">
                  <gf:select name="action" id="action" noSelection="${['':'']}" from='${actions}'
                                     optionValue="label" optionKey="value" optionGroup="group" class="form-control" >
                  </gf:select>
                  <a href="#" title="${msgs['grailsflow.command.pasteAction']}" onClick="openActionParametersEditor()">
                    <span class="glyphicon glyphicon-plus text-success"></span>
                  </a>
                </div>
              </div>
            </div>
            <div class="form-group">
              <div class="row">
                  <g:textArea id="actionsCode" name="actionsCode" value="${actionsCode}" rows="20" cols="80" class="form-control" />
                  <div class="form-submit text-right">
                      <g:actionSubmit action="showProcessNodeEditor" value="${common['grailsflow.command.back']}" class="btn btn-link"/>
                      <g:actionSubmit action="saveActions" value="${common['grailsflow.command.apply']}" class="btn btn-primary"/>
                  </div>
              </div>
            </div>
          </div>
        </div>
      </g:form>

    </body>
</html>
