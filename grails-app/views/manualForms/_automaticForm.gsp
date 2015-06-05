<%@ page import="org.springframework.web.servlet.support.RequestContextUtils; com.jcatalog.grailsflow.utils.TranslationUtils" %>

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
	Default template for manual (of Wait type) node UI. 

	Template parameters:

  required:
		* nodeDetails 		        bean of com.jcatalog.grailsflow.bean.NodeDetails type
		
  NodeDetails has following properties:
	 - properties of ProcessNode class:
          - nodeID                  String node ID
          - caller					String node caller
          - status                  FlowStatus node status
          - startedOn				Date node startedOn
          - dueOn					Date node dueOn
	 - label	       				Map of languageID -> node label
	 - description 					Map of languageID -> node description

	 - assignees					Set<String>	of assignee IDs

	 - events						Set of events beans (see below)

	 - process                      Process bean (see below)

     - variables					Map of variableName -> variable bean (see below)
    
  Process bean has following properties:
    - BasicProcess properties:
      - id
      - type
      - status
      - createdOn
      - createdBy
      - lastModifiedOn
      - lastModifiedBy
      - finishedOn
      - finishedBy
    - label			    		Map of languageID -> variable label
    - description  				Map of languageID -> variable description

  Event bean has following properties
    - event                   String event ID
    - label                   Map of languageID -> event label
    
  Variable bean has following properties:
    - ProcessVariableDef properties:
        - name			   			String variable name
        - type                      String variable type
        - label			    		Map of languageID -> variable label
        - description				Map of languageID -> variable description
        - required					Boolean variable required
        - view						VariableView view of variable
    - value							Object current variable value
    - visibility					int variable visibility for current node

 -->
    <g:set var="isStarted" value="${nodeDetails.process.id != null}"/>

    <div class="row">
      <div class="col-md-12">
        <gf:customizingTemplate template="/manualForms/nodeInfo"
           model="[nodeDetails: nodeDetails]"/>
      </div>
    </div>

    <g:if test="${isStarted}">
      <div class="row">
        <div class="col-md-12">
          <gf:customizingTemplate template="/manualForms/eventForwarding"
              model="[currentAssignees: nodeDetails.assignees]"/>
        </div>
      </div>
    </g:if>

    <div class="row">
      <div class="col-md-12">
        <gf:customizingTemplate template="/manualForms/variablesForm"
            model="[variables: nodeDetails.variables]" />
      </div>
    </div>

    <div class="row">
      <div class="form-submit text-right">
        <g:if test="${params.isEmbedded != 'true'}">
          <g:hiddenField name="backPage" value="${nodeDetails.process.id ? 'showWorklist' : 'showTypes'}" />
          <g:actionSubmit action="returnBack" value="${common['grailsflow.command.back']}" class="btn btn-link"/>
        </g:if>
        <g:set var="app_language" value="${params.lang ? params.lang : RequestContextUtils.getLocale(request)?.language.toString()}" />
          <g:each in="${nodeDetails.events.sort(){ a, b ->
            TranslationUtils.getTranslatedValue(a.label, a.event, app_language)
            .compareToIgnoreCase(TranslationUtils.getTranslatedValue(b.label, b.event, app_language))
            }}"
            var="eventDetails">
            <g:if test="${eventDetails.event != 'overdue'}">
              <g:submitButton name="event_${eventDetails.event}"
                           value="${gf.translatedValue(translations: eventDetails.label, default: eventDetails.event)}"
                           class="btn btn-default"/>
            </g:if>
          </g:each>
    </div>
</div>
