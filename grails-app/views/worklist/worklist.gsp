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
         <gf:messageBundle bundle="grailsflow.worklist" var="worklist"/>
         <title>${worklist['grailsflow.title.worklist']}</title>
    </head>
    <body>
      <gf:storeBackPoint />
      <h1>${worklist['grailsflow.label.worklist']}</h1>

      <g:render plugin="grailsflow" template="/commons/messageInfo"/>

      <div class="row">
          <g:form name="worklistForm" method="GET"
            controller="${gf.currentController()}" action="${gf.currentAction()}">

            <gf:customizingTemplate template="searchForm" defaultTemplate="/worklist/searchForm"/>

            <div class="form-submit text-right">
              <g:actionSubmit action="${gf.currentAction()}" value="${common['grailsflow.command.search']}" class="btn btn-primary"/>
            </div>

            <gf:customizingTemplate template="searchResults" defaultTemplate="/worklist/searchResults"/>

            <g:if test="${itemsTotal}">
              <div class="paginateButtons">
                <g:paginate total="${itemsTotal}"  
                  controller="${gf.currentController()}" action="${gf.currentAction()}"
                  params="${gf.currentParams()}"/>
              </div>
            </g:if>

            <div class="form-submit text-right">
              <gf:refreshButton value="${common['grailsflow.command.refresh']}" class="btn btn-link"/>
            </div>

          </g:form>
      </div>
    </body>
</html>
