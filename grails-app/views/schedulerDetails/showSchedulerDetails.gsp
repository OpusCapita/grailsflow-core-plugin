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
  <meta name="layout" content="grailsflow"/>
  <g:render plugin="grailsflowCore" template="/commons/global"/>
  <title><g:message code="plugin.grailsflow.title.schedulerDetails"/></title>
</head>

<body>
  <h1><g:message code="plugin.grailsflow.label.schedulerDetails"/></h1>

  <g:render plugin="grailsflowCore" template="/commons/messageInfo"/>

  <div class="row">
    <div class="col-md-12">
      <div class="bs-callout bs-callout-info">
        <g:message code="plugin.grailsflow.label.schedulerDetails.desc"/>
      </div>

      <h4><g:message code="plugin.grailsflow.label.schedulerInfo"/></h4>

      <div class="bs-callout bs-callout-info">
        ${schedulerDetails?.schedulerInfo}
      </div>
    </div>
  </div>

  <g:form class="form-horizontal" controller="${params['controller']}" method="POST">

    <div class="row">
      <div class="col-md-12 col-xs-12 col-lg-12">
        <h4><g:message code="plugin.grailsflow.label.schedulerStatus"/></h4>

        <div class="bs-callout bs-callout-info">
          <g:message code="plugin.grailsflow.label.schedulerStatus.desc"/>
        </div>

        <div class="form-group">
          <label class="col-sm-1"><g:message code="plugin.grailsflow.label.paused"/></label>
          <div class="col-sm-11">${g.message(code: (schedulerDetails?.schedulerStatus?.paused) ? 'plugin.grailsflow.boolean.yes' : 'plugin.grailsflow.boolean.no')}</div>
        </div>

        <div class="form-group">
          <label class="col-sm-1"><g:message code="plugin.grailsflow.label.shutdown"/></label>
          <div class="col-sm-11">${g.message(code: (schedulerDetails?.schedulerStatus?.shutdown) ? 'plugin.grailsflow.boolean.yes' : 'plugin.grailsflow.boolean.no')}</div>
        </div>

        <div>
          <g:actionSubmit class="btn btn-default" action="pauseScheduler"
                        value="${g.message(code: (schedulerDetails?.schedulerStatus?.paused) ? 'plugin.grailsflow.label.scheduler.resume' : 'plugin.grailsflow.label.scheduler.pause')}"/>
        </div>
      </div>
    </div>

    <div class="row">
      <div class="col-md-12 col-xs-12 col-lg-12">
        <h4><g:message code="plugin.grailsflow.label.runningJobs"/></h4>
        <g:if test="${schedulerDetails?.runningJobs}">
            <div class="table-responsive">
            <table class="table">
              <thead>
              <th><g:message code="plugin.grailsflow.label.groupName"/></th>
              <th><g:message code="plugin.grailsflow.label.jobName"/></th>
              <th><g:message code="plugin.grailsflow.label.startTime"/></th>
              <th><g:message code="plugin.grailsflow.label.runningTime"/></th>
              <th><g:message code="plugin.grailsflow.label.description"/></th>
              <th><g:message code="plugin.grailsflow.label.attributes"/></th>
              <th><g:message code="plugin.grailsflow.label.triggerName"/></th>
              </thead>
              <tbody>
              <g:each in="${schedulerDetails?.runningJobs}" var="jobInfo">
                <tr>
                  <td>${jobInfo?.job?.group}</td>
                  <td>${jobInfo?.job?.name}</td>
                  <td><gf:displayDateTime value="${jobInfo?.startTime}"/></td>
                  <td>${jobInfo?.runningTime}</td>
                  <td>${jobInfo?.job?.description}</td>
                  <td>
                    <g:message code="plugin.grailsflow.label.volatile"/>&#160;
                    ${g.message(code: jobInfo?.job?.persistJobDataAfterExecution ? 'plugin.grailsflow.boolean.yes' : 'plugin.grailsflow.boolean.no')}<br/>
                    <g:message code="plugin.grailsflow.label.durable"/>&#160;
                    ${g.message(code: jobInfo?.job?.durable ? 'plugin.grailsflow.boolean.yes' : 'plugin.grailsflow.boolean.no')}<br/>
                    <g:message code="plugin.grailsflow.label.stateful"/>&#160;
                    ${g.message(code: jobInfo?.job?.concurrentExectionDisallowed ? 'plugin.grailsflow.boolean.yes' : 'plugin.grailsflow.boolean.no')}<br/>
                  </td>
                  <td>${jobInfo?.trigger?.key?.name}</td>
                </tr>
              </g:each>
              </tbody>
            </table>
            </div>
        </g:if>
        <g:else>
          <div class="bs-callout bs-callout-info">
            <g:message code="plugin.grailsflow.label.noEntries"/>
          </div>
        </g:else>
      </div>
    </div>

    <div class="row">
      <div class="col-md-12 col-xs-12 col-lg-12">
        <h4><g:message code="plugin.grailsflow.label.scheduledJobs"/></h4>

        <div class="bs-callout bs-callout-info">
          <g:message code="plugin.grailsflow.label.scheduledJobs.desc"/>
        </div>

        <g:if test="${schedulerDetails?.scheduledJobs}">
            <div class="table-responsive">
            <table class="table">
              <thead>
              <th><g:message code="plugin.grailsflow.label.groupName"/></th>
              <th><g:message code="plugin.grailsflow.label.jobName"/></th>
              <th><g:message code="plugin.grailsflow.label.description"/></th>
              <th><g:message code="plugin.grailsflow.label.lastFired"/></th>
              <th><g:message code="plugin.grailsflow.label.nextFired"/></th>
              <th>
                <nobr>
                  <g:message code="plugin.grailsflow.label.schedulingDetails"/>
                </nobr>
              </th>
              <th><g:message code="plugin.grailsflow.label.manageJobSchedule"/></th>
              </thead>
              <tbody>
              <g:each in="${schedulerDetails?.scheduledJobs}" var="jobInfo">
                <tr>
                  <td>${jobInfo?.job?.group}</td>
                  <td>
                    <g:set var="jobController"
                           value="${grailsApplication.getArtefact('Controller', jobInfo?.job?.name + 'Controller')}"/>
                    <g:if test="${jobController}">
                      <g:link controller="${jobController.logicalPropertyName}"
                              title="${g.message(code: 'plugin.grailsflow.label.configuration')}">
                        ${jobInfo?.job?.name}
                      </g:link>
                    </g:if>
                    <g:else>
                      ${jobInfo?.job?.name}
                    </g:else>
                  </td>
                  <td>${jobInfo?.job?.description}</td>
                  <td><gf:displayDateTime value="${jobInfo?.previosFireTime}"/></td>
                  <td><gf:displayDateTime value="${jobInfo?.nextFireTime}"/></td>
                  <td>${jobInfo?.executionTimeText}</td>
                  <td>
                    <g:if test="${jobInfo?.running}">
                      <g:message code="plugin.grailsflow.label.running"/>
                    </g:if>
                    <g:else>
                      <div class="btn-group input-group-btn form-submit text-right">
                        <nobr>
                          <g:link class="btn btn-sm btn-default" controller="${params['controller']}" action="pause"
                                  params="${[name: jobInfo?.job?.name, group: jobInfo?.job?.group, isPaused: jobInfo?.paused ? jobInfo?.paused : 'false', isRunning: jobInfo?.running ? jobInfo.running : 'false']}"
                                  title="${g.message(code: (jobInfo?.paused) ? 'plugin.grailsflow.label.resume' : 'plugin.grailsflow.label.pause')}">
                            <g:if test="${jobInfo?.paused}">
                              <span class="glyphicon glyphicon-play"></span>&nbsp;
                            </g:if>
                            <g:else>
                              <span class="glyphicon glyphicon-pause"></span>&nbsp;
                            </g:else>
                            ${g.message(code: (jobInfo?.paused) ? 'plugin.grailsflow.label.resume' : 'plugin.grailsflow.label.pause')}
                          </g:link>
                          <g:link class="btn btn-sm btn-default" controller="${params['controller']}" action="delete"
                                  params="${[name: jobInfo?.job?.name, group: jobInfo?.job?.group]}"
                                  onclick="return window.confirm('${g.message(code: 'plugin.grailsflow.message.job.delete')}');"
                                  title="${g.message(code: 'plugin.grailsflow.label.delete')}">
                            <span class="glyphicon glyphicon-remove text-danger"></span>&nbsp;
                            <g:message code="plugin.grailsflow.label.delete"/>
                          </g:link>
                          <g:link class="btn btn-sm btn-default" controller="${params['controller']}" action="edit"
                                  params="${[name: jobInfo?.trigger?.name, group: jobInfo?.trigger?.group]}"
                                  title="${g.message(code: 'plugin.grailsflow.label.edit')}">
                            <span class="glyphicon glyphicon-edit"></span>&nbsp;
                            <g:message code="plugin.grailsflow.label.edit"/>
                          </g:link>
                        </nobr>
                      </div>
                    </g:else>
                  </td>
                </tr>
              </g:each>
              </tbody>
            </table>
            </div>
        </g:if>
        <g:else>
          <div class="bs-callout bs-callout-info">
            <g:message code="plugin.grailsflow.label.noEntries"/>
          </div>
        </g:else>

        <div class="form-submit text-right">
          <g:actionSubmit class="btn btn-primary" action="scheduleProcess"
                            value="${g.message(code: 'plugin.grailsflow.command.scheduleProcess')}"/>
        </div>
      </div>
    </div>
  </g:form>

</body>
</html>
