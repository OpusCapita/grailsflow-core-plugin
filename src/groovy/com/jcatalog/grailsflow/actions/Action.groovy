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
 
package com.jcatalog.grailsflow.actions

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext

/**
 * An Action abstract class represents action entity that can be
 * executed during process flow. Action Builder knows how to deal
 * with Actions. The class contains common methods and variables
 * for implemented actions.
 * All Actions that are available for process/node definition
 * should extend Action class and implements its abstract
 * methods.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
abstract class Action {
    protected static Log log = LogFactory.getLog(Action.class)

    ApplicationContext appContext
    ActionContext actionContext

    // every action can be executed by calling 'execute' method
    abstract Object execute()

    def getObjectByName(String beanName) {
        if (!appContext) return null;
        
        def bean = null
        try {
            bean = appContext.getBean(beanName)
        } catch (BeansException be) {
            log.error("BeanException has been occurred in getting bean $beanName", be)
        } catch (Exception e) {
            log.error("Exception $e.message has been occurred in getting bean $beanName", e)
        }
        return bean
    }
}