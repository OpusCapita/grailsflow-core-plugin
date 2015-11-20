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

         <title><g:message code="plugin.grailsflow.title.processEditor"/></title>

         <r:require modules="grailsflowDatepicker"/>
         <r:script>
           function openGraphic(id) {
             window.open("${g.createLink(action:'showGraphic')}?processID="+id, "GraphicProcess", 'width=700, height=500, resizable=yes, scrollbars=yes, status=no')
           }
         </r:script>

    </head>
    <body>
      <h1><g:message code="plugin.grailsflow.label.processEditor"/></h1>

      <g:render plugin="grailsflow" template="/commons/messageInfo"/>

      <g:form controller="${params['controller']}">

        <div class="row">
          <div class="col-md-12">
            <input type="hidden" name="id" value="${processDetails.id}"/>
            <div class="form-horizontal">
            <div class="form-group">
                <label class="col-md-2 control-label" for="type">
                  <g:message code="plugin.grailsflow.label.process.type"/>
                </label>
                <div class="col-md-6">
                    <input id="type" name="processID" class="form-control"  value= "${processDetails.processID}" size="50" maxlength="255"/>
                </div>
            </div>
            <div class="form-group">
                <label class="col-md-2 control-label" for="type">
                  <g:message code="plugin.grailsflow.label.process.validFrom"/> / <g:message code="plugin.grailsflow.label.process.validTo"/>
                </label>
                <div class="col-md-6">
                  <gf:dateRangePicker fromId = "validFrom" toId="validTo"
                  fromValue="${processDetails.validFrom}" toValue="${processDetails.validTo}"
                  fromLabel="${g.message(code: 'plugin.grailsflow.label.from')}" toLabel="${g.message(code: 'plugin.grailsflow.label.to')}"/>
                </div>
            </div>
            <div class="form-group">
                    <label class="col-md-2 control-label" for="description">
                      <g:message code="plugin.grailsflow.label.process.description"/>
                    </label>
                    <div class="col-md-6">
                        <textarea id="description" name="description" cols="47" rows="2" class="form-control" >${processDetails.description[request.locale.language]?.encodeAsHTML()}</textarea>
                    </div>
                    <div class="col-md-4">
                        <g:link controller="${params['controller']}" action="editProcessTranslations" id="${processDetails.id}">
                            <g:message code="plugin.grailsflow.command.manageProcessTranslations"/>
                        </g:link>
                    </div>
            </div>

            </div>
           <br/><br/>
          </div>
        </div>

        <div class="row">
          <div class="col-md-12">
            <!-- Assignees list -->
            <gf:section title="${g.message(code: 'plugin.grailsflow.label.process.assignees')}" selected="false">
              <gf:customizingTemplate template="/common/assigneesEditor"
                 model="[assignees: processDetails.processAssignees, controller: 'processDef']"/>
            </gf:section>
          </div>
        </div>

        <div class="row">
          <div class="col-md-12">
            <!-- Variables list -->
            <gf:customizingTemplate template="variablesList"
              model="[variables: processDetails.variables, showOperations: true]"/>
          </div>
        </div>

        <div class="row">
          <div class="col-md-12">
            <!-- Nodes list -->
            <gf:customizingTemplate template="nodesList"
              model="[nodes: processDetails.nodes, showOperations: true]"/>
          </div>
        </div>

        <div class="row">
          <div class="form-submit text-right">
            <g:actionSubmit action="editTypes" value="${g.message(code: 'plugin.grailsflow.command.back')}" class="btn btn-link"/>
            <input type="button" value="${g.message(code: 'plugin.grailsflow.command.showGraphicEditor')}" class="btn btn-default" onclick="return openGraphic('${processDetails.id}')"/>
            <g:actionSubmit action="exportAsHTML" value="${g.message(code: 'plugin.grailsflow.command.exportAsHTML')}" class="btn btn-default"/>
            <g:actionSubmit action="saveProcess" value="${g.message(code: 'plugin.grailsflow.command.apply')}" class="btn btn-primary"/>
          </div>
          <br/>
          <div class="form-submit text-right">
            <g:actionSubmit action="generateProcess" value="${g.message(code: 'plugin.grailsflow.command.generateProcess')}" class="btn btn-default"/>
            <g:set var="sourceCodeWarning" value="${g.message(code: 'plugin.grailsflow.message.sourceCodeWarning')}"/>
            <g:actionSubmit action="showProcessScript" onclick="alert('${sourceCodeWarning}')" value="${g.message(code: 'plugin.grailsflow.command.showCode')}" class="btn btn-default"/>
            <g:actionSubmit action="reloadProcessDef" value="${g.message(code: 'plugin.grailsflow.command.reload')}" class="btn btn-default"/>
          </div>
        </div>
      </g:form>

    </body>
</html>
