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
   <gf:messageBundle bundle="grailsflow.document" var="document_bundle"/>
   <title>${document_bundle['grailsflow.title.directoryContent']}</title>
  </head>

  <body>
  <div class="body">
    <b class="header">${document_bundle['grailsflow.title.directoryContent']}</b>
    <br/><br/>
    <table class="standard" width="50%">
      <thead>
        <tr>
          <th width="80%">${document_bundle['grailsflow.label.name']}</th>
          <th width="20%">${document_bundle['grailsflow.label.size']}</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td>
            <g:if test="${!isRoot}">
              <g:link action="showDirectoryContent" params="[file: currentFile?.parentFile?.absolutePath, moveBack:true]">
                ...
              </g:link>
            </g:if>
          </td>
        </tr>
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
                ${file.length()} ${document_bundle['grailsflow.label.bytes']}
              </g:if>
            </td>
          </tr>
        </g:each>
      </tbody>
    </table>
  </div>

  </body>
</html>
