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
         <gf:messageBundle bundle="grailsflow.processTransitionEditor" var="msgs"/>
         <title>${msgs['grailsflow.title.processTransitions']}</title>
    </head>
    <body>
        <div class="body">
           <b class="header">${msgs['grailsflow.label.processTransitions']}</b>
           <g:render plugin="grailsflowCore" template="/commons/messageInfo"/>
           <br/>
            
           <g:form controller="${params['controller']}" method="POST">
            <input type="hidden" name="id" value="${transition?.id?.encodeAsHTML()}"/>
            <input type="hidden" name="fromNode" value="${transition?.fromNode?.id?.encodeAsHTML()}"/>

            <g:if test="${transition?.id}">
              <table>
                <tr>
                  <td>
                    <div style="border: #C0C0C0 1px solid; background-color: rgb(235,238,247); padding: 5px;">${transition?.fromNode?.nodeID?.encodeAsHTML()}</div>
                  </td>
                  <td>
                    <img src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/general',file:'arrow_right_blue.gif')}"/>
                  </td>
                  <td>
                    <div style="border: #C0C0C0 1px solid; background-color: rgb(255,230,242); padding: 7px;">${transition?.event}</div>
                  </td>
                  <td>
                     <table>
                        <g:each in="${transition.toNodes}">
                         <tr>
                           <td>
                             <img src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/general',file:'arrow_right_blue.gif')}"/>
                           </td>
                           <td>
                             <div style="border: #C0C0C0 1px solid; background-color: rgb(235,238,247); padding: 5px;">${it.nodeID}</div>
                           </td>
                         </tr>
                        </g:each>
                     </table>
                  </td>
                <tr>
              </table>
            </g:if>

              <table>
                <tr>
                  <td>${msgs['grailsflow.label.fromNode']}</td>
                  <td><input readOnly="true" class="readonly" value="${transition?.fromNode?.nodeID?.encodeAsHTML()}" size="30"/></td>
                </tr>
                <tr>
                  <td>${msgs['grailsflow.label.event']}</td>
                  <td>
                    <input name="eventID"  value="${transition?.event?.encodeAsHTML()}"/>
                    <g:if test="${transition?.id != null}">
                      &nbsp;&nbsp;&nbsp;
                      <g:link controller="${params['controller']}" action="editTranslations" id="${transition?.id}">
                          ${msgs['grailsflow.command.manageTranslations']}
                      </g:link>
                    </g:if>
                  </td>
                </tr>
                <tr><td colspan=2>
                   <table class="standard" width="100%">
                     <thead>
                       <tr>
                         <th width="10%">${msgs['grailsflow.label.toNodes']}</th>
                         <th>${msgs['grailsflow.label.nodeID']}</th>
                         <th>${msgs['grailsflow.label.type']}</th>
                       </tr>
                     </thead>
                     <tbody>
                       <g:each in="${transition?.fromNode?.processDef?.nodes}">
                         <tr>
                           <td>
                             <g:if test="${transition?.fromNode?.type == com.jcatalog.grailsflow.utils.ConstantUtils.NODE_TYPE_FORK}">
                               <g:checkBox name="toNode_${it.id}" value="${it?.incomingTransitions?.contains(transition)}" />
                             </g:if>
                             <g:else>
                               <g:radio name="toNode" value="${it.id}" checked = "${it?.incomingTransitions?.contains(transition)}" />
                             </g:else>
                           </td>
                           <td>${it.nodeID?.encodeAsHTML()}</td>
                           <td>${it.type?.encodeAsHTML()}</td>
                         </tr>
                       </g:each>
                     </tbody>
                   </table></td>
                </tr>

              </table>

              <div class="buttons">
                <span class="button">
                 <g:actionSubmit action="saveTransitionDef" value="${common['grailsflow.command.apply']}" class="button"/>
                 &nbsp;&nbsp;
                 <g:if test="${transition?.id != null}">
                   <g:actionSubmit onclick="return askConfirmation('${common['grailsflow.question.confirm']}');" action="deleteTransitonDef" value="${common['grailsflow.command.delete']}" class="button"/>
                   &nbsp;&nbsp;
                 </g:if>
                 <g:actionSubmit action="toProcessEditor" id="${transition?.fromNode?.processDef?.id}" value="${common['grailsflow.command.back']}" class="button"/>
                </span>
              </div>

           </g:form>
        </div>
    </body>
</html>
