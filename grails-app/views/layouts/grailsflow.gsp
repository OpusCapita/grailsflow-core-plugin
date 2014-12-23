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
    <r:layoutResources/>

    <gf:messageBundle bundle="menu" var="menu_bundle"/>

    <g:layoutHead/>
  </head>
  <body>
  <div class="container">
    <div class="row">
      <div class="col-md-2 col-lg-2 col-xs-2">
          <dl id="menuList">
              <dt>${menu_bundle['grailsflow.menu.header.useProcesses']}</dt>
              <dd>
                  <ul>
                      <li><g:link controller="processDef" action="editTypes">${menu_bundle['grailsflow.menu.editProcesTypes']}</g:link></li>
                  </ul>
              </dd>
              <dd>
                  <ul>
                      <li><g:link controller="process" action="showTypes">${menu_bundle['grailsflow.menu.startProcesType']}</g:link></li>
                  </ul>
              </dd>
              <dd>
                  <ul>
                      <li><g:link controller="process" action="showWorklist">${menu_bundle['grailsflow.menu.showWorklist']}</g:link></li>
                  </ul>
              </dd>
              <dd>
                  <ul>
                      <li><g:link controller="process">${menu_bundle['grailsflow.menu.listProcesses']}</g:link></li>
                  </ul>
              </dd>
              <dt>${menu_bundle['grailsflow.menu.header.administration']}</dt>
              <dd>
                  <ul>
                      <li><g:link controller="document">${menu_bundle['grailsflow.menu.showDocuments']}</g:link></li>
                  </ul>
              </dd>
              <dd>
                  <ul>
                      <li><g:link controller="analyse">${menu_bundle['grailsflow.menu.analyseResponse']}</g:link></li>
                  </ul>
              </dd>
              <dd>
                  <ul>
                      <li><g:link controller="schedulerDetails">${menu_bundle['grailsflow.menu.viewSchedulerDetails']}</g:link></li>
                  </ul>
              </dd>
          </dl>
      </div>

      <div class="col-md-10 col-lg-10 col-xs-10">
         <g:layoutBody/>
         <r:layoutResources/>
      </div>
    </div>
  </div>

</body>
</html>