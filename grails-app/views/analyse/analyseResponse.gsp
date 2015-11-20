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

<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="grailsflow" />
         <g:render plugin="grailsflow" template="/commons/global"/>

         <r:require modules="grailsflowJgplot"/>

         <r:script>
             var data = evalJson('${processTypeProtocol}')

             if (data && data.length > 0) {
               var xCoordinates = []
               var lines  = []
               var series = []

               for (var i=0; i< data.length; i++){
                 var line = []
                 var yCoordinates = []
                 var process = data[i]["processId"]
                 var protocolGroups = data[i]["protocolGroups"]
                 for (var group in protocolGroups){
                   var nodes = protocolGroups[group]
                   if (i == 0) xCoordinates.push(group)
                   var totalExecutionTime = 0
                   for (var j=0; j< nodes.length; j++){
                     totalExecutionTime = totalExecutionTime + parseInt(nodes[j]["executionTime"])
                   }
                   yCoordinates[group] = totalExecutionTime
                 }

                 for (var j=0; j< xCoordinates.length; j++) {
                   var protocolGroup = xCoordinates[j]
                   line.push([protocolGroup, yCoordinates[protocolGroup]])
                 }

                 series.push({color: get_random_color(), label: "${g.message(code: 'plugin.grailsflow.label.processID')} = "+process})
                 lines.push(line)
               }

               jQuery.noConflict();

               jQuery(document).ready(function($){
                 jQuery.jqplot.config.enablePlugins = true;
                 jQuery.jqplot("graphic", lines,
                           { title: '',
                             axes: {
                                 yaxis: { label: "${g.message(code:'plugin.grailsflow.label.executionTime')}",
                                          labelOptions: {
                                               enableFontSupport: true,
                                               fontFamily: 'Tahoma',
                                               fontSize: '13px',
                                               textColor: '#002276'
                                           },  autoscale:true, min: 0 },
                                 xaxis: { label: "${g.message(code:'plugin.grailsflow.label.protocolGroups')}",
                                          labelOptions: {
                                               enableFontSupport: true,
                                               fontFamily: 'Tahoma',
                                               fontSize: '13px',
                                               textColor: '#002276'
                                           },
                                 renderer: jQuery.jqplot.CategoryAxisRenderer }
                             },
                             series: series,
                             legend: {
                                 renderer: jQuery.jqplot.EnhancedLegendRenderer,
                                 show: true,
                                 location:'ne',
                                 placement: 'outsideGrid',
                                 shrinkGrid: true,
                             }

                           }
                          );

               });

             }

             function evalJson(json) {
               try {
                 return eval("(" + json + ")");
               } catch (e) {  return null;}
             }

             function get_random_color() {
               var color = Math.floor(Math.random() * 16777216).toString(16);
               return '#000000'.slice(0, -color.length) + color;
             }

         </r:script>
         <style>
            table.jqplot-table-legend {
                display: block;
                height: 300px;
                overflow-y: scroll;
            }
         </style>
         <title><g:message code="plugin.grailsflow.title.analyseResponseTime"/></title>
    </head>
    <body>
      <h1><g:message code="plugin.grailsflow.label.analyseResponseTime"/></h1>

      <g:form class="form-horizontal" controller="${params['controller']}" method="GET">
        <div class="row">
          <div class="col-md-6">
            <div class="form-group">
              <label class="col-md-4 control-label" for="type">
                <g:message code="plugin.grailsflow.label.processType"/>
              </label>
              <div class="col-md-8">
                  <g:select from="${processClasses}" name='type' id="type" class="form-control"
                            optionKey="${{it.processType}}" optionValue="${{gf.translatedValue(translations: it.label, default: it.processType)}}"
                            noSelection="['':'']" value="${params.type}"></g:select>
              </div>
            </div>
            <div class="form-group">
              <label class="col-md-4 control-label">
                <g:message code="plugin.grailsflow.label.sortBy"/>
              </label>
              <div class="col-md-8">
                <g:select name='sortBy' value="${params.sortBy}" noSelection="['':'']" class="form-control"
                          from="${[ AnalyseController.SORT_BY_NODE, AnalyseController.SORT_BY_MIN_TIME,
                                    AnalyseController.SORT_BY_MAX_TIME, AnalyseController.SORT_BY_AVERAGE_TIME,
                                    AnalyseController.SORT_BY_INTERACTIVE_NODE, AnalyseController.SORT_BY_NONINTERACTIVE_NODE]}"
                            optionValue="${{ g.message(code: 'plugin.grailsflow.label.'+it) ?: ''}}" />
              </div>
            </div>
            <div class="form-group">
              <div class="form-submit text-right">
                <g:actionSubmit action="searchNodesInfo" value="${g.message(code:'plugin.grailsflow.command.search')}" class="btn btn-primary"/>
              </div>
            </div>
          </div>
        </div>

        <div class="row">
          <div class="col-md-12">
            <h1><g:message code="plugin.grailsflow.label.protocolingNodes"/></h1>
            <g:if test="${processTypeProtocol}">
              <div class="jqplot-target" id="graphic" style="width:800px; height: 300px; position: relative;"></div>
            </g:if>
            <g:else>
              <div class="bs-callout bs-callout-info"><g:message code="plugin.grailsflow.label.noProtocolGroups"/></div>
            </g:else>

          </div>
        </div>

        <div class="row">
          <div class="col-md-12">
            <h1>${params.type?.encodeAsHTML()} &nbsp;<g:message code="plugin.grailsflow.label.processList"/></h1>
            <g:if test="${processNodes}">
              <table class="table">
                <thead>
                  <tr><th><g:message code="plugin.grailsflow.label.nodeID"/></th>
                      <th><g:message code="plugin.grailsflow.label.quantity"/></th>
                      <th><g:message code="plugin.grailsflow.label.minTime"/></th>
                      <th><g:message code="plugin.grailsflow.label.maxTime"/></th>
                      <th><g:message code="plugin.grailsflow.label.averageTime"/></th>
                      <th><g:message code="plugin.grailsflow.label.type"/></th>
                  </tr>
                </thead>
                <tbody>
                  <g:each in="${processNodes}" var="nodeInfo">
                    <tr>
                      <td><gf:translatedValue translations="${nodeInfo?.label}" default="${nodeInfo?.nodeID}"/></td>
                      <td>${nodeInfo?.quantity}</td>
                      <td><gf:displayDouble value="${nodeInfo?.minTime/1000}"/>&nbsp;(<g:message code="plugin.grailsflow.label.processID"/>=${nodeInfo?.processMinTime})</td>
                      <td><gf:displayDouble value="${nodeInfo?.maxTime/1000}"/>&nbsp;(<g:message code="plugin.grailsflow.label.processID"/>=${nodeInfo?.processMaxTime})</td>
                      <td><gf:displayDouble value="${nodeInfo?.averageTime/1000}"/></td>
                      <td>${nodeInfo?.type}</td>
                    </tr>
                  </g:each>
                </tbody>
              </table>
            </g:if>
            <g:else>
              <div class="bs-callout bs-callout-info"><g:message code="plugin.grailsflow.message.noItems"/></div>
            </g:else>
          </div>
        </div>
      </g:form>
    </body>
</html>
