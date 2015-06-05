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
  <gf:messageBundle bundle="grailsflow.processDetails" var="processDetails"/>
  <title>${processDetails['grailsflow.title.deleteProcesses']}</title>
</head>

<body>
  <gf:customizingTemplate template="/${params['controller']}/processList/header"
                        defaultTemplate="/process/processList/header" model="${['header': 'deleteProcesses']}"/>

  <g:form name="processListForm" controller="${params['controller']}" method="POST">
    <input type="hidden" name="returnPage" value="deleteProcesses">
    <div class="row">
      <div class="col-md-12">
        <gf:customizingTemplate template="/${params['controller']}/processList/searchForm"
            defaultTemplate="/process/processList/searchForm"
            model="${['statuses': com.jcatalog.grailsflow.status.ProcessStatusEnum.values().findAll {it.isFinal()}, 'returnPage': 'deleteProcesses']}"/>

        <div class="form-submit text-right">
          <g:actionSubmit action="searchFinishedProcesses" value="${common['grailsflow.command.search']}" class="btn btn-default"/>
          <g:actionSubmit action="deleteSearchedProcesses" onclick="return askConfirmation('${common['grailsflow.question.confirm']}');" value="${common['grailsflow.command.delete']}" class="btn btn-default"/>
          <g:actionSubmit action="deleteAllProcesses" onclick="return askConfirmation('${common['grailsflow.question.confirm']}');" value="${common['grailsflow.command.deleteAll']}" class="btn btn-primary"/>
        </div>
      </div>
    </div>

    <div class="row">
      <div class="col-md-12">

        <gf:customizingTemplate template="/${params['controller']}/processList/searchResults"
                                  defaultTemplate="/process/processList/searchResults" model="${['returnPage': 'deleteProcesses']}"/>

        <g:if test="${itemsTotal && itemsTotal > (grailsApplication.mainContext.maxResultSize)}">
          <div class="paginateButtons">
            <g:paginate total="${itemsTotal}" id="${params.id}" params="${params}" controller="${params['controller']}" action="search"/>
          </div>
        </g:if>
        <g:elseif test="${!itemsTotal}">
          <div class="bs-callout bs-callout-info">${common['grailsflow.message.noItems']}</div>
        </g:elseif>
      </div>
    </div>

</g:form>
</body>
</html>
