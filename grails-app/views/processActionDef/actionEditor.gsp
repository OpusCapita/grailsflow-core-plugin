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

<html>
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
        <div class="body">
          <b class="header">${msgs['grailsflow.label.actionEditor']}</b>
           <g:if test="${flash.message}">
             <div class="message">${flash.message}</div>
           </g:if>
           <g:if test="${processNodeDef?.type == ConstantUtils.NODE_TYPE_WAIT}">
             <div class="warnings" style="color: #CCCC00;">*${msgs["grailsflow.message.action.warning"]}</div>
           </g:if>
           <g:form controller="${params['controller']}" method="POST">
             <input type="hidden" name="nodeID" value="${processNodeDef?.id}"/>
             <br/>
             <table cellspacing=3>
                <tr>
                  <td>${msgs['grailsflow.label.processType']}:</td>
                  <td>${processNodeDef?.processDef?.processID}</td>
                </tr>
                <tr>
                  <td>${msgs['grailsflow.label.nodeID']}:</td>
                  <td>${processNodeDef?.nodeID}
                  </td>
                </tr>
              </table>
              <br/><br/>

              <table>
                <tr>
                 <td>${msgs['grailsflow.label.variables']}</td>
                 <td>${msgs['grailsflow.label.actions']}</td>
                </tr>
                <tr>
                 <td style="padding-right:15px;">
                   <g:select name="variable" id="variable" noSelection="${['':'']}" from='${variables}'></g:select>
                   <img alt="${msgs['grailsflow.command.pasteVariable']}"
                        src="${g.resource(plugin: 'grailsflow', dir: 'images/grailsflow/editor',file:'add.gif')}"
                        onClick="pasteVariable()"/>
                 </td>
                 <td style="padding-right:15px;">
                   <gf:select name="action" id="action" noSelection="${['':'']}" from='${actions}'
                      optionValue="label" optionKey="value" optionGroup="group">
                   </gf:select>
                   <img alt="${msgs['grailsflow.command.pasteAction']}"
                        src="${g.resource(plugin: 'grailsflow', dir: 'images/grailsflow/editor',file:'add.gif')}"
                        onClick="openActionParametersEditor()"/>
                 </td>
                </tr>
              </table>

              <g:textArea id="actionsCode" name="actionsCode" value="${actionsCode}" rows="20" cols="80"/>

              <br/>
              <div class="buttons">
                 <g:actionSubmit action="saveActions" value="${common['grailsflow.command.apply']}" class="button"/>&nbsp;
                 <g:actionSubmit action="showProcessNodeEditor" value="${common['grailsflow.command.back']}" class="button"/>
              </div>

           </g:form>
        </div>
    </body>
</html>
