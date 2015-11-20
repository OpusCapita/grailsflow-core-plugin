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

         <r:require modules="grailsflowCodeMirror"/>

         <title><g:message code="plugin.grailsflow.title.processScript"/></title>

    </head>
    <body>
      <h1><g:message code="plugin.grailsflow.label.processScript"/></h1>

      <g:render plugin="grailsflow" template="/commons/messageInfo"/>

      <div class="row">
           <g:form controller="${params['controller']}" method="POST" >
             <input type="hidden" name="id" value="${processType}"/>
             <br/>
                <table>
                    <tbody>
                      <tr class='prop'>
                        <td valign='top'> ${processType}&nbsp;<g:message code="plugin.grailsflow.label.processCode"/>: </td>
                      </tr>
                      <tr>
                        <td valign='top' colspan="2">
                          <textarea id="code" name="code" class="form-control">${processCode?.encodeAsHTML()}</textarea>
                          <r:script type="text/javascript">
                             var editor = CodeMirror.fromTextArea(document.getElementById("code"), {
                                lineNumbers: true,
                                matchBrackets: true,
                                mode: "text/x-groovy",
                                onCursorActivity: function() {
                                    editor.setLineClass(hlLine, null);
                                    hlLine = editor.setLineClass(editor.getCursor().line, "activeline");
                                }
                              });
                              var hlLine = editor.setLineClass(0, "activeline");
                          </r:script>
                        </td>
                      </tr>
                    </tbody>
               </table>
               <div class="form-submit text-right">
                 <g:actionSubmit action="editTypes" value="${g.message(code: 'plugin.grailsflow.command.back')}" class="btn btn-link"/>
                 <g:actionSubmit action="saveProcessScript" value="${g.message(code: 'plugin.grailsflow.command.save')}" class="btn btn-default"/>
                 <g:actionSubmit action="editProcessScript" value="${g.message(code: 'plugin.grailsflow.command.check')}" class="btn btn-default"/>
               </div>
            </g:form>
      </div>
    </body>
</html>
