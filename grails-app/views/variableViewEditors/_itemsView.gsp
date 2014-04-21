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

<h3>${msgs['grailsflow.label.itemsView']}</h3>
<label for="itemsView_styleClass">${msgs['grailsflow.label.styleClass']}</label>
<input type="text" id="itemsView_styleClass" name="itemsView_styleClass" value="${view?.styleClass?.encodeAsHTML()}"/>
<br/>
