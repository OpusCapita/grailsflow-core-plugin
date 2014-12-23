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

   Template renders footer for NodeDetails UI

-->

<gf:messageBundle bundle="grailsflow.common" var="common"/>

<g:if test="${params.isEmbedded != 'true'}">
 <g:form controller="${params['controller']}" method="POST">
   <div class="buttons">
      <g:hiddenField name="backPage" value="${nodeDetails.process.id ? 'showWorklist' : 'showTypes'}" />
      <span class="button">
        <g:actionSubmit action="returnBack" value="${common['grailsflow.command.back']}" class="btn btn-link"/>
      </span>
   </div>
 </g:form>
</g:if>
            
