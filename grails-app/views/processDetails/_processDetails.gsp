<%--
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--%>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="grailsflow" />
    <g:render plugin="grailsflow" template="/commons/global"/>
    <gf:messageBundle bundle="grailsflow.processDetails" var="msgs"/>
    <title>${msgs['grailsflow.title.processDetails']}</title>
  </head>
  <body>
      <g:form controller="${params['controller']}" method="post">
        <input type="hidden" name="id" value="${processDetails?.id}"/>

        <div class="row">
          <div class="col-md-12">
            <gf:customizingTemplate template="blocks/header" model="[processDetails: processDetails]"/>
          </div>
        </div>

        <div class="row">
          <div class="col-md-12">
            <gf:customizingTemplate template="blocks/processInfo" model="[processDetails: processDetails]"/>
          </div>
        </div>

        <div class="row">
          <div class="col-md-12">
            <gf:customizingTemplate template="blocks/processVariables" model="[variables: processDetails.variables]"/>
          </div>
        </div>

        <div class="row">
          <div class="col-md-12">
            <gf:customizingTemplate template="blocks/processNodes" model="[nodes: processDetails.nodes]"/>
          </div>
        </div>

        <div class="row">
          <div class="col-md-12">
            <gf:customizingTemplate template="blocks/footer" model="[processDetails: processDetails]"/>
          </div>
        </div>

      </g:form>
  </body>
</html>
