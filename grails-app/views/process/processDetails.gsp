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
         <gf:messageBundle bundle="grailsflow.processDetails" var="msgs"/>
         <gf:messageBundle bundle="grailsflow.processVariableEditor" var="varMsgs"/>
         <title>${msgs['grailsflow.title.processDetails']}</title>

         <r:script>
           function openGraphic(id) {
               window.open("${g.createLink(controller: "process", action:'showGraphic')}?processID="+id, "GraphicProcess", 'width=700, height=500, resizable=yes, scrollbars=yes, status=no')
           }
         </r:script>
    </head>
    <body>
      <h1>${msgs['grailsflow.title.processDetails']}</h1>

      <g:render plugin="grailsflow" template="/commons/messageInfo"/>

      <g:form controller="${params['controller']}" method="POST">
        <input type="hidden" name="id" value="${processDetails?.id}"/>
        <div class="row">
          <div class="col-sm-6 col-md-6 col-lg-6 col-xs-6">
            <div class="form-group">
              <div class="row">
                <div class="col-sm-5 col-md-5 col-lg-5">
                  ${msgs['grailsflow.label.id']}
                </div>
                <div class="col-sm-7 col-md-7 col-lg-7">
                   ${processDetails?.id}
                </div>
              </div>
            </div>
            <div class="form-group">
              <div class="row">
                <div class="col-sm-5 col-md-5 col-lg-5">
                  ${msgs['grailsflow.label.type']}
                </div>
                <div class="col-sm-7 col-md-7 col-lg-7">
                  <gf:translatedValue translations="${processDetails?.label}" default="${processDetails?.type}"/>
                </div>
              </div>
            </div>
            <div class="form-group">
              <div class="row">
                <div class="col-sm-5 col-md-5 col-lg-5">
                  ${msgs['grailsflow.label.status']}
                </div>
                <div class="col-sm-7 col-md-7 col-lg-7">
                  ${processDetails?.status?.statusID ? common['grailsflow.label.status.'+processDetails?.status?.statusID] : '-'}
                </div>
              </div>
            </div>
            <div class="form-group">
              <div class="row">
                <div class="col-sm-5 col-md-5 col-lg-5">
                  ${msgs['grailsflow.label.createdBy']}
                </div>
                <div class="col-sm-7 col-md-7 col-lg-7">
                  ${processDetails?.createdBy}
                </div>
              </div>
            </div>
            <div class="form-group">
              <div class="row">
                <div class="col-sm-5 col-md-5 col-lg-5">
                  ${msgs['grailsflow.label.createdOn']}
                </div>
                <div class="col-sm-7 col-md-7 col-lg-7">
                  <gf:displayDateTime value="${processDetails?.createdOn}"/>
                </div>
              </div>
            </div>
          </div>
        <div class="col-sm-6 col-md-6 col-lg-6 col-xs-6"></div>
      </div>

      <div class="row">
        <div class="col-md-12 col-xs-12 col-lg-12">
           <gf:customizingTemplate template="variablesList" model="[variables: processDetails.variables.values()]"/>
        </div>
      </div>

      <div class="row">
        <div class="col-md-12 col-xs-12 col-lg-12">
          <gf:customizingTemplate template="nodesList" model="[nodes: processDetails.nodes]"/>
        </div>
      </div>

      <div class="row">
        <div class="col-md-12 col-xs-12 col-lg-12">
           <div class="buttons">
             <span class="button">
               <g:actionSubmit action="list" value="${common['grailsflow.command.back']}" class="btn btn-default" />
               &nbsp;&nbsp;
               <r:script>
                   function reloadPage() {
                       window.location = "${g.createLink(controller: params['controller'], action: params['action'], params: params)}";
                   }
               </r:script>
               <input type="button" onclick="reloadPage();"
                        value="${common['grailsflow.command.refresh']}" class="btn btn-default" />
               &nbsp;&nbsp;
               <input type="button" class="btn btn-primary" value="${msgs['grailsflow.command.showGraphicEditor']}" onclick="openGraphic('${processDetails.id}'); return false;"/>
             </span>
           </div>
          </g:form>
        </div>
      </div>

    </body>
</html>
