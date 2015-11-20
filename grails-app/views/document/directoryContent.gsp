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
   <meta name="layout" content="grailsflow"/>
   <g:render plugin="grailsflow" template="/commons/global"/>
   <title><g:message code="plugin.grailsflow.title.directoryContent"/></title>
  </head>

  <body>
    <h1><g:message code="plugin.grailsflow.title.directoryContent"/></h1>

    <div class="row">
      <div class="col-md-6">
        <table class="table">
          <thead>
            <tr>
              <th width="80%"><g:message code="plugin.grailsflow.label.name"/></th>
              <th width="20%"><g:message code="plugin.grailsflow.label.size"/></th>
            </tr>
          </thead>
          <tbody>
            <g:if test="${!isRoot}">
              <tr>
                <td>

                  <g:link action="showDirectoryContent" params="[file: currentFile?.parentFile?.absolutePath, moveBack:true]">
                    ...
                  </g:link>

                </td>
              </tr>
            </g:if>
            <g:each in="${currentFile?.listFiles()}" var="file">
              <tr>
                <td>
                  <g:if test="${file.isFile()}">
                    <gf:workareaFileLink workareaPath="${rootUrl + file.name}" label="${file.name}"/>
                  </g:if>
                  <g:else>
                    <g:link action="showDirectoryContent" params="[file: file.absolutePath, fileName: file.name]">
                      ${file.name}
                    </g:link>
                  </g:else>
                </td>
                <td>
                  <g:if test="${file.isFile()}">
                    ${file.length()} <g:message code="plugin.grailsflow.label.bytes"/>
                  </g:if>
                </td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </div>
  </body>
</html>
