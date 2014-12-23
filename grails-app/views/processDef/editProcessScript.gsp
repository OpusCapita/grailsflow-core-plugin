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

         <gf:messageBundle bundle="grailsflow.common" var="common"/>
         <gf:messageBundle bundle="grailsflow.processDetails" var="msgs"/>
         <title>${msgs['grailsflow.title.processScript']}</title>

    </head>
    <body>
      <div class="row">
        <div class="col-md-12 col-xs-12 col-lg-12">
          <b class="header">${msgs['grailsflow.label.processScript']}</b>
        </div>
      </div>

      <div class="row">
        <div class="col-md-12 col-xs-12 col-lg-12">
           <g:render plugin="grailsflow" template="/commons/messageInfo"/>
        </div>
      </div>

      <div class="row">
        <div class="col-md-12 col-xs-12 col-lg-12">
           <g:form controller="${params['controller']}" method="POST" >
             <input type="hidden" name="id" value="${processType}"/>
             <br/>
                <table>
                    <tbody>
                      <tr class='prop'>
                        <td valign='top'> ${processType}&nbsp;${msgs['grailsflow.label.processCode']}: </td>
                      </tr>
                      <tr>
                        <td valign='top' colspan="2">
                          <textarea id="code" name="code">${processCode?.encodeAsHTML()}</textarea>
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
               <div class="buttons">
                 <span class="menuButton" style="padding-left:3px; margin-bottom:8px;">
                   <g:actionSubmit action="editProcessScript" value="${common['grailsflow.command.check']}" class="button"/>
                   &nbsp;&nbsp;
                   <g:actionSubmit action="saveProcessScript" value="${common['grailsflow.command.save']}" class="button"/>
                   &nbsp;&nbsp;
                   <g:actionSubmit action="editTypes" value="${common['grailsflow.command.back']}" class="button"/>
                 </span>
               </div>
            </g:form>
        </div>
      </div>
    </body>
</html>
