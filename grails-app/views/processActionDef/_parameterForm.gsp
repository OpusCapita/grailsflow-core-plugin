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
 Template that renders paameter value selection

 template parameters:
   param              parameter
   variables          collection of process variables
-->

<r:script>
   function ${param.name}ParamTypeChange() {
       var propsTable = document.getElementById("propsTable")

       document.getElementById("constant_${param.name}").style.display="none";
       document.getElementById("variable_${param.name}").style.display="none";
       document.getElementById("expression_${param.name}").style.display="none";

       document.getElementById(document.getElementById("paramType_${param.name}").value+"_${param.name}").style.display="";

   }

   function ${param.name}Value() {
     var valueType = document.getElementById("paramType_${param.name}").value;
     var value = "";
     switch (valueType) {
       case "constant":
          value = document.getElementById("param_${param.name}").value;
          break;
       case "variable":
          value = document.getElementById("var_${param.name}").value;
          /* disabled for now:
          var update = document.getElementById("var_${param.name}_update").checked;
          if (update) {
            value = "\$"+value
          }
          */
          break;
       case "expression":
          value = document.getElementById("expr_${param.name}").value;
          break;
       default:
     }
     return value;
   }

   function ${param.name}DefaultValue() {
      return document.getElementById("default_${param.name}").value;
   }
</r:script>

<td valign="top" align="right">${param.name}</td>
<td valign="top" align="left">
  <select name="paramType_${param.name}" id="paramType_${param.name}" onchange="${param.name}ParamTypeChange()" class="form-control">
    <option value="constant">Constant</option>
    <option value="expression">Expression</option>
    <option value="variable">Process Variable</option>
  </select>
</td>
<td valign="top" align="left">
    <input type="hidden" id="default_${param.name}" value="${param.value?.inspect()?.replaceAll("\"", "&quot;")}"/>
  <div id="constant_${param.name}">
    <input id="param_${param.name}" value="${param.value?.inspect()?.replaceAll("\"", "&quot;")}" size="25" class="form-control"/>
  </div>
  <div id="variable_${param.name}"  style="display: none;">
    <g:select name="var_${param.name}" id="var_${param.name}" class="form-control"  from="${variables.sort({var1, var2 -> var1.compareToIgnoreCase(var2)})}"/>
  </div>
  <div id="expression_${param.name}" style="display: none;">
    <textarea id="expr_${param.name}" cols="30" rows="5" class="form-control" >${param.value}</textarea>
  </div>
</td>
