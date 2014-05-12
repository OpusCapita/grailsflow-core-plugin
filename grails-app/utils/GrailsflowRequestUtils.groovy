/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import com.jcatalog.grailsflow.model.view.VariableView

import java.text.SimpleDateFormat
import java.text.ParseException

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.springframework.web.multipart.commons.CommonsMultipartFile

import com.jcatalog.grailsflow.process.Link
import com.jcatalog.grailsflow.process.Document
import com.jcatalog.grailsflow.model.process.ProcessVariable

/**
 * Helper methods for retrieving specific data from request
 *
 * @author Maria Voitovich
 */  
class GrailsflowRequestUtils {
    static protected Log log = LogFactory.getLog(getClass())

    /**
     * Creates date from request parameters submited by datepicker
     */
    def static Date getDateFromParams(def params, String parameter) {
      def year = params["${parameter}_year".toString()]
      def month = params["${parameter}_month".toString()]
      def day = params["${parameter}_day".toString()]
      def date = null
      if (year && month && day) {
        def sdf = new SimpleDateFormat("yyyy.M.dd")
        def dateString = "${year}.${month}.${day}"
	      try {
	          date = sdf.parse(dateString.toString())
	      } catch (ParseException e){
	          log.error("Request parameter ${parameter} does not contain valid date value: cannot convert ${dateString} to date.")
	          return null
	      }
      } else {
        return null
      }
    }

    /**
     * Creates map of translations from request parameters
     * Properties for view are taken from params.<viewType>_<propertyName>
     */
    public static Map<String, String> getTranslationsMapFromParams(def params, String parameterPrefix){
      def translationParameters = params ? params.findAll() {key, value -> key.startsWith(parameterPrefix)} : [:]
      def translations = [:]
      translationParameters.each() { key, value ->
        def lang = StringUtils.substringAfter(key, parameterPrefix)
        if (lang && value) {
          translations.put(lang, value)
        }
      }
      def addLang = params["add_${parameterPrefix}lang".toString()]
      def addValue = params["add_${parameterPrefix}value".toString()]
      if (addLang && addValue) {
        translations.put(addLang, addValue)
      }
      return translations;
    }

    /**
     * Creates VariableView object from request parameters
     * Class of view defined by params.variableViewType
     * Properties for view are taken from params.<variableViewType>_<propertyName>
     */
    public static VariableView getVariableViewFromParams(def params){
        def type = params.variableViewType
        def viewClass = VariableView.getViewClassFromViewType(type)
        if (!viewClass) {
          log.error("Cannot get view class for view type ${type}")
          return null;
        }
        try {
          def view = viewClass.newInstance()
          def parameterPrefix = "${type}_"
          def viewParameters = params ? params.findAll() {key, value -> key.startsWith(parameterPrefix)} : [:] 
          def viewProperties = [:]
          viewParameters.each(){ key, value ->
            def propertyName = StringUtils.substringAfter(key, parameterPrefix)
            viewProperties.put(propertyName, value)
          }
          view.properties = viewProperties
          return view
        } catch (Exception e) {
          log.error("Error occurred while creating view of type ${type}")
        }
        return null;
    }


    public static  Link getLinkFromParams(def params, String parameter){
          def path = params["${parameter}.path".toString()]
          def description = params["${parameter}.description".toString()]
          return new Link(path: path, description: description)
    }

    public static  Document getDocumentFromRequest(def request, String parameter, File documentsRoot){
	    CommonsMultipartFile file = request.getFile(parameter)

	    if(file && !file.isEmpty()){
	        //creating attachment directory for current date if necessary
	        def sdf = new SimpleDateFormat("yyyy.MM.dd")
	        File documentsDirectory = new File(
	                "${documentsRoot.absolutePath}/${sdf.format(new Date())}")
	        if(!documentsDirectory.exists()){
	            documentsDirectory.mkdirs()
	        }

	        //document file name: process.id + file count + initial filename without special characters
	        def fileName = file.originalFilename.replaceAll('[^a-zA-Z0-9\\.]','')
	        File document = new File(documentsDirectory,
	            "${request.getParameter('processID')}-${documentsDirectory.listFiles().size()+1}-${fileName}")

	        file.transferTo(document)
	        return new Document(documentUrl: StringUtils.substringAfter(document.toURI().toString(),
	                documentsRoot.toURI().toString()))
	    } else if (file && file.fileItem) {
	        log.error("Cannot get Document for ${parameter} parameter. File is not posted or is empty")
            return null
	    }
    }

    public static List getVariableItemsFromParams(String variableName, Map parameters){
        List items = []

        Map types = parameters.findAll {String key, value -> key.startsWith("listItemType_${variableName}_") }
        Map values = parameters.findAll {key, value -> key.indexOf("listItemValue_${variableName}_") != -1 }
        types?.each() { String typeKey, String typeValue ->
            try {
                String type = typeValue
                String index = typeKey.substring("listItemType_${variableName}_".length())
                String content = values["listItemValue_${variableName}_${index}"]
                Object convertedValue
                if (type == 'Link') {
                    content = "['path':'"+ values["listItemValue_${variableName}_path_${index}"]+"','description':'"+values["listItemValue_${variableName}_desc_${index}"]+"']"
                } else if (type == 'Boolean') {
                    content =  content ? "true" : "false"
                } else if (type == 'Date') {
                    String pattern = parameters.datePattern
                    content = GrailsflowUtils.getParsedDate(values["listItemValue_${variableName}_${index}"], pattern)?.time?.toString()
                } else {
                    content = values["listItemValue_${variableName}_${index}"] ?: ''
                }
                items << ProcessVariable.getConvertedValue(content, type, null)
            } catch (Exception ex) {
                log.error("Impossible to get item value from parameters, probably the value is not fit list items type.", ex)
                throw ex
            }
        }

        return items
    }

}