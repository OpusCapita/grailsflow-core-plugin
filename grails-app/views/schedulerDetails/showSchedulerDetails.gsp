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
    <gf:messageBundle bundle="grailsflow.schedulerDetails" var="msgs"/>
    <g:render plugin="grailsflowCore" template="/commons/global"/>
    <title>${msgs['grailsflow.title.schedulerDetails']}</title>
  </head>
  <body>
    <div class="body">
      <b class="header">${msgs['grailsflow.label.schedulerDetails']}</b>

      <g:render plugin="grailsflowCore" template="/commons/messageInfo"/>

      <p>${msgs['grailsflow.label.schedulerDetails.desc']}</p>

      <h2 class="headline">${msgs['grailsflow.label.schedulerInfo']}</h2>
      <p>${schedulerDetails?.schedulerInfo}</p>

      <g:form controller="${params['controller']}" method="POST">
        <h2 class="headline">${msgs['grailsflow.label.schedulerStatus']}</h2>
        <p>${msgs['grailsflow.label.schedulerStatus.desc']}</p>
        <table>
          <tr><td>${msgs['grailsflow.label.paused']}: <b>${msgs[(schedulerDetails?.schedulerStatus?.paused) ? 'grailsflow.boolean.yes' : 'grailsflow.boolean.no']}</b></td></tr>
          <tr><td>${msgs['grailsflow.label.shutdown']}:<b>${msgs[(schedulerDetails?.schedulerStatus?.shutdown) ? 'grailsflow.boolean.yes' : 'grailsflow.boolean.no']}</b></td></tr>
          <tr>
            <td>
              <g:actionSubmit class="button" action="pauseScheduler" value="${msgs[(schedulerDetails?.schedulerStatus?.paused) ? 'grailsflow.label.scheduler.resume' : 'grailsflow.label.scheduler.pause']}"/>
            </td>
          </tr>
        </table>

        <h2 class="headline">${msgs['grailsflow.label.runningJobs']}</h2>
        <table class="standard">
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
            <g:if test="${schedulerDetails?.runningJobs}">
              <g:each in="${schedulerDetails?.runningJobs}" var="jobInfo">
                <tr>
                  <td>${jobInfo?.job?.group}</td>
                  <td>${jobInfo?.job?.name}</td>
                  <td><gf:displayDateTime value="${jobInfo?.startTime}"/></td>
                  <td>${jobInfo?.runningTime}</td>
                  <td>${jobInfo?.job?.description}</td>
                  <td>
                    ${msgs['grailsflow.label.volatile']}&#160;
                    ${msgs[(jobInfo?.job ? jobInfo?.job.persistJobDataAfterExecution : false) ? 'grailsflow.boolean.yes' : 'grailsflow.boolean.no']}<br/>
                    ${msgs['grailsflow.label.durable']}&#160;
                    ${msgs[(jobInfo.job.durable) ? 'grailsflow.boolean.yes' : 'grailsflow.boolean.no']}<br/>
                    ${msgs['grailsflow.label.stateful']}&#160;
                    ${msgs[(jobInfo?.job?.concurrentExectionDisallowed) ? 'grailsflow.boolean.yes' : 'grailsflow.boolean.no']}<br/>
                  </td>
                  <td>${jobInfo?.trigger?.key?.name}</td>
                </tr>
              </g:each>
            </g:if>
            <g:else>
              <tr><td><b>${msgs['grailsflow.label.noEntries']}</b></td></tr>
            </g:else>
          </tbody>
        </table>
          
        <br/>

        <h2 class="headline">${msgs['grailsflow.label.scheduledJobs']}</h2>
        <p>${msgs['grailsflow.label.scheduledJobs.desc']}</p>
        <table class="standard">
          <thead>
            <th>${msgs['grailsflow.label.groupName']}</th>
            <th>${msgs['grailsflow.label.jobName']}</th>
            <th>${msgs['grailsflow.label.description']}</th>
            <th>${msgs['grailsflow.label.lastFired']}</th>
            <th>${msgs['grailsflow.label.nextFired']}</th>
            <th>${msgs['grailsflow.label.schedulingDetails']}</th>
            <th>${msgs['grailsflow.label.manageJobSchedule']}</th>
          </thead>
          <tbody>
            <g:if test="${schedulerDetails?.scheduledJobs}">
              <g:each in="${schedulerDetails?.scheduledJobs}" var="jobInfo">
                <tr>
                  <td>${jobInfo?.job?.group}</td>
                  <td>
                    <g:set var="jobController" value="${grailsApplication.getArtefact('Controller', jobInfo?.job?.name+'Controller')}"/>
                    <g:if test="${jobController}">
                      <g:link controller="${jobController.logicalPropertyName}" title="${msgs['grailsflow.label.configuration']}">
                        ${jobInfo?.job?.name}
                      </g:link>
                    </g:if>
                    <g:else>
                      ${jobInfo?.job?.name}
                    </g:else>
                  </td>
                  <td>${jobInfo?.job?.description}</td>
                  <td><gf:displayDateTime value="${jobInfo?.previosFireTime}"/></td>
                  <td><gf:displayDateTime value="${jobInfo?.nextFireTime}" /></td>
                  <td>${jobInfo?.executionTimeText}</td>
                  <td>
                    <g:if test="${jobInfo?.running}">
                      ${msgs['grailsflow.label.running']}
                    </g:if>
                    <g:else>
	                    <g:link controller="${params['controller']}" action="pause"
	                           params="${[name: jobInfo?.job?.name, group: jobInfo?.job?.group, isPaused: jobInfo?.paused ? jobInfo?.paused : 'false', isRunning: jobInfo?.running ? jobInfo.running : 'false']}"
	                           title=" ${msgs[(jobInfo?.paused) ? 'grailsflow.label.resume' : 'grailsflow.label.pause']}">
	                      ${msgs[(jobInfo?.paused) ? 'grailsflow.label.resume' : 'grailsflow.label.pause']}
	                    </g:link>&nbsp;
	                    <g:link controller="${params['controller']}" action="delete"
	                           params="${[name: jobInfo?.job?.name, group: jobInfo?.job?.group]}"
	                           onclick="return window.confirm('${msgs['grailsflow.message.job.delete']}');"
	                           title="${msgs['grailsflow.label.delete']}">
	                      ${msgs['grailsflow.label.delete']}
	                    </g:link>&nbsp;
	                    <g:link controller="${params['controller']}" action="edit"
	                           params="${[name: jobInfo?.trigger?.name, group: jobInfo?.trigger?.group]}"
	                           title="${msgs['grailsflow.label.edit']}">
	                      ${msgs['grailsflow.label.edit']}
	                    </g:link>&nbsp;
                    </g:else>
                  </td>
                </tr>
              </g:each>
            </g:if>
            <g:else>
              <tr><td><b>${msgs['grailsflow.label.noEntries']}</b></td></tr>
            </g:else>
          </tbody>
        </table>

        <g:actionSubmit class="button" action="scheduleProcess" value="${msgs['grailsflow.command.scheduleProcess']}"/>
          
      </g:form>
    </div>
  </body>
</html>
