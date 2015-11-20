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
<!DOCTYPE html>
<html>
  <head>                      
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>
      <g:layoutTitle default="Grailsflow"/>
    </title>

    <r:require modules="grailsflow"/>
    <r:require modules="bootstrap"/>
    <r:require modules="font-awesome"/>
    <r:layoutResources/>

    <g:layoutHead/>
  </head>
  <body>
  <div class="container">
    <div class="row">
      <div class="col-md-2">
          <dl id="menuList">
              <dt><g:message code="plugin.grailsflow.menu.header.useProcesses"/></dt>
              <dd>
                  <ul>
                      <li><g:link controller="processDef" action="editTypes"><g:message code="plugin.grailsflow.menu.editProcesTypes"/></g:link></li>
                  </ul>
              </dd>
              <dd>
                  <ul>
                      <li><g:link controller="process" action="showTypes"><g:message code="plugin.grailsflow.menu.startProcesType"/></g:link></li>
                  </ul>
              </dd>
              <dd>
                  <ul>
                      <li><g:link controller="process" action="showWorklist"><g:message code="plugin.grailsflow.menu.showWorklist"/></g:link></li>
                  </ul>
              </dd>
              <dd>
                  <ul>
                      <li><g:link controller="process"><g:message code="plugin.grailsflow.menu.listProcesses"/></g:link></li>
                  </ul>
              </dd>
              <dt><g:message code="plugin.grailsflow.menu.header.administration"/></dt>
              <dd>
                  <ul>
                      <li><g:link controller="process" action="deleteProcesses"><g:message code="plugin.grailsflow.menu.deleteProcesses"/></g:link></li>
                  </ul>
              </dd>
              <dd>
                  <ul>
                      <li><g:link controller="document"><g:message code="plugin.grailsflow.menu.showDocuments"/></g:link></li>
                  </ul>
              </dd>
              <dd>
                  <ul>
                      <li><g:link controller="analyse"><g:message code="plugin.grailsflow.menu.analyseResponse"/></g:link></li>
                  </ul>
              </dd>
              <dd>
                  <ul>
                      <li><g:link controller="schedulerDetails"><g:message code="plugin.grailsflow.menu.viewSchedulerDetails"/></g:link></li>
                  </ul>
              </dd>
          </dl>
      </div>

      <div class="col-md-10">
         <g:layoutBody/>
         <r:layoutResources/>
      </div>
    </div>
  </div>

</body>
</html>