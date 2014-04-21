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

import org.codehaus.groovy.grails.commons.ControllerArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsControllerClass

/**
 * Contains custom tags.
 *
 * @author Maria Voitovich
 */
class GrailsflowRequestTagLib {
  static namespace = "gf"
  static returnObjectForTags = ['currentParams', 'currentController', 'currentAction',
                                'currentURL', 'backURL']

  static private BACK_CONTROLLER_ATTRIBUTE = '_back_controller_'
  static private BACK_ACTION_ATTRIBUTE = '_back_action_'
  static private BACK_PARAMS_ATTRIBUTE = '_back_params_'

  /**
   * returns request params excluding service information such as action
   */
  def currentParams = {
    return params?.findAll() {key, value -> key!= null && !key.startsWith('_action_') };
  }

  /**
   * returns current controller name
   */
  def currentController = {
    return params['controller']
  }

  /**
   * returns current action name
   */
  def currentAction = {
    def action = params['action']
    if (!action) {
      def controller = currentController()
      GrailsControllerClass controllerClass = grailsApplication.getArtefactByLogicalPropertyName(ControllerArtefactHandler.TYPE, controller)
      String defaultAction = controllerClass?.getDefaultAction()
      if (controllerClass?.hasProperty(defaultAction)) {
        action = defaultAction
      }
    }
    return action
  }

  def currentURL = {
    def controller = currentController()
    def action = currentAction()
    def params = currentParams()
    return g.createLink(controller: controller, action:action, params: params)
  }

  def refreshButton = { attrs ->
    out << "<input type=\"button\" onclick=\"window.location.reload()\" "
    // process attributes
    attrs.each { key, value ->
      out << "${key}=\"${value.encodeAsHTML()}\" "
    }
    out << " />"
  }

  def storeBackPoint = {
    session[BACK_CONTROLLER_ATTRIBUTE]=currentController()
    session[BACK_ACTION_ATTRIBUTE]=currentAction()
    session[BACK_PARAMS_ATTRIBUTE]=currentParams()
  }

  def backURL = {
    def controller = session[BACK_CONTROLLER_ATTRIBUTE]
    def action = session[BACK_ACTION_ATTRIBUTE]
    def params = session[BACK_PARAMS_ATTRIBUTE]
    return g.createLink(controller: controller, action:action, params: params)
  }

  def backButton = { attrs ->
    def url = backURL() ?: ''
    out << "<input type=\"button\" onclick=\"window.location.assign('${url}')\" "
    // process attributes
    attrs.each { key, value ->
      out << "${key}=\"${value.encodeAsHTML()}\" "
    }
    out << " />"
  }

}
