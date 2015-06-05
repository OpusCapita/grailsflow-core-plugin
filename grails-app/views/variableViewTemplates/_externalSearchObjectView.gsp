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
        * variable                      variable bean object. Must be not null.
    * view                  VariableView object. Must be not null and of corresponding type.
  
  optional:
    * parameterName         'name' attribute for variable input. Default is empty.
 -->

    <g:set var="readOnly" value="${variable.visibility == com.jcatalog.grailsflow.utils.ConstantUtils.READ_ONLY}"/>
    <g:set var="required" value="${variable.required != null ? variable.required : false}"/>

    <!-- Setting default values -->
    <g:set var="readOnly" value="${readOnly != null ? readOnly : false}"/>
    <g:set var="required" value="${required != null ? required : false}"/>
    
    <g:set var="styleClass" value="${readOnly ? 'readonly' : ''}"/>
    <g:set var="size" value="${20}"/>

    <g:set var="additionalFields" value="${view.additionalFields ? view.additionalFields.split(',').collect(){ it.trim() } : [] }"/>
    <g:set var="displayKey" value="${view.displayKey ? view.displayKey : 'id'}"/>
    
    <input id="${parameterName}" type="hidden" name="${readOnly ? '' : parameterName}" value="${variable?.value ? variable?.value?.ident() : ''}"/>
    <input readonly="true" class="${styleClass}"  id="${parameterName}_${displayKey}"
           value="${(variable?.value ? variable?.value[displayKey] : '')?.encodeAsHTML()}" size="${size}"/>

    <g:if test="${!readOnly}">
      <g:set var="searchUrl" value="${view.searchUrl ? g.resource(file: view.searchUrl, contextPath: '') : ''}"/>
      <g:set var="callbackFunction" value="callbackSearch${parameterName}"/>
      <g:set var="clearFunction" value="clear${parameterName}"/>
    
      <script>
        function isArray(obj) {
          return obj.constructor.toString().indexOf("Array") != -1
        }
              
        function ${callbackFunction}(item) {
        
             // for compatibility with jCatalog searches
             if (item && item != 'undefined') { // jCatalog easysearch returns Array of values, get first result form array
               if (isArray(item)) {
                 if (item.length > 0) { 
                   item = item[0]
                 } else {
                   item = null
                 }
               }
             }
             if (item && item != 'undefined'){
               if (!item['ident'] || item['ident'] == 'undefined') { // jCatalog easysearch returns ident as 'key'
                 item['ident'] = item['key']
               }
               if (!item['${displayKey}'] || item['${displayKey}'] == 'undefined') {
                 <g:if test="${displayKey.endsWith('Id')}"> 
                   <g:set var="indexOfId" value="${displayKey.lastIndexOf('Id')}"/>
                   var propertyKey = '${com.jcatalog.grailsflow.utils.NameUtils.upCase(displayKey).substring(0, indexOfId)+"ID"}' // jCatalog easysearch returns capitalized property name and "ID" instead of "Id"
                 </g:if>
                 <g:else>
                   var propertyKey = '${com.jcatalog.grailsflow.utils.NameUtils.upCase(displayKey)}' // jCatalog easysearch returns capitalized property names as keys
                 </g:else>
                 item['${displayKey}'] = item[propertyKey]
               }
               <g:each var="field" in="${additionalFields}">
                 if (!item['${field}'] || item['${field}'] == 'undefined') {
                   <g:if test="${field.endsWith('Id')}"> 
                     <g:set var="indexOfId" value="${field.lastIndexOf('Id')}"/>
                     var propertyKey = '${com.jcatalog.grailsflow.utils.NameUtils.upCase(field).substring(0, indexOfId)+"ID"}' // jCatalog easysearch returns capitalized property name and "ID" instead of "Id"
                   </g:if>
                   <g:else>
                     var propertyKey = '${com.jcatalog.grailsflow.utils.NameUtils.upCase(field)}' // jCatalog easysearch returns capitalized property names as keys
                   </g:else>
                   item['${field}'] = item[propertyKey]
                 }
               </g:each>
             }
             
             if (item && item != 'undefined') {
                // set ident value
                document.getElementById('${parameterName}').value = item['ident']
                
                // set displayKey value
                document.getElementById('${parameterName}_${displayKey}').value = item['${displayKey}']
                
                // set additionalFields values
                <g:each var="field" in="${additionalFields}">
                  document.getElementById('${parameterName}_${field}').value = item['${field}']
                </g:each>
            } // if (item)
        } // function
        
        function ${clearFunction}() {
            // clear ident value
            document.getElementById('${parameterName}').value = ''
            
            // clear displayKey value
            document.getElementById('${parameterName}_${displayKey}').value = ''
            
            // clear additionalFields values
            <g:each var="field" in="${additionalFields}">
              document.getElementById('${parameterName}_${field}').value = ''
            </g:each>
        } // function
      </script>
      <g:set var="searchTitle" value="'${variable?.name}'" />
      <g:set var="searchString" value="'${searchUrl}?callbackFunctionName=${callbackFunction}'" />
      <g:set var="features" value="'screenX=50,screenY=100,resizable=yes,scrollbars=yes,status=yes,width=700,height=450'"/>
      <a class="search" href="#" onclick="winId=window.open(${searchString}, ${searchTitle}, ${features}); winId.focus(); return false;">
        <img src="${g.resource(plugin: 'grailsflow', dir: 'images/grailsflow/general', file: 'find.gif')}"/></a>
      &nbsp;
      <a href="#" onclick="${clearFunction}()">
        <span class="glyphicon glyphicon-remove text-danger"></span>
      </a>

    </g:if> <!-- if (!readOnly) -->
    
    <g:each var="field" in="${additionalFields}">
      <input id="${parameterName}_${field}" value="${(variable?.value ? variable?.value[field] : '')?.encodeAsHTML()}" size="${size}" readonly="true" class="${styleClass}"/>
      &nbsp;
    </g:each>
