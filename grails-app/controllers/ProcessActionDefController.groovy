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

import com.jcatalog.grailsflow.model.definition.ProcessNodeDef
import com.jcatalog.grailsflow.model.definition.ActionStatement

import com.jcatalog.grailsflow.utils.ClassUtils

/**
 * ProcessActionDefController class manages action statements and action
 * parameters.
 *
 * @author Stephan Albers
 * @author July Karpey
 * @author Maria Voitovich
 */
class ProcessActionDefController extends GrailsFlowSecureController {
    def actionFactory
   
    def index = {
        redirect(controller: "processDef")
    }

    // the delete, save and update actions only
    // accept POST requests
    def static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def showEditor = {
      def processNodeDef = ProcessNodeDef.get(Long.valueOf(params.ndID))
      def actionsCode = ""
      if (processNodeDef?.actionStatements) {
          actionsCode = processNodeDef?.actionStatements.collect({
              it.content ?: ""
          }).join('\n')
      }

      def processDef = processNodeDef?.processDef
      def variables = processDef?.variables ? processDef.variables*.name : []
      def actionsNames = actionFactory.getActionTypes()
      def actions = []
      actionsNames?.each() { name ->
        def option = [:]
        option.label = name
        option.value = name
        def actionClass = actionFactory.getActionClassForName(name)
        option.group = actionClass?.package?.name ?: ""
        if (option.value != null) {
          actions << option
        }
      }

      render(view: "actionEditor",
              model:[processNodeDef: processNodeDef, actionsCode: actionsCode,
                     variables: variables.sort({var1, var2 -> var1.compareToIgnoreCase(var2)}), actions: actions])
    }

    def actionParametersEditor = {
      def actionName = params.actionName
      def actionClass = actionFactory.getActionClassForName(actionName)
      if (actionClass != null) {
        try {
          def props = ClassUtils.getActionClassProperties(actionClass)
          def action = actionClass.newInstance()
          def actionParameters = []
          props?.each() { propName ->
            def value = action[propName]
            def actionParameter = [name: propName, value: value]
            actionParameters << actionParameter
          }
          def processNodeDef = ProcessNodeDef.get(Long.valueOf(params.nodeID))
          def processDef = processNodeDef?.processDef
          def variables = processDef?.variables ? processDef.variables*.name : []
          render(view: "actionParametersEditor",
                  model:[actionName: actionName, actionParameters: actionParameters,
                          variables: variables])
        } catch (Exception e) {
          log.error("Cannot get get signature for action ${actionName}", e)
          return null
        }
      } else {
        log.error("Cannot get class for action ${actionName}")
        return null
      }
    }


    def showProcessNodeEditor = {
        redirect(controller: "processNodeDef", action: "editNodeDef", params: [id: params.nodeID] )
    }

    def saveActions = {
        def processNodeDef = ProcessNodeDef.get(Long.valueOf(params.nodeID))

        processNodeDef.actionStatements*.delete()
        processNodeDef.actionStatements?.clear()
        if (params.actionsCode) {
            params.actionsCode.eachLine() { line ->
                processNodeDef.addToActionStatements(new ActionStatement(content: line))
            }
        }
        processNodeDef.save(flush:true)
       
        redirect(action: "showEditor", params: [id: processNodeDef.processDef.id, ndID: processNodeDef.id])
    }


}