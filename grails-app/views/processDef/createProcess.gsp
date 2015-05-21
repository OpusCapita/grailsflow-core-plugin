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
         <title>${msgs['grailsflow.title.editProcess']}</title>
    </head>
    <body>
      <h1>${msgs['grailsflow.label.editProcess']}</h1>

      <g:render plugin="grailsflow" template="/commons/messageInfo"/>

      <g:form class="form-horizontal" controller="${params['controller']}" method="POST">
        <div class="row">
          <div class="col-md-6">
            <div class="form-group">
              <label class="col-md-4 control-label" for="processID">${msgs['grailsflow.label.processID']}</label>
              <div class="col-md-8">
                <input id="processID" name="processID" size="50" value="${params.processID?.encodeAsHTML()}" maxlength="255"/>
              </div>
            </div>
            <div class="form-group">
              <label class="col-md-4 control-label" for="description">${msgs['grailsflow.label.description']}</label>
              <div class="col-md-8">
                <textarea id="description" name="description" cols="47" rows="2">${params.description?.encodeAsHTML()}</textarea>
              </div>
            </div>
            <div class="form-group">
              <div class="col-md-12">
                <g:actionSubmit action="processDefinition" value="${common['grailsflow.command.create']}" class="btn btn-primary"/>
              </div>

            </div>
          </div>
        </div>
      </g:form>

    </body>
</html>
