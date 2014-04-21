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
  Template for displaying ProcessVariables on the ProcessDetails UI

  Template parameters:

  required:
    * variables               List of variables objects

 -->
<gf:messageBundle bundle="grailsflow.processVariableEditor" var="msgs"/>
<gf:section title="${msgs['grailsflow.label.processVars']}" selected="false">
 <table class="standard" width="100%">
     <thead>
       <th>${msgs['grailsflow.label.name']}</th>
       <th>${msgs['grailsflow.label.value']}</th>
     </thead>
     <tbody>
     <g:each in="${variables}">
       <tr>
         <td><gf:translatedValue translations="${it.label}" default="${it.name}"/></td>
         <td>
           <g:if test="${it.type && it.type == 'Document'}">
             <gf:renderDocument document="${it.value}"/>
           </g:if>
           <g:elseif test="${it.type && it.type == 'Link'}">
             <gf:renderLink link="${it.value}" />
           </g:elseif>
           <g:elseif test="${it.type && it.type == 'Date'}">
             <gf:displayDate value="${it?.value}"/>
           </g:elseif>
           <g:elseif test="${it.type && it.type == 'Double'}">
             <gf:displayDouble value="${it.value}"/>
           </g:elseif>
           <g:else>   <!-- Default displaying -->
             <g:if test="${com.jcatalog.grailsflow.model.process.ProcessVariable.isValueIdentifier(it.type)}">
               ${it.variableValue}
             </g:if>
             <g:else>
               ${it.value ? org.apache.commons.lang.StringEscapeUtils.escapeHtml(it.value.toString()) : ''}
             </g:else>
           </g:else>
         </td>
       </tr>
     </g:each>
     </tbody>
 </table>
</gf:section>
<br/><br/>
