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
         <gf:messageBundle bundle="grailsflow.processEditor" var="msgs"/>
         <gf:messageBundle bundle="grailsflow.processDetails" var="details"/>

         <title>${msgs['grailsflow.title.processEditor']}</title>

         <r:require modules="grailsflowCalendar"/>
         <r:script>
           function openGraphic(id) {
             window.open("${g.createLink(action:'showGraphic')}?processID="+id, "GraphicProcess", 'width=700, height=500, resizable=yes, scrollbars=yes, status=no')
           }
         </r:script>

    </head>
    <body>
      <h1>${msgs['grailsflow.label.processEditor']}</h1>

      <g:render plugin="grailsflow" template="/commons/messageInfo"/>

      <g:form controller="${params['controller']}">

        <div class="row">
          <div class="col-md-12 col-xs-12 col-lg-12">
            <input type="hidden" name="id" value="${processDetails.id}"/>
            <div class="form-horizontal">
            <div class="form-group">
                <label class="col-sm-2 col-xs-2 col-md-2 col-lg-2  control-label" for="type">
                    ${msgs['grailsflow.label.process.type']}
                </label>
                <div class="col-sm-10 col-md-10 col-lg-10">
                    <input id="type" name="processID" value= "${processDetails.processID}" size="50"/>
                </div>
            </div>
            <div class="form-group">
                    <label  class="col-sm-2 col-md-2 col-lg-2 control-label" for="validFrom">
                        ${msgs['grailsflow.label.process.validFrom']}
                    </label>
                    <div class="col-sm-10 col-md-10 col-lg-10">
                        <gf:jQueryCalendar property="validFrom" value="${processDetails.validFrom}" />
                    </div>
            </div>
            <div class="form-group">
                    <label class="col-sm-2 col-md-2 col-lg-2  control-label" for="validTo">
                        ${msgs['grailsflow.label.process.validTo']}
                    </label>
                    <div class="col-sm-10 col-md-10 col-lg-10">
                        <gf:jQueryCalendar property="validTo" value="${processDetails.validTo}" />
                    </div>
            </div>
            <div class="form-group">
                    <label class="col-sm-2 col-md-2 col-lg-2  control-label" for="description">
                        ${msgs['grailsflow.label.process.description']}
                    </label>
                    <div class="col-sm-6 col-md-6 col-lg-6">
                        <textarea id="description" name="description" cols="47" rows="2">${processDetails.description[request.locale.language]?.encodeAsHTML()}</textarea>
                    </div>
                    <div class="col-sm-4 col-md-4 col-lg-4">
                        <g:link controller="${params['controller']}" action="editProcessTranslations" id="${processDetails.id}">
                            ${details['grailsflow.command.manageProcessTranslations']}
                        </g:link>
                    </div>
            </div>

            </div>
           <br/><br/>

           <!-- Assignees list -->
           <gf:section title="${msgs['grailsflow.label.process.assignees']}" selected="false">
             <gf:customizingTemplate template="/common/assigneesEditor"
                 model="[assignees: processDetails.processAssignees, controller: 'processDef']"/>
           </gf:section>

           <!-- Variables list -->
           <gf:customizingTemplate template="variablesList"
             model="[variables: processDetails.variables, showOperations: true]"/>

           <!-- Nodes list -->
           <gf:customizingTemplate template="nodesList"
             model="[nodes: processDetails.nodes, showOperations: true]"/>

           <div class="buttons">
             <span class="button">
               <g:actionSubmit action="saveProcess" value="${common['grailsflow.command.apply']}" class="btn btn-default"/>
               &nbsp;&nbsp;
               <g:actionSubmit action="editTypes" value="${common['grailsflow.command.back']}" class="btn btn-default"/>
               &nbsp;&nbsp;
               <input type="button" value="${details['grailsflow.command.showGraphicEditor']}" class="btn btn-default" onclick="return openGraphic('${processDetails.id}')"/>
               &nbsp;&nbsp;
               <g:actionSubmit action="exportAsHTML" value="${details['grailsflow.command.exportAsHTML']}" class="btn btn-default"/>
             </span>
           </div>
           <br/><br/>
           <div class="buttons">
             <span class="button">
               <g:actionSubmit action="generateProcess" value="${msgs['grailsflow.command.generateProcess']}" class="btn btn-default"/>
               &nbsp;&nbsp;
               <g:set var="sourceCodeWarning" value="${details['grailsflow.message.sourceCodeWarning']}"/>
               <g:actionSubmit action="showProcessScript" onclick="alert('${sourceCodeWarning}')" value="${details['grailsflow.command.showCode']}" class="btn btn-default"/>
               &nbsp;&nbsp;
               <g:actionSubmit action="reloadProcessDef" value="${details['grailsflow.command.reload']}" class="btn btn-default"/>
             </span>
           </div>

           </g:form>
        </div>
      </div>
    </body>
</html>
