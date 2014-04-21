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
         <gf:messageBundle bundle="grailsflow.processTypes" var="msgs"/>
         <title>${msgs['grailsflow.title.editProcess']}</title>
    </head>
    <body>
        <div class="body">
           <b class="header">${msgs['grailsflow.label.editProcess']}</b>

           <g:render plugin="grailsflowCore" template="/commons/messageInfo"/>
            
           <br/>
           <g:form controller="${params['controller']}" method="POST">
              <table cellspacing=3 class="blockLayout">
                <tr>
                  <td>${msgs['grailsflow.label.processID']}</td>
                  <td><input name="processID" size="50" value="${params.processID?.encodeAsHTML()}" maxlength="255"/></td>
                </tr>
                <tr>
                  <td>${msgs['grailsflow.label.description']}</td>
                  <td><textarea name="description" cols="47" rows="2">${params.description?.encodeAsHTML()}</textarea></td>
                </tr>
              </table>

              <div class="buttons">
                <span class="button">
                  <g:actionSubmit action="processDefinition" value="${common['grailsflow.command.create']}" class="button"/>
                </span>
              </div>
           </g:form>
        </div>
    </body>
</html>
