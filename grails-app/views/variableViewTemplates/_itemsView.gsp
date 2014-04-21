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
	Template for displaying processVariable input. 

	Template parameters:

	required:	
    	* variable  			variable bean object.
		* view                  VariableView object.
	
	optional:
		* parameterName         'name' attribute for variable input. Default is empty.
 -->
<gf:messageBundle bundle="grailsflow.link" var="linkType"/>
<g:hiddenField name="datePattern" value="${gf.datePattern()}"/>

<g:set var="readOnly" value="${variable.visibility == com.jcatalog.grailsflow.utils.ConstantUtils.READ_ONLY}"/>
<g:set var="required" value="${variable.required != null ? variable.required : false}"/>

<!-- Preventing read-only parameter submitting -->

<g:set var="styleClass" value="${view?.styleClass}"/>

<script>
    var globalIndex = ${new Date().time}
    function deleteItem(element) {
        jQuery("#"+element).remove();
    }

    function addItem(parentElement, varName, value, newType, isReadonly, style) {
        globalIndex++
        var index = globalIndex
        var visibility = ''
        if (isReadonly == true) {
            visibility += ' disabled="disabled" class="readonly" name="" '
        } else {
            visibility += ' class ="'+style+'" '
        }

        var content = '<div id="listItem_'+varName+'_'+index+'">'
        content += '<input size="10" type="text" style="display: none;" value="'+newType+'" name="listItemType_'+varName+'_'+index+'"/>'
        if (newType == 'Boolean') {
            content += '<input type="checkbox" '
            if (value == "true") {
                content += ' checked="checked" '
            }
            if (isReadonly == false) {
                content+= ' name="listItemValue_'+varName+'_'+index+'"'
            }
            content += visibility+' />&nbsp;&nbsp;'

        } else if (newType == 'Link') {
            content += "${linkType['grailsflow.label.linkUrl']}"
            if (value == '') value = '  '
            content += '&nbsp;<input value="'+ value[0]+'" type="text" size="50" '
            if (isReadonly == false) {
                content+= ' name="listItemValue_'+varName+'_path_'+index+'"'
            }
            content += visibility+' />&nbsp;&nbsp;'
            content += "${linkType['grailsflow.label.linkDescription']}"
            content += '&nbsp;<input value="'+value[1]+'" type="text" size="25" '
            if (isReadonly == false) {
                content+= ' name="listItemValue_'+varName+'_desc_'+index+'"'
            }
            content += visibility+' />&nbsp;&nbsp;'
        } else if (newType == 'Date') {
            if(isReadonly == true) {
                content += ' <input name="" id="listItemValue_'+varName+"_"+index+'" value = "'+value+'" maxlength="20" readonly="true" class="readonly"/>'
            } else {
                content += " <script type='text/javascript'>jQuery.noConflict();"
                content += "jQuery(document).ready(function(\$){"
                content += " \$('#listItemValue_"+varName+"_"+index+"').datepicker({dateFormat: convertDatePatternFromJavaToJqueryDatePicker('"+"${gf.datePattern()}"+"'), showOn: 'button'}); }) <\/script>"
                content += ' <input name="listItemValue_'+varName+"_"+index+'" id="listItemValue_'+varName+"_"+index+'" value = "'+value+'" maxlength="20" class="'+style+'"/>'
            }
        } else if (newType == 'String') {
            content += ' <textarea '
            if (isReadonly == false) {
                content += ' name="listItemValue_'+varName+'_'+index+'" '
            }
            content += visibility +' >'
            content += value+'</textarea>&nbsp;&nbsp;'
        } else {
            content += ' <input value="'+value+'" type="text" size="30" '
            if (isReadonly == false) {
                content += ' name="listItemValue_'+varName+'_'+index+'" '
            }
            content += visibility +' />&nbsp;&nbsp;'
        }

        if(isReadonly == false) {
            content += '<a href="javascript: void(0)" onclick="deleteItem(\'listItem_'+varName+'_'+index+'\')"><img src="${g.resource(plugin: 'grailsflow-core', dir:'images/grailsflow/editor',file:'delete.gif')}" alt="Delete"/>'
            content += '</a>'
        }
        content += '</div>'
        jQuery("#"+parentElement+varName).append(content)
    }


    function checkTypeSelection(selectedValue, varName) {
        if(window.confirm('Are you sure? All prepared items will be deleted with changing type.')) {
            jQuery("#"+"listItem_"+varName).empty();
            jQuery("#"+"previousType_"+varName).val(selectedValue)
            return true;
        } else {
            var prevValue = jQuery("#"+"previousType_"+varName).val()
            jQuery("#"+"parent_varType_"+varName).val(prevValue);
            return false;
        }
    }
</script>

<g:if test="${readOnly == Boolean.FALSE}">
  <div>
    <g:hiddenField name="previousType_${variable.name}" value="${variable.subType}"/>
    <g:if test="${variable?.value && variable?.value instanceof List}">
      <input type="text" readonly="true" class="readonly"  value="${variable.subType}" name="parent_varType_${variable.name}" id="parent_varType_${variable.name}"/>
    </g:if>
    <g:else>
      <g:select from="${com.jcatalog.grailsflow.model.definition.ProcessVariableDef.listTypes}" value="${variable.subType}" name="parent_varType_${variable.name}" id="parent_varType_${variable.name}" onchange="checkTypeSelection(this.value, '${variable.name ?: ''}')"/>&nbsp;
    </g:else>
    <a href="javascript: void(0)" onclick="addItem('listItem_', '${variable.name}', '', document.getElementById('parent_varType_'+'${variable.name}').value, false, '${view?.styleClass}')">
      <img src="${g.resource(plugin: 'grailsflow-core', dir:'images/grailsflow/editor',file:'add.gif')}" alt="Add"/>
    </a>
  </div><br/>
</g:if>

<div id="listItem_${variable.name}">
  <g:if test="${variable?.value && variable?.value instanceof List}">
    <g:each in="${variable?.value}" var="listItem" status="i">
      <script>
          addItem('listItem_', '${variable.name}',
              ${variable.subType == 'Date' ? '"'+ (listItem ? gf.displayDate(value: listItem) : '')+'"' : (variable.subType == 'Link' ? ['"'+listItem.path+'"','"'+listItem.description+'"'] : (listItem ? '"'+listItem+'"' : '""'))},
              '${variable.subType}', ${readOnly}, '${styleClass}')
      </script>
    </g:each>
  </g:if>

  <g:elseif test="${variable?.items}">
    <g:each in="${variable?.items}" var="listItem" status="i">
      <script>
          addItem('listItem_', '${variable.name}',
              ${variable.subType == 'Date' ? '"'+ (listItem.content ? gf.displayDate(value: new Date(Long.parseLong(listItem.content))) : '')+'"' : (variable.subType == 'Link' ? ['"'+listItem.linkValue?.path+'"','"'+listItem.linkValue?.description+'"'] : (listItem.content ? '"'+listItem.content+'"' : '""'))},
              '${variable.subType}', ${readOnly}, '${styleClass}')
      </script>
    </g:each>
  </g:elseif>
</div>
