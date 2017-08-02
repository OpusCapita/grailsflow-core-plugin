/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.jcatalog.grailsflow.utils.AuthoritiesUtils
import grails.converters.JSON
import org.springframework.web.servlet.support.RequestContextUtils

import javax.servlet.http.HttpServletResponse

/**
 * Controller for processing actions on process nodes.
 *
 * @author Alexey Zinchenko
 */
class ProcessNodeManagementController {

    def processManagerService

    /**
     * Validates input parameters and calls service method for node reservation for provided user.
     * It means that if node has multiple assignees one of them can take care of this node. Such user will be the only assignee for it
     * and other assignees won't have more access to it.
     *
     * @param processID Process key (Long type). Required.
     * @param nodeID Node ID (String type). Example: "TestApproval". Required.
     * @param user Assignee that will be the only responsible for provided node. Required.
     * @param excludedRoles Roles that will not be taken into account on assignee search(roles that mustn't be deleted from assignees). Separated with ',' sign. Optional.
     * @param excludedGroups Groups that will not be taken into account on assignee search(groups that mustn't be deleted from assignees). Separated with ',' sign. Optional.
     * @param excludedUsers Users that will not be taken into account on assignee search(users that mustn't be deleted from assignees). Separated with ',' sign. Optional.
     * @param backUrl URL to redirect after reservation complete. Optional.
     * @return If <i>backUrl</i> is provided reservation results will be available in <b>flash.message</b>, <b>flash.errors</b> or <b>flash.warnings</b> collection.
     * Otherwise it will render JSON or Map with reservation results.
     */
    def reserveNodeForUser(Long processID, String nodeID, String user) {

        List excludedRoles = params.excludedRoles?.split(',')*.trim()
        List excludedGroups = params.excludedGroups?.split(',')*.trim()
        List excludedUsers = params.excludedUsers?.split(',')*.trim()

        flash.errors = []
        flash.warnings = []

        if (!processID) {
            flash.errors << g.message(code: 'plugin.grailsflow.message.processID.required')
        }

        if (!nodeID) {
            flash.errors << g.message(code: 'plugin.grailsflow.message.nodeID.required')
        }

        if (!user) {
            flash.errors << g.message(code: 'plugin.grailsflow.message.user.required')
        }

        if (!flash.errors) {

            Set<String> excludedAssignees = []

            excludedAssignees += AuthoritiesUtils.getUserAuthorities(excludedUsers)
            excludedAssignees += AuthoritiesUtils.getRoleAuthorities(excludedRoles)
            excludedAssignees += AuthoritiesUtils.getGroupAuthorities(excludedGroups)

            Map result = processManagerService.reserveNodeForUser(processID, nodeID, user, RequestContextUtils.getLocale(request), excludedAssignees)

            if (result.error) {
                flash.errors << result.error
            }

            if (result.message) {
                flash.message = result.message
            }

            if (result.warning) {
                flash.warnings << result.warning
            }
        }

        if (flash.errors) {
            log.error(flash.errors.join('\n'))
        }

        withFormat {
            html {
                redirect(url: params.backUrl ?: request.getHeader('Referer'))
            }

            json {
                if (flash.errors) {
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                }
                render([message: flash.message, warnings: flash.warnings, errors: flash.errors] as JSON)
            }
        }
    }
}
