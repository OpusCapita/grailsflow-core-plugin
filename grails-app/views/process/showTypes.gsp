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
         <gf:messageBundle bundle="grailsflow.processTypes" var="msgs"/>
         <title>${msgs['grailsflow.title.startProcess']}</title>
    </head>
    <body>
      <h1>${msgs['grailsflow.label.startProcess']}</h1>
      <g:render plugin="grailsflow" template="/commons/messageInfo"/>

      <g:form controller="${params['controller']}" method="GET">
        <div class="row">
            <table class="table">
              <thead>
                <tr>
                  <g:sortableColumn property="type" title="${msgs['grailsflow.label.processID']}"/>
                  <th width="70%">${msgs['grailsflow.label.description']}</th>
                  <th>&nbsp;</th>
                </tr>
              </thead>
              <tbody>
                <g:each in="${processClasses}" var="item" status="i">
                  <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <td><g:set var="label" value="${gf.translatedValue(['translations': item.label, 'default': item.processType])}" scope="page" />${label?.encodeAsHTML()}</td>
                    <td><g:set var="description" value="${gf.translatedValue(['translations': item.description, 'default': ''])}" scope="page" />${description?.encodeAsHTML()}</td>
                    <td>
                      <div class="form-submit text-right">
                        <g:link action="startProcess" controller="${params['controller']}" id="${item.processType}" title="${common['grailsflow.command.start']}" class="btn btn-sm btn-default">${common['grailsflow.command.start']}</g:link>
                      </div>
                    </td>
                  </tr>
                </g:each>
              </tbody>
            </table>
        </div>
      </g:form>

    </body>
</html>
