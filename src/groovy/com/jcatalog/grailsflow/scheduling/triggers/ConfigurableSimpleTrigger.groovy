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
 
package com.jcatalog.grailsflow.scheduling.triggers

import org.quartz.SimpleTrigger

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.quartz.impl.triggers.SimpleTriggerImpl

/**
 * ConfigurableSimpleTrigger is quartz SimpleTrigger that gets it's initial
 * configuration from Grails application configuration
 *
 * grailsflow.scheduler.<triggerName>.<triggerProperty>=<value>
 * where trigger properties are properties of org.quartz.SimpleTrigger
 *
 * @author Maria Voitovich
 */
class ConfigurableSimpleTrigger extends SimpleTriggerImpl {
    protected final Log log = LogFactory.getLog(getClass())
    static final String START_DELAY = "startDelay"
    static final String REPEAT_COUNT = "repeatCount"
    static final String REPEAT_INTERVAL = "repeatInterval"
    
    static final long DEFAULT_START_DELAY = 30000l  // 30 seconds
    static final long DEFAULT_REPEAT_INTERVAL = 60000l  // one minute
    static final int DEFAULT_REPEAT_COUNT = SimpleTrigger.REPEAT_INDEFINITELY  // forever

    /**
     * Overwrite org.quartz.Trigger.setName()
     * to read other properties from Grails application configuration
     *
     */
    void setName(String name) {
        super.setName(name)
        initTriggerProperties()
    }
    
    /**
     * Read trigger properties from Grails application configuration
     *
     */
    private void initTriggerProperties() {
      def config = this.getGrailsApplication()?.config?.grailsflow?.scheduler?.get(getName())
      if (config instanceof ConfigObject) {
        Map properties = config.flatten()
        if (properties) {
          log.debug("Setting properties ${properties} for the trigger ${getName()}")
          
          // start delay
          if(properties.containsKey(START_DELAY)) {
              Number startDelay = (Number) properties.remove(START_DELAY);
              this.setStartTime(new Date(System.currentTimeMillis() + startDelay.longValue()))
          } else {
              this.setStartTime(new Date(System.currentTimeMillis() + DEFAULT_START_DELAY))
          }
          
          // repeat interval
          if (!properties.containsKey(REPEAT_INTERVAL)) {
            this.setRepeatInterval(DEFAULT_REPEAT_INTERVAL)
          } else {
            this.setRepeatInterval((Number) properties.remove(REPEAT_INTERVAL))
          }
          
          // repeat count
          if (!properties.containsKey(REPEAT_COUNT)) {
            this.setRepeatCount(DEFAULT_REPEAT_COUNT)
          } else {
            this.setRepeatCount((Number) properties.remove(REPEAT_COUNT))
          }
          
        }
      }
    }

 }