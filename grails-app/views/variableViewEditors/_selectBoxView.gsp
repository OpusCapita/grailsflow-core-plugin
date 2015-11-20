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
        * viewType                                              View type. Cannot be null.
                * view                                           VariableView instance of corresponding type. Can be null.

  development note:
    - parameter names for properties should have "<viewType>_<viewPropertyName>" format 

 -->
<script>
    var order = 0;
    var number = 0;
    function addRow(value) {
      order ++;
      number ++;
      var rowOrder = order;
      var table = document.getElementById('selectBoxTable');
      var tableBody = table.getElementsByTagName('tbody')[0];

      var newRow = document.createElement("tr");
      var newColumn1 = document.createElement("td");
      var inp = document.createElement("input");
      inp.type="text";
      inp.id = "selectBoxView_"+rowOrder;
      inp.name = "selectBoxView_itemsString";
      inp.size = 40;
      inp.value = value;
      newColumn1.appendChild(inp);

      var newColumn2 = document.createElement("td");
      var linkDelete = document.createElement("a");
      linkDelete.href="#"
      linkDelete.onclick = function() { deleteRow(rowOrder); };
      var span = document.createElement("span");
      span.className = "glyphicon glyphicon-remove text-danger"
      linkDelete.appendChild(span);
      newColumn2.appendChild(linkDelete);

      newRow.appendChild(newColumn1);
      newRow.appendChild(newColumn2);
      tableBody.appendChild(newRow);
      table.appendChild(tableBody);
    }

    function deleteRow(row) {
      var table = document.getElementById('selectBoxTable');
      var tableBody = table.getElementsByTagName('tbody')[0];
      var elements = tableBody.getElementsByTagName("tr");
      var deletedRow;
      var deletedInput;
      for (var i = 0; i < elements.length; i++) {
        var rowElement = elements.item(i);
        var cols = rowElement.getElementsByTagName("td");
        var input = cols[0].getElementsByTagName("input")[0];
        if (input.id && input.id.substring(14) == row) {
            deletedRow = rowElement;
            deletedInput = input;
        }
      }

      if (number > 1) {
        number --;
        tableBody.removeChild(deletedRow);        
      } else {
        deletedInput.value = '';
        alert("The Select Box is empty!")
      }
    }
 </script>

 <style type="text/css">
   * html div#selectBoxBlock {
       height: expression(this.scrollHeight > 500 ? "500px" : "auto" );
       overflow: auto;
   }

   div#selectBoxBlock {
       max-height: 500px;
       overflow: auto;
   }
 </style>

<h3><g:message code="plugin.grailsflow.label.selectBoxView"/></h3>

<label><g:message code="plugin.grailsflow.label.items"/>:</label>
  &nbsp;&nbsp;&nbsp;
<a href="#" title="${g.message(code: 'plugin.grailsflow.label.addItem')}" onclick="addRow('');">
  <g:message code="plugin.grailsflow.label.addItem"/>
</a>&nbsp;
<a href="#" title="${g.message(code: 'plugin.grailsflow.label.addItem')}" onclick="addRow('');">
  <span class="glyphicon glyphicon-plus text-success"></span>
</a>

<div id="selectBoxBlock">
  <table id="selectBoxTable">
    <tbody>
      <g:if test="${!view?.items || view?.items?.size() == 0}">
        <script>
          addRow("");
        </script>
      </g:if>
    </tbody>
  </table>
</div>

<g:each in="${view?.items}" status="i" var="statement">
  <script>
    addRow("${statement?.encodeAsJavaScript()}")
  </script>
</g:each>
