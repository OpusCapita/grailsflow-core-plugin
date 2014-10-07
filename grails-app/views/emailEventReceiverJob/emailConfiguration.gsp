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
         <gf:messageBundle bundle="grailsflow.emailReceiver" var="emailReceiver"/>
         <title>${emailReceiver['grailsflow.title.emailReciever']}</title>

    </head>
    <body>
        <div class="body">
          <b class="header">${emailReceiver['grailsflow.title.emailReciever']}</b>
          <g:render plugin="grailsflow" template="/commons/messageInfo"/>
          <br/><br/>
            
          <g:form controller="${params['controller']}" method="GET">
            <table cellspacing=3 class="blockLayout">
              <tr>
                <td>${emailReceiver['grailsflow.label.enabled']}</td>
                <td><g:checkBox name="enabled" value="${enabled}"/></td>
              </tr>
              <tr>
                <td>${emailReceiver['grailsflow.label.mailAddress']}</td>
                <td><input name='mailAddress' value="${mailAddress}"/></td>
              </tr>
              <tr>
                <td>${emailReceiver['grailsflow.label.mailHost']}</td>
                <td><input name='mailHost' value="${mailHost}"/></td>
              </tr>
              <tr>
                <td>${emailReceiver['grailsflow.label.mailAccount']}</td>
                <td><input name='mailAccount' value="${mailAccount}"/></td>
              </tr>
              <tr>
                <td>${emailReceiver['grailsflow.label.mailPassword']}</td>
                <td><input type="password" name='mailPassword' value="${mailPassword}"/></td>
              </tr>
              <tr>
                <td colspan="2">
                  <g:actionSubmit action="save" value="${common['grailsflow.command.save']}" class="button"/>
                  &nbsp;&nbsp;&nbsp;
                  <g:actionSubmit action="back" value="${common['grailsflow.command.back']}" class="button"/>
                </td>
              </tr>
            </table>
          </g:form>
        </div>
    </body>
</html>
