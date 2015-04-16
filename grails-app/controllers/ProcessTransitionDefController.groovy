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

/**
 * Process transition definition controller class is used for working with
 * transition definitions.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class ProcessTransitionDefController extends GrailsFlowSecureController {
    private static final String RESOURCE_BUNDLE = "grailsflow.processTypes"
    def grailsflowMessageBundleService
    
    def index = {
        redirect(controller: "processDef")
    }

    // the delete, save and update actions only accept POST requests
    def static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def addTransitonDef = {
        def node = params.fromNode ? ProcessNodeDef.get(Long.valueOf(params.fromNode)) : null
        render(view: 'transitionForm',
               model: [transition: new ProcessTransitionDef(fromNode: node) ] )
    }

    def editTransitonDef = {
        def trans = ProcessTransitionDef.get(Long.valueOf(params.id))
        render(view: 'transitionForm', model: [transition: trans])
    }

    def saveTransitionDef = {
        def fromNode = params.fromNode ? ProcessNodeDef.get(Long.valueOf(params.fromNode)) : null
        def event = params.eventID?.trim()
        def process = fromNode.processDef

        def finishNodes = []
        if (params.toNode) {
            finishNodes << ProcessNodeDef.get(Long.valueOf(params.toNode))
        } else {
            process.nodes.each {
                if (params["toNode_" + it.id] == "true"
                    || params["toNode_" + it.id] == "on") {
                    finishNodes << it
                }
            }
        }
        if (!event || fromNode == null || finishNodes.isEmpty()) {
            flash.errors = [grailsflowMessageBundleService
                                .getMessage(RESOURCE_BUNDLE, "grailsflow.message.transition.required")]
            render(view: 'transitionForm', model: [transition: new ProcessTransitionDef(fromNode: fromNode, event: event)] )
        } else {

            // Validate parameters
            if (!NameUtils.isValidIdentifier(event)) {
                flash.errors = [grailsflowMessageBundleService
                                    .getMessage(RESOURCE_BUNDLE, "grailsflow.message.event.invalid")]
                return render(view: 'transitionForm', model: [transition: new ProcessTransitionDef(fromNode: fromNode, event: event)] )
            }

            def duplication = ProcessTransitionDef.findWhere(fromNode: fromNode, event: event)
            if (duplication != null && duplication.id.toString() != params.id) {
                flash.errors = [grailsflowMessageBundleService
                                    .getMessage(RESOURCE_BUNDLE, "grailsflow.message.transition.invalid")]
                render(view: 'transitionForm', model: [transition: new ProcessTransitionDef(fromNode: fromNode, event: event)])
            } else {
              def transition
			        if (params.id) {
			            transition = ProcessTransitionDef.get(Long.valueOf(params.id))
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
        def transition = params.id ? ProcessTransitionDef.get(Long.valueOf(params.id)) : null
        if (!transition) {
            flash.errors = ["Impossible to edit transition with key ${params.id}"]
            return redirect(controller: 'processDef', action: 'editTypes')    
        }
        render(view: 'editTranslations', model: [transition: transition])
    }
        
    def saveTranslations = {
        if (!flash.message) flash.message = []
        def transition = params.id ? ProcessTransitionDef.get(Long.valueOf(params.id)) : null
        if (!transition) {
            flash.errors = ["Impossible to edit transition with key ${params.id}"]
            return redirect(controller: 'processDef', action: 'editTypes')    
        }
        def labels = GrailsflowRequestUtils.getTranslationsMapFromParams(params, 'label_')
        transition.label = labels
        transition.save()
        redirect(action: 'editTransitonDef', params: [id: params.id])
    }

    def deleteTransitonDef = {
        def transition = ProcessTransitionDef.get(Long.valueOf(params.id))
        def processDefID = transition.fromNode.processDef.id
        transition.removeFromAssociations()
        transition.delete(flush: true)
        redirect(controller: "processDef", action: 'editProcess', params: [id: processDefID])
    }

    def toProcessEditor = {
      def fromNode = params.fromNode ? ProcessNodeDef.get(Long.valueOf(params.fromNode)) : null
      def processDefID = fromNode?.processDef?.id
      if (processDefID != null) {
        redirect(controller: "processDef", action: 'editProcess', params: [id: processDefID])
      } else {
        redirect(controller: "process", action: 'showTypes')
      }
    }

}

