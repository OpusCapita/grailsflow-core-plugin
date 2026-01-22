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
import com.jcatalog.grailsflow.model.definition.ProcessTransitionDef

import com.jcatalog.grailsflow.utils.NameUtils

import grails.converters.JSON

/**
 * Process transition definition controller class is used for working with
 * transition definitions.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class ProcessTransitionDefController extends GrailsFlowSecureController {

    static allowedMethods = [
        delete             : 'POST',
        save               : 'POST',
        update             : 'POST',
        deleteTransitionDef: 'DELETE'
    ]

    def index = {
        redirect(controller: "processDef")
    }

    def addTransitonDef = {
        def node = ProcessNodeDef.get(params.long('fromNode'))
        render(view: 'transitionForm',
               model: [transition: new ProcessTransitionDef(fromNode: node) ] )
    }

    def editTransitonDef = {
        def trans = ProcessTransitionDef.get(params.long('id'))
        render(view: 'transitionForm', model: [transition: trans])
    }

    def saveTransitionDef = {
        def fromNode = ProcessNodeDef.get(params.long('fromNode'))
        def event = params.eventID?.trim()
        def process = fromNode.processDef

        def finishNodes = []
        if (params.toNode) {
            finishNodes << ProcessNodeDef.get(params.long('toNode'))
        } else {
            process.nodes.each {
                if (params["toNode_" + it.id] == "true"
                    || params["toNode_" + it.id] == "on") {
                    finishNodes << it
                }
            }
        }
        if (!event || fromNode == null || finishNodes.isEmpty()) {
            flash.errors = [g.message(code: "plugin.grailsflow.message.transition.required")]
            render(view: 'transitionForm', model: [transition: new ProcessTransitionDef(fromNode: fromNode, event: event)] )
        } else {

            // Validate parameters
            if (!NameUtils.isValidIdentifier(event)) {
                flash.errors = [g.message(code: "plugin.grailsflow.message.event.invalid")]
                return render(view: 'transitionForm', model: [transition: new ProcessTransitionDef(fromNode: fromNode, event: event)] )
            }

            def duplication = ProcessTransitionDef.findWhere(fromNode: fromNode, event: event)
            if (duplication != null && duplication.id.toString() != params.id) {
                flash.errors = [g.message(code: "plugin.grailsflow.message.transition.invalid")]
                render(view: 'transitionForm', model: [transition: new ProcessTransitionDef(fromNode: fromNode, event: event)])
            } else {
              def transition
			        if (params.id) {
			            transition = ProcessTransitionDef.get(params.long('id'))
			        } else {
			            transition = new ProcessTransitionDef(fromNode: fromNode)
			        }
			        transition.toNodes = finishNodes
						  transition.event = event
			        transition.save()
			        redirect(controller: "processDef", action: 'editProcess', params: [id: process.id])
            }
        }
    }

    def editTranslations = {
        if (!flash.message) flash.message = ""
        def transition = ProcessTransitionDef.get(params.long('id'))
        if (!transition) {
            flash.errors = ["Impossible to edit transition with key ${params.id}"]
            return redirect(controller: 'processDef', action: 'editTypes')
        }
        render(view: 'editTranslations', model: [transition: transition])
    }

    def saveTranslations = {
        if (!flash.message) flash.message = []
        def transition = ProcessTransitionDef.get(params.long('id'))
        if (!transition) {
            flash.errors = ["Impossible to edit transition with key ${params.id}"]
            return redirect(controller: 'processDef', action: 'editTypes')
        }
        def labels = GrailsflowRequestUtils.getTranslationsMapFromParams(params, 'label_')
        transition.label = labels
        transition.save()
        redirect(action: 'editTransitonDef', params: [id: params.id])
    }

    def deleteTransitionDef = {
        def transition = ProcessTransitionDef.get(params.long('id'))

        def result
        if (transition) {
            def processDefId = transition.fromNode.processDef.id
            transition.removeFromAssociations()
            transition.delete(flush: true)
            result = [success: true, processDefId: processDefId]
        } else {
            def errorMessage = 'Transition def not found'
            log.debug(errorMessage)
            result = [success: false, error: [code: 'NOT_FOUND', message: errorMessage]]
        }
        render result as JSON
    }

    def toProcessEditor = {
      def fromNode = ProcessNodeDef.get(params.long('fromNode'))
      def processDefID = fromNode?.processDef?.id
      if (processDefID != null) {
        redirect(controller: "processDef", action: 'editProcess', params: [id: processDefID])
      } else {
        redirect(controller: "process", action: 'showTypes')
      }
    }

}

