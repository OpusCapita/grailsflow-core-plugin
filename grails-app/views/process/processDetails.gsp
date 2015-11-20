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

         <title><g:message code="plugin.grailsflow.title.processDetails"/></title>

         <r:script>
           function openGraphic(id) {
               window.open("${g.createLink(controller: "process", action:'showGraphic')}?processID="+id, "GraphicProcess", 'width=700, height=500, resizable=yes, scrollbars=yes, status=no')
           }
         </r:script>
    </head>
    <body>
      <h1><g:message code="plugin.grailsflow.title.processDetails"/></h1>

      <g:render plugin="grailsflow" template="/commons/messageInfo"/>

      <g:form controller="${params['controller']}" method="POST">
        <input type="hidden" name="id" value="${processDetails?.id}"/>
        <div class="row">
          <div class="col-md-6">
            <div class="form-group">
              <div class="row">
                <div class="col-md-5">
                  <g:message code="plugin.grailsflow.label.id"/>
                </div>
                <div class="col-md-7">
                   ${processDetails?.id}
                </div>
              </div>
            </div>
            <div class="form-group">
              <div class="row">
                <div class="col-md-5">
                  <g:message code="plugin.grailsflow.label.type"/>
                </div>
                <div class="col-md-7">
                  <gf:translatedValue translations="${processDetails?.label}" default="${processDetails?.type}"/>
                </div>
              </div>
            </div>
            <div class="form-group">
              <div class="row">
                <div class="col-md-5">
                  <g:message code="plugin.grailsflow.label.status"/>
                </div>
                <div class="col-md-7">
                  ${processDetails?.status?.statusID ? g.message(code: 'plugin.grailsflow.label.status.'+processDetails?.status?.statusID) : '-'}
                </div>
              </div>
            </div>
            <div class="form-group">
              <div class="row">
                <div class="col-md-5">
                  <g:message code="plugin.grailsflow.label.createdBy"/>
                </div>
                <div class="col-md-7">
                  ${processDetails?.createdBy}
                </div>
              </div>
            </div>
            <div class="form-group">
              <div class="row">
                <div class="col-md-5">
                  <g:message code="plugin.grailsflow.label.createdOn"/>
                </div>
                <div class="col-md-7">
                  <gf:displayDateTime value="${processDetails?.createdOn}"/>
                </div>
              </div>
            </div>
          </div>
        <div class="col-md-6"></div>
      </div>

      <div class="row">
        <div class="col-md-12">
           <gf:customizingTemplate template="variablesList" model="[variables: processDetails.variables.values()]"/>
        </div>
      </div>

      <div class="row">
        <div class="col-md-12">
          <gf:customizingTemplate template="nodesList" model="[nodes: processDetails.nodes]"/>
        </div>
      </div>

      <div class="row">
        <div class="form-submit text-right">
          <g:actionSubmit action="list" value="${g.message(code: 'plugin.grailsflow.command.back')}" class="btn btn-link" />
          <r:script>
                   function reloadPage() {
                       window.location = "${g.createLink(controller: params['controller'], action: params['action'], params: params)}";
                   }
          </r:script>
          <input type="button" onclick="reloadPage();"
            value="${g.message(code: 'plugin.grailsflow.command.refresh')}" class="btn btn-default" />
          <input type="button" class="btn btn-primary" value="${g.message(code: 'plugin.grailsflow.command.showGraphicEditor')}" onclick="openGraphic('${processDetails.id}'); return false;"/>
        </div>
      </div>
      </g:form>
    </body>
</html>
