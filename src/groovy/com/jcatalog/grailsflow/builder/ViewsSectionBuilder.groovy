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

package com.jcatalog.grailsflow.builder;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import com.jcatalog.grailsflow.utils.NameUtils
import com.jcatalog.grailsflow.model.view.VariableView

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * ViewsSectionBuilder class is used to construct VariableView objects from views closure of process definition file.
 * Order of variables in views section specifies order of variables on the UI 
 *
 * The result of ViewsSectionBuilder work is:
 *   - views: Map of variableName -> variableView.
 *   - order: Map of variableName -> variable order.
 *
 *
 * @author Maria Voitovich
 */
class ViewsSectionBuilder extends AbstractSectionBuilder {
    private def views
    private def order
    private int index
    
    public def getViews(){
      return views
    }

    public def getOrder(){
      return order
    }

    public List<String> getSupportedSections() {
      return [ "views" ]
    }
    
    public ViewsSectionBuilder(def process) {
        views = [:]
        order = [:]
        index = 0
        build(process)
    }
    
    private createView(String name, Map properties) {
        def viewClass = VariableView.getViewClassFromViewType(name)
        if (!viewClass) {
          log.debug("Cannot get view class for view type ${name}")
          return null;
        }
        try {
          def view = viewClass.newInstance()
          view.properties = properties
          return view
        } catch (Exception e) {
          log.debug("Error occurred while building view object of type ${viewClass.name}")
        }
        return null
    }

    protected Object createNode(Object name, Map attributes) {
        log.debug("Building view of type ${name} from attributes ${attributes}")
        def view = createView(name.toString(), attributes)
        if (!view) {
          log.error("Cannot build view of type ${name}")
          return null;
        }
        return view
    }

    protected Object createNode(Object name, Map attributes, Object value) {
      log.warn("Incorrect DSL syntax at node ${name} with attributes ${attributes} and value ${value.inspect()}")
      return null
    }

    protected void setParent(Object parent, Object child) {
      log.warn("Set parent for ${parent} and child ${child} should not occur for ${this.class.name}")
    }

    protected Object createNode(Object name) {
      log.debug("Parsing empty node with name ${name}")
      log.debug("Trying to create view from empty node ${name}")
      def view = createView(name, (Map)[:])
      if (!view) {
        log.debug("Assuming empty node ${name} is variable node")
        return createNode(name, (Object)null)
      } else {
        return view;
      }
    }

    protected Object createNode(Object name, Object value){
      log.debug("Setting view of type ${value?.type} for variable ${name}")
      views.put("${name}".toString(), value)
      log.debug("Setting orderNo ${index} for variable ${name}")
      order.put("${name}".toString(), index++)
      return null
    }

}
