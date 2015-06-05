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
  	* viewType  						View type. Cannot be null.
		* view 						 VariableView instance of corresponding type. Can be null.

  development note:
    - parameter names for properties should have "<viewType>_<viewPropertyName>" format 

 -->

<gf:messageBundle bundle="grailsflow.variableView" var="msgs"/>

<g:set var="labelKey" value="label.${viewType}"/>
<h3>${msgs['grailsflow.'+labelKey]}</h3>

<g:set var="viewClass" value="${com.jcatalog.grailsflow.model.view.VariableView.getViewClassFromViewType(viewType)}"/>

<g:if test="${viewClass}">
  <g:each var="propertyName" in="${com.jcatalog.grailsflow.utils.ClassUtils.getDomainClassProperties(viewClass)}">
    <g:set var="name" value="${viewType + '_' +propertyName}"/>
    <g:set var="labelKey" value="label.${propertyName}"/>
	  <label for="${name}">${msgs['grailsflow.'+labelKey]}</label>
	  <input type="text" id="${name}" name="${name}" class="form-control" value="${(view ? view[name] : '')?.encodeAsHTML()}"/>
	  <br/>
  </g:each>
</g:if>
<g:else>
  Couldn't get view class for view type '${viewType}' 
</g:else>