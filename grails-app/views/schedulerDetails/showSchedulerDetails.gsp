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
  <gf:messageBundle bundle="grailsflow.schedulerDetails" var="msgs"/>
  <g:render plugin="grailsflowCore" template="/commons/global"/>
  <title>${msgs['grailsflow.title.schedulerDetails']}</title>
</head>

<body>
  <div class="row">
    <div class="col-md-12 col-xs-12 col-lg-12">
      <h3>${msgs['grailsflow.label.schedulerDetails']}</h3>
    </div>
  </div>
  <div class="row">
    <div class="col-md-12 col-xs-12 col-lg-12">
      <g:render plugin="grailsflowCore" template="/commons/messageInfo"/>
    </div>
  </div>
  <div class="row">
    <div class="col-md-12 col-xs-12 col-lg-12">
      <div class="bs-callout bs-callout-info">
        ${msgs['grailsflow.label.schedulerDetails.desc']}
      </div>

      <h4>${msgs['grailsflow.label.schedulerInfo']}</h4>

      <div class="bs-callout bs-callout-info">
        ${schedulerDetails?.schedulerInfo}
      </div>
    </div>
  </div>

  <g:form class="form-horizontal" controller="${params['controller']}" method="POST">

    <div class="row">
      <div class="col-md-12 col-xs-12 col-lg-12">
        <h4>${msgs['grailsflow.label.schedulerStatus']}</h4>

        <div class="bs-callout bs-callout-info">
          ${msgs['grailsflow.label.schedulerStatus.desc']}
        </div>

        <div class="form-group">
          <label class="col-sm-1">${msgs['grailsflow.label.paused']}</label>
          <div class="col-sm-11">${msgs[(schedulerDetails?.schedulerStatus?.paused) ? 'grailsflow.boolean.yes' : 'grailsflow.boolean.no']}</div>
        </div>

        <div class="form-group">
          <label class="col-sm-1">${msgs['grailsflow.label.shutdown']}</label>
          <div class="col-sm-11">${msgs[(schedulerDetails?.schedulerStatus?.shutdown) ? 'grailsflow.boolean.yes' : 'grailsflow.boolean.no']}</div>
        </div>

        <div>
          <g:actionSubmit class="btn btn-default" action="pauseScheduler"
                        value="${msgs[(schedulerDetails?.schedulerStatus?.paused) ? 'grailsflow.label.scheduler.resume' : 'grailsflow.label.scheduler.pause']}"/>
        </div>
      </div>
    </div>

    <div class="row">
      <div class="col-md-12 col-xs-12 col-lg-12">
        <h4>${msgs['grailsflow.label.runningJobs']}</h4>
        <g:if test="${schedulerDetails?.runningJobs}">
            <div class="table-responsive">
            <table class="table table-bordered">
              <thead>
              <th>${msgs['grailsflow.label.groupName']}</th>
              <th>${msgs['grailsflow.label.jobName']}</th>
              <th>${msgs['grailsflow.label.startTime']}</th>
              <th>${msgs['grailsflow.label.runningTime']}</th>
              <th>${msgs['grailsflow.label.description']}</th>
              <th>${msgs['grailsflow.label.attributes']}</th>
              <th>${msgs['grailsflow.label.triggerName']}</th>
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
                    ${msgs['grailsflow.label.volatile']}&#160;
                    ${msgs[jobInfo?.job?.persistJobDataAfterExecution ? 'grailsflow.boolean.yes' : 'grailsflow.boolean.no']}<br/>
                    ${msgs['grailsflow.label.durable']}&#160;
                    ${msgs[jobInfo?.job?.durable ? 'grailsflow.boolean.yes' : 'grailsflow.boolean.no']}<br/>
                    ${msgs['grailsflow.label.stateful']}&#160;
                    ${msgs[jobInfo?.job?.concurrentExectionDisallowed ? 'grailsflow.boolean.yes' : 'grailsflow.boolean.no']}<br/>
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
            ${msgs['grailsflow.label.noEntries']}
          </div>
        </g:else>
      </div>
    </div>

    <div class="row">
      <div class="col-md-12 col-xs-12 col-lg-12">
        <h4>${msgs['grailsflow.label.scheduledJobs']}</h4>

        <div class="bs-callout bs-callout-info">
          ${msgs['grailsflow.label.scheduledJobs.desc']}
        </div>

        <g:if test="${schedulerDetails?.scheduledJobs}">
            <div class="table-responsive">
            <table class="table table-bordered">
              <thead>
              <th>${msgs['grailsflow.label.groupName']}</th>
              <th>${msgs['grailsflow.label.jobName']}</th>
              <th>${msgs['grailsflow.label.description']}</th>
              <th>${msgs['grailsflow.label.lastFired']}</th>
              <th>${msgs['grailsflow.label.nextFired']}</th>
              <th>
                <nobr>
                  ${msgs['grailsflow.label.schedulingDetails']}
                </nobr>
              </th>
              <th>${msgs['grailsflow.label.manageJobSchedule']}</th>
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
                              title="${msgs['grailsflow.label.configuration']}">
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
                      ${msgs['grailsflow.label.running']}
                    </g:if>
                    <g:else>
                      <div class="btn-group input-group-btn">
                        <nobr>
                          <g:link class="btn btn-sm btn-default" controller="${params['controller']}" action="pause"
                                  params="${[name: jobInfo?.job?.name, group: jobInfo?.job?.group, isPaused: jobInfo?.paused ? jobInfo?.paused : 'false', isRunning: jobInfo?.running ? jobInfo.running : 'false']}"
                                  title="${msgs[(jobInfo?.paused) ? 'grailsflow.label.resume' : 'grailsflow.label.pause']}">
                            <g:if test="${jobInfo?.paused}">
                              <span class="glyphicon glyphicon-play"></span>&nbsp;
                            </g:if>
                            <g:else>
                              <span class="glyphicon glyphicon-pause"></span>&nbsp;
                            </g:else>
                            ${msgs[(jobInfo?.paused) ? 'grailsflow.label.resume' : 'grailsflow.label.pause']}
                          </g:link>
                          <g:link class="btn btn-sm btn-default" controller="${params['controller']}" action="delete"
                                  params="${[name: jobInfo?.job?.name, group: jobInfo?.job?.group]}"
                                  onclick="return window.confirm('${msgs['grailsflow.message.job.delete']}');"
                                  title="${msgs['grailsflow.label.delete']}">
                            <span class="glyphicon glyphicon-trash"></span>&nbsp;
                            ${msgs['grailsflow.label.delete']}
                          </g:link>
                          <g:link class="btn btn-sm btn-default" controller="${params['controller']}" action="edit"
                                  params="${[name: jobInfo?.trigger?.name, group: jobInfo?.trigger?.group]}"
                                  title="${msgs['grailsflow.label.edit']}">
                            <span class="glyphicon glyphicon-edit"></span>&nbsp;
                            ${msgs['grailsflow.label.edit']}
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
            ${msgs['grailsflow.label.noEntries']}
          </div>
        </g:else>

        <div>
          <g:actionSubmit class="btn btn-primary" action="scheduleProcess"
                            value="${msgs['grailsflow.command.scheduleProcess']}"/>
        </div>
      </div>
    </div>
  </g:form>

</body>
</html>
