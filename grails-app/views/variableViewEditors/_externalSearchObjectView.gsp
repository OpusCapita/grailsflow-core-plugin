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

<h3><g:message code="plugin.grailsflow.label.externalSearchObjectView"/></h3>
<label for="externalSearchObjectView_displayKey"><g:message code="plugin.grailsflow.label.displayKey"/></label>
<input type="text" id="externalSearchObjectView_displayKey" class="form-control" name="externalSearchObjectView_displayKey" value="${view?.displayKey?.encodeAsHTML()}"/>
<br/>
<label for="externalSearchObjectView_searchUrl"><g:message code="plugin.grailsflow.label.searchUrl"/></label>
<input type="text" id="externalSearchObjectView_searchUrl" class="form-control" name="externalSearchObjectView_searchUrl" value="${view?.searchUrl?.encodeAsHTML()}"/>
<br/>
<label for="externalSearchObjectView_additionalFields"><g:message code="plugin.grailsflow.label.additionalFields"/></label>
<input type="text" id="externalSearchObjectView_additionalFields" class="form-control" name="externalSearchObjectView_additionalFields" value="${view?.additionalFields?.encodeAsHTML()}"/>
