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

         <r:require modules="grailsflowHighlighter"/>
         <r:script>
             SyntaxHighlighter.config.tagName = 'textarea';
             SyntaxHighlighter.all();
         </r:script>

         <title><g:message code="plugin.grailsflow.title.processScript"/></title>
    </head>
    <body>
      <h1><g:message code="plugin.grailsflow.label.processScript"/></h1>

      <g:render plugin="grailsflow" template="/commons/messageInfo"/>

      <div class="row">
        <g:form controller="${params['controller']}" method="POST" >
          <input type="hidden" name="id" value="${processID?.encodeAsHTML()}"/>
          <div class="dialog">
            <br/>
            <table>
              <tbody>
                <tr class='prop'>
                  <td valign='top'> ${processType}&nbsp;<g:message code="plugin.grailsflow.label.processCode"/>: </td>
                </tr>
                <tr>
                  <td valign='top' colspan="2">
                    <textarea cols="95" rows="35" class="brush: groovy">${processCode?.encodeAsHTML()}</textarea>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="form-submit text-right">
            <g:actionSubmit action="editProcess" value="${g.message(code: 'plugin.grailsflow.command.back')}" class="btn btn-link"/>
          </div>
        </g:form>
      </div>
    </body>
</html>
