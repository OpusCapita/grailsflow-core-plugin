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
	Template for displaying processVariable input. 

	Template parameters:

	required:	
    	* variable  			variable bean object.
		* view                  VariableView object. Must be not null and of corresponding type.
	
	optional:
		* parameterName					'name' attribute for variable input. Default is empty.
 -->

    <g:set var="readOnly" value="${variable.visibility == com.jcatalog.grailsflow.utils.ConstantUtils.READ_ONLY}"/>
    <g:set var="required" value="${variable.required != null ? variable.required : false}"/>

    <!-- prevent from submiting readonly parameter -->
    <g:set var="parameterName" value="${readOnly ? '' : parameterName}"/>
    <g:set var="size" value="${view?.size ? view?.size : 20}"/>
    <g:set var="styleClass" value="${readOnly ? 'readonly' : view?.styleClass}"/>

    <!-- Defining value to display -->
    <g:if test="${com.jcatalog.grailsflow.model.process.ProcessVariable.isValueIdentifier(variable.type)}"><!-- Domain object -->
      <g:set var="displayValue" value="${variable.value?.ident()}"/>
    </g:if>
    <g:elseif test="${variable.type == 'Double'}">
      <g:set var="displayValue" value="${gf.displayDouble(value: variable.value)}"/>
    </g:elseif>
    <g:else>
      <g:set var="displayValue" value="${variable.value}"/>
    </g:else>
   	<input name="${parameterName}" id="var_${variable.name}" value="${displayValue?.encodeAsHTML()}" size="${size}" maxlength="2000" class="form-control" ${readOnly ? 'readonly="true"' : ''}/>
