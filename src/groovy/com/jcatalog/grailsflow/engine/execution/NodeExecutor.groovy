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

import com.jcatalog.grailsflow.actions.ActionFactory
import com.jcatalog.grailsflow.actions.ActionContext

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * NodeExecutor executes node's action code section.
 *
 * @author Maria Voitovich
 */

class NodeExecutor implements ApplicationContextAware {
    private static final Log log = LogFactory.getLog(getClass())
    private ApplicationContext applicationContext

    private ActionFactory actionFactory

    public execute(Closure code, ActionContext actionContext) {
      def result = null
      code = code.clone()

      def closureClass = code.class

      def closureMetaClass = new ExpandoMetaClass(closureClass, false)

      def executionContext = new NodeExecutionHelper(actionFactory, actionContext, applicationContext);

      closureMetaClass.invokeMethod = { String name, args ->
         def metaMethod = closureClass.metaClass.getMetaMethod(name, args)
         def methodResult = null

         if(metaMethod) {
           methodResult = metaMethod.invoke(delegate,args)
         } else {
           methodResult = executionContext.invokeProcessMethod(name, args)
         }
         return methodResult
      }

      closureMetaClass.getProperty = { String name ->
        def methodResult = null
        if (name.startsWith("\$")) {
          def innerName = name.substring(1)
          def metaProperty = closureClass.metaClass.getMetaProperty(innerName)
          def owner = metaProperty ? delegate : executionContext.delegate
          methodResult = new ReflexiveActionParameter(owner, innerName)
        } else {
          def metaProperty = closureClass.metaClass.getMetaProperty(name)
          methodResult = metaProperty ? metaProperty.getProperty(delegate) : executionContext.getProcessProperty(name)
        }
        return methodResult
      }


      closureMetaClass.setProperty = { String name, value ->
         def metaProperty = closureClass.metaClass.getMetaProperty(name)
         if(metaProperty) {
           metaProperty.setProperty(delegate, value)
         } else {
           executionContext.setProcessProperty(name, value)
         }
      }
      closureMetaClass.initialize()
      code.metaClass = closureMetaClass
      code.setResolveStrategy(Closure.TO_SELF)
      result = code.call()
      return result
    }

    public void setActionFactory(ActionFactory actionFactory) {
      this.actionFactory = actionFactory
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext
    }


}
