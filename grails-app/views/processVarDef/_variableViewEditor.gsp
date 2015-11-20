<!--
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!--
	Template for displaying editor for VariableDef.view 

	Template parameters:

  required:	
		* variable 						 VariableDef instance

 -->

<r:script>
  function changeViewType() {
    var typeElement = document.getElementById('variableViewType')
    var selectedType = typeElement.value

    for (i = 0; i< typeElement.options.length; ++i) {
      var type = typeElement.options[i].value
      if (type != "") {
	      var divID = type+"Div"
	      document.getElementById(divID).style.display = selectedType == type ? '' : 'none'
	    }
    }
  }
</r:script>

<g:set var="supportedViewTypes" value="${com.jcatalog.grailsflow.model.view.VariableView.supportedViewTypes}"/>
<g:set var="view" value="${variable?.view}"/>
<g:set var="viewType" value="${view?.type ? view?.type : params.variableViewType}"/>

<label for="variableViewType"><g:message code="plugin.grailsflow.label.type"/></label>
<g:select id="variableViewType" name="variableViewType" value="${viewType}" from="${supportedViewTypes}" noSelection="${['' : '']}" class="form-control" onchange="return changeViewType();"></g:select>

<g:each var="type" in="${supportedViewTypes}">
  <div id="${type}Div" ${type == viewType ? '' : 'style="display: none"'}>
	  <gf:customizingTemplate template="${com.jcatalog.grailsflow.model.view.VariableView.getEditorForViewType(type)}" defaultTemplate="${com.jcatalog.grailsflow.model.view.VariableView.DEFAULT_EDITOR}"
	     model="[viewType: type, view: (type == viewType ? view : null) ]"/>
  </div>
</g:each>
