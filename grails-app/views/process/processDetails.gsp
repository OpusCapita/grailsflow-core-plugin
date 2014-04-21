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
         <g:render plugin="grailsflowCore" template="/commons/global"/>
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
        <div class="body">
          <b class="header">${msgs['grailsflow.title.processDetails']}</b>

          <g:render plugin="grailsflowCore" template="/commons/messageInfo"/>

          <g:form controller="${params['controller']}" method="POST">
            <input type="hidden" name="id" value="${processDetails?.id}"/>

            <table class="standard">
             <tr>
               <td>${msgs['grailsflow.label.id']}</td>
               <td>${processDetails?.id}</td>
             </tr>
             <tr>
               <td>${msgs['grailsflow.label.type']}</td>
               <td><gf:translatedValue translations="${processDetails?.label}" default="${processDetails?.type}"/></td>
             </tr>
             <tr>
               <td>${msgs['grailsflow.label.status']}</td>
               <td>${processDetails?.status?.statusID ? common['grailsflow.label.status.'+processDetails?.status?.statusID] : '-'}</td>
             </tr>
             <tr>
               <td>${msgs['grailsflow.label.createdBy']}</td>
               <td>${processDetails?.createdBy}</td>
             </tr>
             <tr>
               <td>${msgs['grailsflow.label.createdOn']}</td>
               <td><gf:displayDateTime value="${processDetails?.createdOn}"/></td>
             </tr>
           </table>
           <br/>
           
           <gf:customizingTemplate template="variablesList" model="[variables: processDetails.variables.values()]"/>
           
           <gf:customizingTemplate template="nodesList" model="[nodes: processDetails.nodes]"/>
           
           <div class="buttons">
             <span class="button">
               <g:actionSubmit action="list" value="${common['grailsflow.command.back']}" class="button"/>
               &nbsp;&nbsp;
               <r:script>
                   function reloadPage() {
                       window.location = "${g.createLink(controller: params['controller'], action: params['action'], params: params)}";
                   }
               </r:script>
               <input type="button" onclick="reloadPage();"
                        value="${common['grailsflow.command.refresh']}" class="button"/>
               &nbsp;&nbsp;
               <input type="button" class="button" value="${msgs['grailsflow.command.showGraphicEditor']}" onclick="openGraphic('${processDetails.id}'); return false;"/>
             </span>
           </div>
          </g:form>
        </div>
    </body>
</html>
