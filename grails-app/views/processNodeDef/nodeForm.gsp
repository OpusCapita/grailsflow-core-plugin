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
         <gf:messageBundle bundle="grailsflow.processNodeEditor" var="msgs"/>
         <gf:messageBundle bundle="grailsflow.processVariableEditor" var="varMsgs"/>
         <title>${msgs['grailsflow.title.processNode']}</title>
         <r:script>
           function showRules() {
             window.open("${g.createLink(action:'showPageRules')}", "PageRules", 'width=650, height=700, resizable=yes, scrollbars=yes, status=no')
           }

           function updateOptionsView(selectBox){
               var visible="none"
               if (selectBox.value == '${com.jcatalog.grailsflow.utils.ConstantUtils.NODE_TYPE_WAIT}') {
                   visible = ""
               }
               document.getElementById("waitNodeOptions").style.display = visible
           }

           function setActiveOption(arg) {
               if (arg == 2) {
                   document.getElementById("customPage").style.display = ""
                   document.getElementById("customContrAndPage").style.display = "none"
               } else if (arg == 3) {
                   document.getElementById("customPage").style.display = "none"
                   document.getElementById("customContrAndPage").style.display = ""
               } else if (arg == 1) {
                   document.getElementById("customPage").style.display = "none"
                   document.getElementById("customContrAndPage").style.display = "none"
               } else if (arg == 4) {
                   document.getElementById("customPage").style.display = "none"
                   document.getElementById("customContrAndPage").style.display = "none"
               }
           }
           function setPageName(arg) {
             document.getElementById("pageName").value = arg
           }

         </r:script>
    </head>
    <body>
      <h1>${msgs['grailsflow.label.processNode']}</h1>

      <g:render plugin="grailsflow" template="/commons/messageInfo"/>

      <g:form controller="${params['controller']}" method="POST">
        <input type="hidden" name="id" value="${process?.id?.encodeAsHTML()}"/>
        <input type="hidden" name="processType" value="${process?.processID?.encodeAsHTML()}"/>
        <input type="hidden" id="ndID" name="ndID" value="${node?.id?.encodeAsHTML()}"/>
        <div class="row">
          <div class="col-md-12">
            <div class="form-horizontal">

              <div class="form-group">
                <label class="col-md-2 control-label" for="nodeID">
                  ${msgs['grailsflow.label.nodeID']}
                </label>
                <div class="col-md-10">
                  <input id="nodeID" name="nodeID" value="${node?.nodeID?.encodeAsHTML()}" class="form-control" />
                  <g:if test="${node?.id != null}">
                    &nbsp;&nbsp;&nbsp;
                    <g:link controller="${params['controller']}" action="editNodeTranslations" id="${node?.id?.encodeAsHTML()}">
                      ${msgs['grailsflow.command.manageTranslations']}
                    </g:link>
                  </g:if>
                </div>
              </div>

              <div class="form-group">
                <label class="col-md-2 control-label" for="type">
                  ${msgs['grailsflow.label.type']}
                </label>
                <div class="col-md-10">
                  <g:select class="form-control"  id="type" name="type" value="${node?.type}" from="${com.jcatalog.grailsflow.utils.ConstantUtils.nodeTypes}"
                      onchange="updateOptionsView(this);" />
                </div>
              </div>

              <div class="form-group">
                <label class="col-md-2 control-label" for="dueDate">
                  ${msgs['grailsflow.label.dueDate']}
                </label>
                <div class="col-md-10">
                  <input class="form-control"  id="dueDate" name="dueDate_days" size="4" maxlength="4" value="${params.dueDate_days?.encodeAsHTML()}"/>${msgs['grailsflow.label.dueDate.days']}
                  &nbsp;<input class="form-control" name="dueDate_hours" size="4" maxlength="4" value="${params.dueDate_hours?.encodeAsHTML()}"/>${msgs['grailsflow.label.dueDate.hours']}
                  &nbsp;<input class="form-control" name="dueDate_minutes" size="4" maxlength="4" value="${params.dueDate_minutes?.encodeAsHTML()}"/>${msgs['grailsflow.label.dueDate.minutes']}
                  <br/><p style="margin: 3px; font-style:italic">${msgs['grailsflow.label.dueDate.validation']}</p>
                </div>
              </div>

              <div class="form-group">
                <label class="col-md-2 control-label" for="expectedDuration">
                  ${msgs['grailsflow.label.expectedDuration']}
                </label>
                <div class="col-md-10">
                  <input class="form-control" id="expectedDuration" name="expectedDuration_days" size="4" maxlength="4" value="${params.expectedDuration_days?.encodeAsHTML()}"/>${msgs['grailsflow.label.expectedDuration.days']}
                  &nbsp;<input class="form-control" name="expectedDuration_hours" size="4" maxlength="4" value="${params.expectedDuration_hours?.encodeAsHTML()}"/>${msgs['grailsflow.label.expectedDuration.hours']}
                  &nbsp;<input class="form-control" name="expectedDuration_minutes" size="4" maxlength="4" value="${params.expectedDuration_minutes?.encodeAsHTML()}"/>${msgs['grailsflow.label.expectedDuration.minutes']}
                  <br/><p style="margin: 3px; font-style:italic">${msgs['grailsflow.label.expectedDuration.validation']}</p>
                </div>
              </div>

              <div class="form-group">
                <label class="col-md-2 control-label" for="protocolGroup">
                  ${msgs['grailsflow.label.protocolGroup']}
                </label>
                <div class="col-md-10">
                  <input name="protocolGroup" id="protocolGroup" value="${node?.protocolGroup?.encodeAsHTML()}" class="form-control" />
                </div>
              </div>
            </div>

          </div>
        </div>

        <!-- asynchronously managing assignees -->
        <div id="waitNodeOptions" ${node && node.type == com.jcatalog.grailsflow.utils.ConstantUtils.NODE_TYPE_WAIT ? '' : 'style="display: none;"' } >
          <g:if test="${node?.id != null}">
            <div class="row">
              <div class="col-md-12">
                <gf:section title="${msgs['grailsflow.label.assignees']}" selected="true">
                  <gf:customizingTemplate template="/common/assigneesEditor"
                       model="['assignees': node?.assignees, 'controller': params['controller']]"/>
                </gf:section>
              </div>
            </div>
          </g:if>

          <div class="row">
            <div class="col-md-12">
              <gf:section title="${msgs['grailsflow.label.varVisibility']}" selected="true">
                <table class="table">
                  <thead>
                    <tr>
                      <th>${msgs['grailsflow.label.visibility']}</th>
                      <th>${varMsgs['grailsflow.label.name']}</th>
                      <th>${varMsgs['grailsflow.label.value']}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <g:each in="${process.variables}">
                      <tr>
                        <td><g:select name="visibility_${it.name}" from="${com.jcatalog.grailsflow.utils.ConstantUtils.visibilityTypes}"
                                         optionKey='key' optionValue='value'
                                         value='${node.variablesVisibility[it]}' />
                        </td>
                        <td>${it.name?.encodeAsHTML()}</td>
                        <td>${it.defaultValue?.encodeAsHTML()}</td>
                      </tr>
                    </g:each>
                  </tbody>
                </table>

                <div class="form-submit text-right">
                  <g:actionSubmit action="previewGeneratedForm" value="${msgs['grailsflow.command.previewForm']}" class="btn btn-primary"/>
                </div>
              </gf:section>
            </div>
          </div>

          <div class="row">
            <div class="col-md-12">
              <gf:section title="${msgs['grailsflow.label.manualForm']}" selected="true">
                <div id="externalEditor" class="form-horizontal">
                  <div class="form-group">
                    <label class="col-md-2 control-label" for="externalUrl">
                      ${msgs['grailsflow.label.externalUrl']}
                    </label>
                    <div class="col-md-10">
                      <input id="externalUrl" name="externalUrl" value="${externalUrl?.encodeAsHTML()}" size="100" class="form-control" />
                    </div>
                  </div>
                  <div class="form-group">
                    <div class="col-md-12">
                      <p style="margin: 3px; font-style:italic">
                        ${msgs['grailsflow.hint.externalUrl.placeholder']}
                      </p>
                    </div>
                  </div>
                </div>
                <div id="radioButtons">
                  <table>
                    <tr>
                      <td valign="top">
                        <g:radio name="manualForm" value="1" checked="${params.formType == '1'}" onclick="setActiveOption(1);"/>${msgs['grailsflow.label.form.automatic']}
                      </td>
                    </tr>
                    <tr>
                      <td valign="top">
                        <g:radio name="manualForm" value="2" checked="${params.formType == '2'}" onclick="setActiveOption(2);"/>${msgs['grailsflow.label.form.customForm']}
                      </td>
                    </tr>
                    <g:if test="${grails.util.Environment.current == grails.util.Environment.DEVELOPMENT}">
                      <tr>
                        <td>
                          <g:radio name="manualForm" value="3" checked="${params.formType == '3'}" onclick="setActiveOption(3);"/>${msgs['grailsflow.label.form.customFormAndController']} <i>${msgs['grailsflow.label.devMode.only']}</i>
                        </td>
                      </tr>
                    </g:if>
                  </table>
                </div>
                <br/>

                <g:link title="${msgs['grailsflow.command.showRules']}" onclick="showRules(); return false;">${msgs['grailsflow.command.showRules']}</g:link><br/><br/>

                <div id="customPage" ${params.formType == '2' ? '' : 'style="display: none;"'}>
                  <table>
                    <tr>
                      <td valign="top">${msgs['grailsflow.label.manualForm']}</td>
                      <td><textarea cols="80" rows="25" name="formTextArea" class="form-control">${formTextArea?.encodeAsHTML()}</textarea></td>
                    </tr>
                    <tr>
                      <td>&nbsp;</td>
                      <td><g:actionSubmit onclick="return checkCondition(${node?.id != null}, 'Please save node before')" action="generateManualForm" value="${msgs['grailsflow.command.generate']}" class="button"/></td>
                    </tr>
                  </table>
                </div>

                <div id="customContrAndPage" ${params.formType == '3' ? '' : 'style="display: none;"'}>
                  <table>
                    <tr>
                      <td valign="top">${msgs['grailsflow.label.manualForm']}</td>
                      <td><textarea cols="80" rows="15" name="pageTextArea" class="form-control">${pageTextArea?.encodeAsHTML()}</textarea></td>
                    </tr>
                    <tr>
                      <td>&nbsp;</td>
                      <td>${msgs['grailsflow.label.availablePages']}&nbsp;
                        <a name="pages" href="#" onclick="document.getElementById('multiPage').style.display=''; document.forms[0].multiStepPage.focus(); return false;">
                                ${msgs['grailsflow.command.addStepPage']}
                        </a><br/>
                        <table>
                          <g:each in="${multiPages}">
                            <tr>
                              <td style="color: green;">${it}</td>
                              <td>
                                <g:link onclick="setPageName('${it}'); return checkCondition(${node?.id != null}, 'Please save node before')" action="deleteMultiPage" value="${msgs['grailsflow.command.delete']}">
                                  <span class="glyphicon glyphicon-remove text-danger"></span>
                                </g:link>
                              </td>
                            </tr>
                          </g:each>
                        </table>
                        <br/>
                        <div id="multiPage" style="display: none;">
                                <textarea cols="80" rows="10" name="multiStepPage" class="form-control" >${multiStepPage?.encodeAsHTML()}</textarea><br/>
                                ${msgs['grailsflow.label.pageName']}&nbsp;&nbsp;<input size="35" name="pageName"  id="pageName" class="form-control"/>&nbsp;&nbsp;<g:actionSubmit onclick="return checkCondition(${node?.id != null}, 'Please save node before')" action="addMultiPage" value="${msgs['grailsflow.command.addPage']}" class="button"/>
                                &nbsp;&nbsp;<g:actionSubmit onclick="document.getElementById('multiPage').style.display='none'; return false;" value="${common['grailsflow.command.cancel']}" class="button"/>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td valign="top">${msgs['grailsflow.label.controller']}</td>
                      <td><textarea cols="80" rows="15" name="controllerTextArea" class="form-control">${controllerTextArea?.encodeAsHTML()}</textarea></td>
                    </tr>
                    <g:if test="${grails.util.Environment.current == grails.util.Environment.DEVELOPMENT}">
                      <tr>
                        <td>&nbsp;</td>
                        <td><g:actionSubmit onclick="return checkCondition(${node?.id != null}, 'Please save node before')" action="generateManualActivity" value="${msgs['grailsflow.command.generate']}" class="button"/></td>
                      </tr>
                    </g:if>
                  </table>
                  <br/>
                </div>
              </gf:section>
            </div>
          </div>

        </div>

        <div class="row">
          <div class="form-submit text-right">
            <g:actionSubmit action="showProcessEditor" value="${common['grailsflow.command.back']}" class="btn btn-link"/>
            <g:if test="${node && node.id}">
              <g:actionSubmit action="editNodeAction" value="${msgs['grailsflow.command.editNodeActions']}" class="btn btn-default"/>
            </g:if>
            <g:actionSubmit action="saveNodeDef" value="${common['grailsflow.command.apply']}" class="btn btn-primary"/>
          </div>
        </div>
      </g:form>

    </body>
</html>
