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

<h3><g:message code="plugin.grailsflow.label.listObjectsView"/></h3>
<label for="listObjectsView_displayKey"><g:message code="plugin.grailsflow.label.displayKey"/></label>
<input type="text" id="listObjectsView_displayKey" name="listObjectsView_displayKey" value="${view?.displayKey?.encodeAsHTML()}"/>
<br/>
<label for="listObjectsView_restriction"><g:message code="plugin.grailsflow.label.restriction"/></label>
<input type="text" id="listObjectsView_restriction" name="listObjectsView_restriction" value="${view?.restriction?.encodeAsHTML()}"/>
