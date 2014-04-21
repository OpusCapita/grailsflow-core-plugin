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

package com.jcatalog.grailsflow.model.view

import com.jcatalog.grailsflow.utils.NameUtils;

import com.jcatalog.grailsflow.model.definition.ProcessVariableDef

/**
 * Base class for variable view.
 * All view types should be subclasses of VariableView class
 *
 * @author Maria Voitovich
 */
class VariableView {
  public static String DEFAULT_TEMPLATE = "/variableViewTemplates/defaultView"
  public static String DEFAULT_EDITOR = "/variableViewEditors/defaultView"
  public static String VIEW_PACKAGE = VariableView.class.package.name

  static belongsTo = [ variable: ProcessVariableDef ]

  static transients = ["type", "template"]

  // For using on the UI
  String getTemplate () {
    return "/variableViewTemplates/${getType()}"
  }

  // Introduces because GORM does not support property for discriminator
  String getType () {
    return NameUtils.downCase(this.class.simpleName)
  }

  // Introduces because GORM does not worked correctly with variable.variableView.properties = view.proeprties
  void mergeChanges(VariableView view) {
    this.properties = view.properties
  }

  static mapping = {
   tablePerHierarchy false
  }

  // For using on the UI
  static def getEditorForViewType(String viewType) {
    return "/variableViewEditors/${viewType}".toString()
  }

  static String getDefaultTemplateForType(String variableType) {
      String defaultView
      switch (variableType) {
          case ["Date", "Document", "Link"]:
              defaultView = NameUtils.downCase("${variableType}View")
              break;
          case "Boolean":
              defaultView = NameUtils.downCase(CheckBoxView.class.simpleName)
              break;
          case "List":
              defaultView = NameUtils.downCase(ItemsView.class.simpleName)
              break;
          default :
              defaultView = NameUtils.downCase(SimpleView.class.simpleName)
      }
      return "/variableViewTemplates/${defaultView}".toString()
  }


    static def getViewClassFromViewType(String viewType){
      if (!viewType) {
        return null
      }
      def viewClassName = NameUtils.upCase(viewType)
      try {
        def viewClass = new VariableView().domainClass.grailsApplication
            .getClassForName("${VIEW_PACKAGE}.${viewClassName}")
        return viewClass
      } catch (Exception e) {
      }
      return null;
  }
  
  static def getSupportedViewTypes() {
    def viewTypes = []
    new VariableView().domainClass?.grailsApplication?.domainClasses?.each() { domainClazz ->
      try {
        def obj = domainClazz.newInstance()
        if (obj instanceof VariableView) {
          viewTypes << obj.getType()
        }
	  } catch (Exception e) {}
    }
    viewTypes.remove("variableView")
    return viewTypes.isEmpty() ? [] : viewTypes.sort() { it ? it.toString() : "" }
  }
}
