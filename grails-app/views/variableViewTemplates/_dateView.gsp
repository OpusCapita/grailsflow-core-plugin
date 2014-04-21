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
		* variable              variable bean object.
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
    <g:set var="dateFormat" value="${view?.dateFormat ? view?.dateFormat : gf.datePattern().toString()}"/>

    <r:require modules="grailsflowCalendar" />

    <g:if test="${readOnly}">
	  <input value="${gf.displayDate(pattern: dateFormat, value: variable.value)}" size="${size}" class="${styleClass}"/>
	</g:if>
	<g:else>
      <gf:jQueryCalendar property="${parameterName}" value="${variable.value}" pattern="${dateFormat}"
                         size="${size}" class="${styleClass}"/>
	</g:else>
