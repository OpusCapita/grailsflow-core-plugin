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
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=10">
    <g:render plugin="grailsflow" template="/commons/global"/>
    <g:if test="${params.isEmbedded == 'true'}">
      <meta name="layout" content="" />
      <r:layoutResources/>
    </g:if>
    <g:else>
      <meta name="layout" content="grailsflow" />
    </g:else>
    <gf:messageBundle bundle="grailsflow.common" var="common"/>
    <gf:messageBundle bundle="grailsflow.worklist" var="worklist"/>
    <title>${worklist['grailsflow.title.worklist']}</title>

    <r:script>
      var variableValuesMap = {};
      <g:each var="name" in="${variableValues?.keySet()}">
        variableValuesMap["${name}"] = new Array();
        <g:each var="value" status="i" in="${variableValues[name]}">
          variableValuesMap["${name}"][${i}] = new Option("${value.value}", "${value.variableValue}");
        </g:each>
      </g:each>

      function clearOptions(selectElement) {
        for (var i = selectElement.options.length; i>0;  --i ){
          selectElement.remove(i-1);
        }
      }

      function changeFilterVariable() {
        var name = document.getElementById('filterVariable').value;
        var valueSelect = document.getElementById('filterVariableValue');

        valueSelect.value = "";
        clearOptions(valueSelect);
        if (name == "") {
          var option = new Option("${worklist['grailsflow.label.emptyFilterVariable']}", "");
          addOption(valueSelect, option);
          valueSelect.disabled = true;
          return;
        } else {
          if (variableValuesMap[name].length > 0) {
            addOption(valueSelect, new Option("", ""));
            valueSelect.disabled = false;
	        for (var i=0; i < variableValuesMap[name].length; ++i ){
	          var protoOption = variableValuesMap[name][i];
	          var option = new Option(protoOption.text, protoOption.value);
	          addOption(valueSelect, option);
	        }
            return;
          } else {
            addOption(valueSelect, new Option("${worklist['grailsflow.label.emptyFilterVariableValue']}", ""));
            valueSelect.disabled = true;
            return;
          }
        }
      }

    </r:script>
  </head>

  <body>
    <h1>${worklist['grailsflow.title.worklist']}</h1>
    <g:render plugin="grailsflow" template="/commons/messageInfo"/>

    <g:form controller="${params['controller']}" method="POST">
      <g:if test="${params.isEmbedded}">
        <input type="hidden" name="isEmbedded" value="${params.isEmbedded}"/>
      </g:if>
      <g:set var="varsFilterString" value="${['isEmbedded': params.isEmbedded]}"/>

      <g:if test="${isFilterAvailable}">
        <g:each var="parameter" in="${varsFilter?.keySet()}">
          <g:if test="${varsFilter[parameter] instanceof List}">
            <g:hiddenField name="varsFilter.${parameter}.list" value="${varsFilter[parameter].inspect()}"/>
            <g:set var="key" value="${'varsFilter.'+parameter+'.list'}" />
            <g:set var="varsFilterString" value="${varsFilterString + [key : varsFilter[parameter].inspect()]}"/>
          </g:if>
          <g:else>
            <g:hiddenField name="varsFilter.${parameter}" value="${varsFilter[parameter]}"/>
            <g:set var="key" value="${'varsFilter.'+parameter}" />
            <g:set var="varsFilterString" value="${varsFilterString + [key : varsFilter[parameter]]}"/>
          </g:else>
        </g:each>
        <g:set var="varsFilterString" value="${varsFilterString + ['filterVariable' : filterVariable]}"/>
        <g:set var="varsFilterString" value="${varsFilterString + ['filterVariableValue' : filterVariableValue]}"/>

        <g:if test="${additionalColumns}">

          <div class="row">
            <div class="col-md-4">
              ${worklist['grailsflow.label.filterVariable']}&nbsp;
              <g:select id="filterVariable" name="filterVariable" value="${filterVariable}" noSelection="['':'']"
                          from="${additionalColumns.keySet()}" onchange = "changeFilterVariable();"
                          optionValue="${{gf.translatedValue(translations: additionalColumns[it], default: it)}}">
              </g:select>
            </div>
            <div class="col-md-3">
              ${worklist['grailsflow.label.filterVariableValue']}&nbsp;
              <select id="filterVariableValue" name="filterVariableValue" value="${filterVariableValue}"
                    ${filterVariable && variableValues?.get(filterVariable)?.size() ? '' : 'disabled="true"'}>
                    <g:if test="${! filterVariable}">
                        <option value="" selected="true">${worklist['grailsflow.label.emptyFilterVariable']}</option>
                    </g:if>
                    <g:else>
                        <g:if test="${variableValues?.get(filterVariable)?.size()}">
                            <option value="" selected="${! filterVariableValue}"></option>
                            <g:each var="value" in="${variableValues[filterVariable]}">
                                <option value="${value.value}" ${value.value == filterVariableValue ? 'selected="true"' : ''}>
                                    ${value.variableValue}
                                </option>
                            </g:each>
                        </g:if>
                        <g:else>
                            <option value="" selected="true">${worklist['grailsflow.label.emptyFilterVariableValue']}</option>
                        </g:else>
                    </g:else>
              </select>
            </div>
            <div class="col-md-5">
              <g:actionSubmit value="${common['grailsflow.command.filter']}" action="showWorklist" class="btn btn-primary"/>
            </div>
          </div>
        </g:if>
      </g:if>

      <g:if test="${processNodeList}">
      <div class="row margin-top-10">
        <div class="col-md-12">
          <table width="100%" class="table table-striped table-bordered">
            <thead>
              <tr>
                  <gf:sortableColumn property="nodeLabel"
                      defaultOrder="desc" controller="${params['controller']}"
                      title="${worklist['grailsflow.label.nodeID']}"
                      action="showWorklist" params="${varsFilterString}"/>
                  <th>${worklist['grailsflow.label.externalUrl']}</th>
                  <gf:sortableColumn property="processTypeLabel" defaultOrder="desc" controller="${params['controller']}"
                      title="${worklist['grailsflow.label.processType']}"
                      action="showWorklist" params="${varsFilterString}"/>
                  <th>${worklist['grailsflow.label.description']}</th>

                  <g:if test="${additionalColumns}">
                    <g:each var="column" in="${additionalColumns.keySet()}">
                      <gf:sortableColumn property="vars.${column}" defaultOrder="desc" controller="${params['controller']}"
                          title="${gf.translatedValue(translations: additionalColumns[column], default: column)}"
                          action="showWorklist" params="${varsFilterString}"/>
                    </g:each>
                  </g:if>
                  <th>${worklist['grailsflow.label.caller']}</th>
                  <gf:sortableColumn property="startedOn" title="${worklist['grailsflow.label.startedOn']}" defaultOrder="desc"
                      controller="${params['controller']}" action="showWorklist"/>
                  <gf:sortableColumn property="dueOn" title="${worklist['grailsflow.label.dueOn']}" defaultOrder="desc"
                      controller="${params['controller']}" action="showWorklist"/>
              </tr>
            </thead>
            <tbody>
              <g:each in="${processNodeList}" var="node">
                <tr>
                  <td>
                    <g:if test="${node.type == com.jcatalog.grailsflow.utils.ConstantUtils.NODE_TYPE_WAIT}">
                      <g:set var="detailsParams" value="${params.isEmbedded ? [isEmbedded: params.isEmbedded] : [:]}"/>
                      <g:link id="${node.id}" controller="${params['controller']}"
                            params="${detailsParams}" action="showNodeDetails">
                        <gf:translatedValue translations="${node.label}" default="${node.nodeID}"/>
                      </g:link>
                    </g:if>
                    <g:else>
                      <gf:translatedValue translations="${node.label}" default="${node.nodeID}"/>
                    </g:else>
                  </td>
                  <td>
                    <gf:generateExternalUrl processNodeId="${node.id}" action="openExternalUrl"
                        controller="process" label="${worklist['grailsflow.message.externalUrl']}"/>
                  </td>
                  <td>
                    <gf:translatedValue translations="${node.process.label}" default="${node.process.type}"/>
                  </td>
                  <td><gf:translatedValue translations="${node.description}" default=""/></td>
                    <g:if test="${additionalColumns}">
                      <g:each var="column" in="${additionalColumns.keySet()}">
                        <td>${node.variables[column]?.value?.toString()}</td>
                      </g:each>
                    </g:if>
                  <td>${node.caller}</td>
                  <td><gf:displayDateTime value="${node.startedOn}"/></td>
                  <td><gf:displayDateTime value="${node.dueOn}"/></td>
                </tr>
              </g:each>
            </tbody>
          </table>

            <div class="paginateButtons">
              <g:paginate total="${itemsTotal}" id="${params.id}" params="${params}"
                controller="${params['controller']}" action="showWorklist"/>
            </div>
            <br/>
            <r:script>
              function reloadPage() {
                window.location = "${g.createLink(controller: params['controller'], action: params['action'], params: params)}";
              }
            </r:script>

            <div class="buttons">
              <span class="button">
                <input type="button" onclick="reloadPage();" value="${common['grailsflow.command.refresh']}" class="btn btn-default"/>
              </span>
            </div>
          </div>
      </div>
      </g:if>
      <g:else>
        <div class="bs-callout bs-callout-info">${common['grailsflow.message.noItems']}</div>
      </g:else>
      </g:form>

      <g:if test="${params.isEmbedded == 'true'}">
        <r:layoutResources/>
      </g:if>
  </body>
</html>
