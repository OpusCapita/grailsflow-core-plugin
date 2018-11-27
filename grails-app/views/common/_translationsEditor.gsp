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
	Template for selecting users and roles. 

	Template parameters:

  required:	
		* translations 		      map of language -> value
		* parameterName         'name' attribute prefix for for translations input

	optional:
	  * supportedLanguages   	list of supported languages. Default is ['', 'en', 'de']
	  * textarea							if true, then textarea is rendered instead of input. Default is false					
	  * size									size of input. Default is 20
	  * cols									columns size of textarea input. Defaullt is 20
	  * rows									rows size of textarea input. Default is 5
 -->
 
  <r:script>
    function addTranslation(parameterName){
      var langSelect = document.getElementById('add_'+parameterName+'_lang')
      var lang = langSelect.value
      if (lang.trim() == "") return false;
      
      var addValueInput = document.getElementById('add_'+parameterName+'_value')
      var value = addValueInput.value
      if (value.trim() == "") return false;
      
      var table = document.getElementById(parameterName+'_translations')
      
      // add new row
      var row = table.insertRow(table.rows.length)
      row.id = parameterName+'_'+lang+'_row'
      
      // language
      var langCell = row.insertCell(0)
      langCell.innerHTML = lang

      // value
      var valueInput = cloneSampleElement('sample_'+parameterName+'_value')
      valueInput.name = parameterName+'_'+lang
      valueInput.value = value
      var valueCell = row.insertCell(1)
      valueCell.appendChild(valueInput)
      
      // remove link
      var removeLink = cloneSampleElement('sample_'+parameterName+'_link')
      removeLink.onclick = function() { return removeTranslation(parameterName, lang) }
      var linkCell = row.insertCell(2)
      linkCell.appendChild(removeLink)
      
      // remove lang form available       
      removeAvailableLanguage(langSelect, lang)
      
      // cleanup input
      addValueInput.value=""
      
      return false;
    }
    
    function cloneSampleElement(sampleId) {
      var sampleElement = document.getElementById(sampleId)
      var cloneElement = sampleElement.cloneNode(true);
      cloneElement.id = ''
      cloneElement.style.display = ''
      return cloneElement
    }
    
    function removeTranslation(parameterName, lang){
      var langSelect = document.getElementById('add_'+parameterName+'_lang')
      var table = document.getElementById(parameterName+'_translations')
      var row = document.getElementById(parameterName+'_'+lang+'_row')
      if (row) { 
        table.deleteRow(row.rowIndex)
        // add lang to available
        addAvailableLanguage(langSelect, lang)
      }
      return false;
    }

    function addAvailableLanguage(selectElement, lang){
      addOption(selectElement, new Option(lang))
      selectElement.options.length > 0
    }

    function removeAvailableLanguage(selectElement, lang){
      var itemNo = -1;
      for (i=0; i<selectElement.options.length; ++i) {
        if (selectElement.options[i].value == lang) {
          selectElement.remove(i);
          break;  
        }
      }
    }
    
  </r:script>

  <g:set var="langs" value="${supportedLanguages != null ? supportedLanguages : ['en', 'de', 'zh_CN']}"/>
  <g:set var="visible_langs" value="${langs - translations?.keySet()}"/>
  <g:set var="textarea" value="${textarea != null ? textarea : false}"/>
  
  <!-- samples for dynamic rows creation -->
  <g:if test="${textarea}">
    <textarea id="sample_${parameterName}_value" rows="${rows != null ? rows : 5}" cols="${cols != null ? cols : 40}" style="display: none" class="form-control" ></textarea>
  </g:if>
  <g:else>
    <input id="sample_${parameterName}_value" type="text" size="${size ? size : 40}" style="display: none" class="form-control" />
  </g:else>
  <a id="sample_${parameterName}_link" href="#" style="display: none" title="${g.message(code: 'plugin.grailsflow.command.delete')}">
    <span class="glyphicon glyphicon-remove text-danger"></span>&nbsp;
  </a>
  
  <table id="${parameterName}_translations" class="table">
    <tr>
	  <td valign="top"><g:select id="add_${parameterName}_lang" name="add_${parameterName}_lang" from="${visible_langs}"/></td>
	  <td valign="top">
		<g:if test="${textarea}">
		  <textarea id="add_${parameterName}_value" name="add_${parameterName}_value" rows="${rows != null ? rows : 5}" cols="${cols != null ? cols : 40}" class="form-control" ></textarea>
		</g:if>
		<g:else>
		  <input id="add_${parameterName}_value" name="add_${parameterName}_value" type="text" size="${size ? size : 40}" class="form-control" />
		</g:else>
	  </td>
	  <td valign="top">
        <a href="#" onclick="addTranslation('${parameterName}')" title="${g.message(code: 'plugin.grailsflow.command.add')}">
          <span class="glyphicon glyphicon-plus text-success"></span>&nbsp;
        </a>
      </td>
    </tr>
    <g:each in="${translations?.keySet()}" var="lang">
	  <tr id="${parameterName}_${lang}_row">
	    <td valign="top">${lang}</td>
	    <td valign="top">
		  <g:if test="${textarea}">
    		<textarea name="${parameterName}_${lang}" rows="${rows != null ? rows : 5}" cols="${cols != null ? cols : 40}" class="form-control" >${translations[lang]?.encodeAsHTML()}</textarea>
		  </g:if>
		  <g:else>
		    <input name="${parameterName}_${lang}" type="text" size="${size ? size : 40}" value="${translations[lang]?.encodeAsHTML()}" class="form-control" />
		  </g:else>
	    </td>
	    <td valign="top">
          <a href="#" onclick="removeTranslation('${parameterName}', '${lang}')" title="${g.message(code: 'plugin.grailsflow.command.delete')}">
            <span class="glyphicon glyphicon-remove text-danger"></span>&nbsp;
          </a>
        </td>
	  </tr>
	</g:each>
  </table>
