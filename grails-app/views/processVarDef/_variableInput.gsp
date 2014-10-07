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

<gf:messageBundle bundle="grailsflow.link" var="linkType"/>
<r:require modules="grailsflowCalendar" />
<g:hiddenField name="datePattern" value="${gf.datePattern()}"/>

<g:if test="${variable?.type == 'Boolean'}">
  <g:checkBox name="varValue" value="${variable?.value ? variable?.value == 'true' : params.varValue}"/>
</g:if>
<g:elseif test="${variable?.type == 'Date'}">
  <gf:jQueryCalendar property="varValue" pattern="${gf.datePattern()}" value="${variable?.value ? new Date(new Long(variable?.value)) : null}" />
</g:elseif>
<g:elseif test="${variable?.type == 'Document'}">
</g:elseif>
<g:elseif test="${variable?.type == 'Link'}">
   ${linkType['grailsflow.label.linkUrl']}&nbsp;<input name="varValue_path" value="${(variable?.linkValue?.path ? variable?.linkValue?.path : params.varValue_path)?.encodeAsHTML()}" size="50"/>&nbsp;
   ${linkType['grailsflow.label.linkDescription']}&nbsp;<input name="varValue_description" value="${(variable?.linkValue?.description ? variable?.linkValue?.description : params.varValue_description)?.encodeAsHTML()}" size="25"/>
</g:elseif>
<g:elseif test="${variable?.type == 'List'}">

<script type="text/javascript">
    var globalIndex = 0;
    function deleteItem(element) {
        jQuery("#"+element).remove();
    }

    function addItem(parentElement, varName, value, newType) {
        globalIndex++;
        var index = globalIndex;
        var content = '<div id="listItem_'+varName+'_'+index+'">';
        content += '<input size="10" type="text" readonly="true" class="readonly" style="display: none;" value="'+newType+'" name="listItemType_'+varName+'_'+index+'"/>';
        if (newType == 'Boolean') {
            content += '<input type="checkbox" name="listItemValue_'+varName+'_'+index+'"';
            if (value == "true") {
                content += ' checked="checked" '
            }
            content += '/>&nbsp;&nbsp;'
        } else if (newType == 'Link') {
            content += "${linkType['grailsflow.label.linkUrl']}";
            if (value == '') value = '  ';
            content += '&nbsp;<input value="'+ value[0]+'" type="text" size="50" name="listItemValue_'+varName+'_path_'+index+'"/>&nbsp;&nbsp;';
            content += "${linkType['grailsflow.label.linkDescription']}";
            content += '&nbsp;<input value="'+value[1]+'" type="text" size="25" name="listItemValue_'+varName+'_desc_'+index+'"/>&nbsp;&nbsp;'
        } else if (newType == 'Date'){
            content += " <script type='text/javascript'>jQuery.noConflict();";
            content += "jQuery(document).ready(function(\$){";
            content += " \$('#listItemValue_"+varName+"_"+index+"').datepicker({dateFormat: convertDatePatternFromJavaToJqueryDatePicker('"+"${gf.datePattern()}"+"'), showOn: 'button'}); }) <\/script>";
            content += ' <input name="listItemValue_'+varName+"_"+index+'" id="listItemValue_'+varName+"_"+index+'" value = "'+value+'" maxlength="20" readonly="true"/>'
        } else if (newType == 'String') {
            content += ' <textarea cols="60" rows="3" name="listItemValue_'+varName+'_'+index+'">' + value + '</textarea>&nbsp;&nbsp;'
        } else {
            content += ' <input value="'+value+'" type="text" size="30" name="listItemValue_'+varName+'_'+index+'"/>&nbsp;&nbsp;'
        }

        content += '<a href="javascript: void(0)" onclick="deleteItem(\'listItem_'+varName+'_'+index+'\')"><img src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/editor',file:'delete.gif')}" alt="Delete"/>';
        content += '</a>';
        content += '</div>';
        jQuery("#"+parentElement+varName).append(content)
    }

    function checkTypeSelection(selectedValue) {
        if(window.confirm('Are you sure? All prepared items will be deleted with changing type.')) {
            jQuery("#"+"listItem_${variable.name ?: ''}").empty();
            jQuery("#"+"previousType_${variable.name ?: ''}").val(selectedValue);
                return true;
        } else {
            var prevValue = jQuery("#"+"previousType_${variable.name ?: ''}").val();
            jQuery("#"+"parent_varType_${variable.name ?: ''}").val(prevValue);
            return false;
        }
    }
</script>

    <div>
      <g:hiddenField name="previousType_${variable.name}" value="${variable.subType}"/>
      <g:select from="${com.jcatalog.grailsflow.model.definition.ProcessVariableDef.listTypes}" value="${variable.subType}" name="parent_varType_${variable.name ?: ''}" id="parent_varType_${variable.name ?: ''}" onchange="checkTypeSelection(this.value)"/>&nbsp;
      <a href="javascript: void(0)" onclick="addItem('listItem_', '${variable.name}', '', document.getElementById('parent_varType_'+'${variable.name}').value)"><img src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/editor',file:'add.gif')}" alt="Add"/></a>
    </div><br/>
    <div id="listItem_${variable.name ?: ''}">
      <g:each in="${variable?.items}" var="listItem" status="i">
        <script>
            addItem('listItem_', '${variable.name}',
                 ${variable.subType == 'Date' ? '"'+ (listItem.content ? gf.displayDate(value: new Date(Long.parseLong(listItem.content))) : '')+'"' : (variable.subType == 'Link' ? ['"'+listItem.linkValue?.path+'"','"'+listItem.linkValue?.description+'"'] : (listItem.content ? '"'+listItem.content+'"' : '""'))}, '${variable.subType}')
        </script>
      </g:each>
    </div>
</g:elseif>
<g:else>
  <input name="varValue" value="${(variable?.value ? variable?.value : params.varValue)?.encodeAsHTML()}" size="50"/>
</g:else>

<script type="text/javascript">
    var type = document.getElementById("varType").value;
    if (type == 'Object') {
        document.getElementById("objectType").style.display=""
    }
</script>
