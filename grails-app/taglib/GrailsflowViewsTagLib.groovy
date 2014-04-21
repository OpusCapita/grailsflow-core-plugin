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

import org.apache.commons.lang.StringUtils
import com.jcatalog.grailsflow.utils.NameUtils
import com.jcatalog.grailsflow.utils.TranslationUtils

import org.codehaus.groovy.grails.web.pages.exceptions.GroovyPagesException

/**
 * Contains tags for support VariableViews.
 *
 * @author July Antonicheva
 */
class GrailsflowViewsTagLib {
    static namespace = "gf"
    def documentsPath

   /**
    * Renders link for DocumentView
    *
    * parameters: 
    * - document        Document object. mandatory
    * - label           Document label. Optional. Default value is Document filaname. 
    *
    */ 
    def renderDocument = { attrs->
      def document = attrs.document
      if (!document) {
        return
      }
      def workareaDocumentPath = "${documentsPath}/${document?.documentUrl}"
      def documentName = attrs.label ? attrs.label : StringUtils.substringAfterLast(document?.documentUrl, "/")
      out << gf.workareaFileLink(workareaPath: workareaDocumentPath, label: documentName)
    }
    
   /**
    * Renders link for LinkView
    *
    * parameters:
    * - link        Link object. mandatory
    * - label       link label. optional. default value is link.description
    * - isExternal  define if the link absolute (external) or relative
    */ 
    def renderLink = { attrs->
       def link = attrs.link
       if (!link) return

       def url = link.path
       if (!url) return
    
       if (link.isExternal()) {
         if (url.startsWith("www.")) url = "http://$url"
       } else {
         url = grailsAttributes.getApplicationUri(request)+"/$url"
       }
       def label = attrs.label ? attrs.label : (link.description ? link.description : link.path)
       
       out << """<a href="${url?.encodeAsHTML()}" target="_blank">${label?.encodeAsHTML()}</a> """
    }
    

  /**
    * Renders selectbox for listObjectsView
    *
    * parameters: 
    * - searchClass     domain class name. mandatory
    * - restrictions    where clause without word 'where'. optional
    * - displayKey      name of property that will be used as option label. optional. if not specified than object.ident() will be used
    * - readonly        readonly. default is false
    * - styleClass      CSS class for selectbox
    *
    */ 
  def listObjectsBox = { attrs ->
    def query = "from ${attrs.searchClass} "

    if (attrs.restriction) {
      query += "where ${attrs.restriction}"
    }

    def params
    out << """
            <select name="${attrs.name}" class="${attrs.styleClass}" ${attrs.readonly == true ? 'disabled="true"' : ''}">
              <option value=""></option>
          """
    try {
        getClass().getClassLoader().loadClass(attrs.searchClass, false)?.executeQuery(query).each() {
           if (it.ident() == attrs.value?.ident()) {
                out << """
                         <option value="${it.ident()?.encodeAsHTML()}" selected="true">${(attrs.displayKey ? it.properties[attrs.displayKey] : it.ident())?.encodeAsHTML()}</option>
                       """
           } else {
                out << """
                         <option value="${it.ident()?.encodeAsHTML()}" >${(attrs.displayKey ? it.properties[attrs.displayKey] : it.ident())?.encodeAsHTML()}</option>
                       """
           }
       }
    } catch (Exception e) {
        log.error("Exception occurred while generating select box for class ${attrs.searchClass} with restrictions ${attrs.restriction}",e)
    }

      out << """
            </select>
         """
  }
}
