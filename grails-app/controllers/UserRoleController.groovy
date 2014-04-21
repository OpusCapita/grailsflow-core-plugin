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


/**
 * UserRoleController used for accessing available users and roles.
 *
 * TODO use good name for controller
 *
 * @author Maria Voitovich
 */
class UserRoleController extends GrailsFlowSecureController {
    def usersProvider
    def rolesProvider
    def groupsProvider
    
    def index = {
        redirect(action: "listUsers")
    }

    def listUsers = {
        def users = usersProvider.getUsers()
        render(view: "listUsers",
               model:[users: users])
    }

    def listRoles = {
        def roles = rolesProvider.getRoles()
        render(view: "listRoles",
               model:[roles: roles])
    }

    def listGroups = {
        def groups = groupsProvider.getGroups()
        render(view: "listGroups",
               model:[groups: groups])
    }

}