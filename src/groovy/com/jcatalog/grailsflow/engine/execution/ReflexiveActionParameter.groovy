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

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import com.jcatalog.grailsflow.actions.Action

/**
 * ReflexiveParameter is used to update variables from action parameter after action execution
 *
 * @author Maria Voitovich
 */

class ReflexiveActionParameter {
  private static final Log log = LogFactory.getLog(getClass())
  
  private Action action
  private String actionParameterName
  private Object owner
  private String propertyName
  private Object value

  public ReflexiveActionParameter(Object owner, String propertyName) {
    if (owner == null || propertyName == null) {
      throw new Exception("Owner and propertyName must not be null")
    }
    this.owner = owner
    this.propertyName = propertyName
    this.value = owner[propertyName]
  }

  public Object getValue() {
    return this.value
  }

  public void setTargetActionParameter(Action action, String actionParameterName){
    this.action = action
    this.actionParameterName = actionParameterName
  }

  public void updateValue() {
    if (owner == null) return
    if (propertyName == null) return
    if (action == null) return
    if (actionParameterName == null) return

    log.debug("Updating variable ${propertyName} from action parameter ${actionParameterName}")
    owner[propertyName] = action[actionParameterName]
  }

}
