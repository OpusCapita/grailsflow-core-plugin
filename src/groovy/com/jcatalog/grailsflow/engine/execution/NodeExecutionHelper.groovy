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

package com.jcatalog.grailsflow.engine.execution;

import com.jcatalog.grailsflow.actions.ActionContext
import com.jcatalog.grailsflow.actions.ActionFactory
import com.jcatalog.grailsflow.actions.ActionUtils
import com.jcatalog.grailsflow.utils.ClassUtils
import com.jcatalog.grailsflow.actions.Action

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.springframework.context.ApplicationContext
import org.codehaus.groovy.runtime.InvokerHelper

/**
 * An NodeExecutionHelper used to resolve variables and actions during node actions execution
 *
 * @author Maria Voitovich
 */

class NodeExecutionHelper extends GroovyObjectSupport {
    private static final Log log = LogFactory.getLog(getClass())
    private ApplicationContext applicationContext
    private ActionFactory actionFactory
    private ActionContext actionContext

    public NodeExecutionHelper(ActionFactory actionFactory, ActionContext actionContext, def applicationContext) {
        this.actionFactory = actionFactory
        this.actionContext = actionContext
        this.applicationContext = applicationContext
    }

    public Object getDelegate() {
        return actionContext?.variables
    }

    public Object getProcessProperty(String propertyName) {
        def value = null
        if (propertyName == "actionContext") {
          return actionContext
        } else if (actionContext?.variables?.keySet()?.contains(propertyName)) {
          value = actionContext?.variables[propertyName]
        } else {
          throw new MissingPropertyException("Variable ${propertyName} not found")
        }
        return value
    }

    public void setProcessProperty(String propertyName, Object newValue) {
      if (actionContext?.variables?.keySet()?.contains(propertyName)) {
        actionContext?.variables[propertyName] = newValue
      } else {
        throw new MissingPropertyException("Variable ${propertyName} not found")
      }
    }

    public Object invokeProcessMethod(String name, Object args) {
      List list = InvokerHelper.asList(args);
      log.debug("Calling method ${name} with arguments ${list?.inspect()}")
      Map arguments
      switch (list.size()) {
        case 0:
          arguments = [:]
          break;
        case 1:
          arguments = list.get(0);
          break;
        default:
          throw new Exception("Invalid arguments")
          break;
      }

      def action = getActionInstance(name)
      if (action != null) {
        Collection<ReflexiveActionParameter> reflexiveActionParameters = prepareActionParameters(action, arguments)
        def result = action.execute()
        reflexiveActionParameters.each() {
          it.updateValue()
        }
        return result
      } else {
        throw new MissingPropertyException("Action ${name} not found")
      }
    }

    private def getActionInstance(String name) {
      def actionClass = actionFactory.getActionClassForName(name)
      if (actionClass != null) {
        def action
        try {
            action = actionClass.newInstance()
            return action
        } catch(Throwable e) {
            log.error("Could not instantiate Action ${name}", e)
            return null
        }
      }
      return null
    }

    private Collection<ReflexiveActionParameter> prepareActionParameters(Action action, def attributes) {
      Collection<ReflexiveActionParameter> reflexiveActionParameters = new ArrayList<ReflexiveActionParameter>()
      action.appContext = applicationContext
      action.actionContext = actionContext

      def propertiesNames = ClassUtils.getActionClassProperties(action.getClass())

      // copy values from arguments to class variables
      attributes?.keySet()?.each() { name ->
          if (propertiesNames.contains(name)) {
              def value = attributes[name]
              def type = action.getClass().getField(name).type
              if (value != null) {
                if (value instanceof ReflexiveActionParameter) {
                  value.setTargetActionParameter(action, name)
                  reflexiveActionParameters << value
                  value = value.value
                }
                if (value!= null && type != null && !type.isAssignableFrom(value.getClass())) {
                  value = getParameterValue(type, value)
                  log.debug("Converting value ${value} of type ${value.getClass()} to type ${type}")
                }
              }
              log.debug("Setting value ${value} of type ${type} to action property ${name}")
              action[name] = value
          } else {
            log.warn("No such property ${name} for action class ${action.getClass().simpleName}. Value skipped")
          }
      }
      return reflexiveActionParameters;
    }

    private def getParameterValue(Class type, def value) {
      if (type == Object.class) {
        return value
      } else {
        return ActionUtils.getValueOfType(type.simpleName, value.toString())
      }
    }


}
