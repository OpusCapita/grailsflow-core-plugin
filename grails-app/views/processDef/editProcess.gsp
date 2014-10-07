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
        <div class="body">
           <b class="header">${msgs['grailsflow.label.processEditor']}</b>
            <g:render plugin="grailsflow" template="/commons/messageInfo"/>
            <br/>
            <g:form controller="${params['controller']}">
            <input type="hidden" name="id" value="${processDetails.id}"/>

            <table class="standard">
             <tr>
               <td>${msgs['grailsflow.label.process.type']}</td>
               <td colspan="2"><input name="processID" value="${processDetails.processID}" size="50"/></td>

             </tr>
             <tr>
               <td>${msgs['grailsflow.label.process.validFrom']}</td>
               <td colspan="2">
                 <gf:jQueryCalendar property="validFrom" value="${processDetails.validFrom}" />
               </td>
             </tr>
             <tr>
               <td>${msgs['grailsflow.label.process.validTo']}</td>
               <td colspan="2">
                 <gf:jQueryCalendar property="validTo" value="${processDetails.validTo}" />
               </td>
             </tr>
             <tr>
               <td>${msgs['grailsflow.label.process.description']}</td>
               <td><textarea name="description" cols="47" rows="2">${processDetails.description[request.locale.language]?.encodeAsHTML()}</textarea></td>
               <td>
                 <g:link controller="${params['controller']}" action="editProcessTranslations" id="${processDetails.id}">
                     ${details['grailsflow.command.manageProcessTranslations']}
                 </g:link>
               </td>
             </tr>
           </table>
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
               <g:actionSubmit action="saveProcess" value="${common['grailsflow.command.apply']}" class="button"/>
               &nbsp;&nbsp;
               <g:actionSubmit action="editTypes" value="${common['grailsflow.command.back']}" class="button"/>
               &nbsp;&nbsp;
               <input type="button" value="${details['grailsflow.command.showGraphicEditor']}" class="button" onclick="return openGraphic('${processDetails.id}')"/>
               &nbsp;&nbsp;
               <g:actionSubmit action="exportAsHTML" value="${details['grailsflow.command.exportAsHTML']}" class="button"/>
             </span>
           </div>
           <br/><br/>
           <div class="buttons">
             <span class="button">
               <g:actionSubmit action="generateProcess" value="${msgs['grailsflow.command.generateProcess']}" class="button"/>
               &nbsp;&nbsp;
               <g:set var="sourceCodeWarning" value="${details['grailsflow.message.sourceCodeWarning']}"/>
               <g:actionSubmit action="showProcessScript" onclick="alert('${sourceCodeWarning}')" value="${details['grailsflow.command.showCode']}" class="button"/>
               &nbsp;&nbsp;
               <g:actionSubmit action="reloadProcessDef" value="${details['grailsflow.command.reload']}" class="button"/>
             </span>
           </div>

           </g:form>
        </div>
    </body>
</html>
