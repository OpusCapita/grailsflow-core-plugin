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
         <gf:messageBundle bundle="grailsflow.common" var="common"/>

         <title>Action Parameters</title>

         <g:set var="parametersNames" value="${actionParameters.collect(){ it.name.inspect() } }"/>

         <r:script>

           var parameters = new Array(${parametersNames.join(", ")});

           function addAction() {
             var signature = "${actionName}"
             var params = new Array()
             for(i=0; i<parameters.length; ++i) {
               var name = parameters[i]
               var value = eval(name+"Value()");
               var defaultValue = eval(name+"DefaultValue()");
               if (value != defaultValue) {
                   if (value.length == 0) {
                     value = "null"
                   }
                   params.push(name+": "+value);
               }
             }
             window.opener.pasteAction(signature+"("+params.join(", ")+")");
             window.close();
           }

         </r:script>
         <r:layoutResources/>
    </head>
    <body>
      <div class="container">
        <div class="row" style="margin-top: 10px;">
          <div class="col-md-12">
            <div class="panel panel-default">
              <div class="panel-heading"><h1>Action ${actionName}</h1></div>
              <div class="panel-body">
                <h1>Action Call Parameters</h1>
                <table id="propsTable" class="table table-bordered">
                  <g:each in="${actionParameters}">
                    <tr>
                      <gf:customizingTemplate template="parameterForm" model="[param: it, variables: variables]"/>
                    </tr>
                  </g:each>
                </table>
                <input type="button" class="btn btn-primary"  value="${common['grailsflow.command.apply']}" onclick="addAction();"/>&nbsp;
                <input type="button" class="btn btn-default" value="${common['grailsflow.command.cancel']}" onclick="window.close();"/>
                <r:layoutResources/>
              </div>
            </div>
          </div>
        </div>
      </div>
    </body>
</html>





