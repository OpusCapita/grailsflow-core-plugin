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
import com.jcatalog.grailsflow.utils.TranslationUtils

import org.codehaus.groovy.grails.web.pages.exceptions.GroovyPagesException

import com.jcatalog.grailsflow.model.process.BasicProcess
import com.jcatalog.grailsflow.model.process.ProcessNode
import org.springframework.web.servlet.support.RequestContextUtils as RCU

/**
 * Contains custom tags.
 *
 * @author Alexander Shulga
 * @author Maria Voitovich
 */
class GrailsflowTagLib {
  static namespace = "gf"
  def processManagerService
  def workareaPathProvider

  /**
   * TODO: use g:sortableColumn instead as soos
   * as http://jira.codehaus.org/browse/GRAILS-6231 get fixed 
   */
	def sortableColumn = { attrs ->
		def writer = out
		if(!attrs.property)
			throwTagError("Tag [sortableColumn] is missing required attribute [property]")

		if(!attrs.title && !attrs.titleKey)
			throwTagError("Tag [sortableColumn] is missing required attribute [title] or [titleKey]")

		def property = attrs.remove("property")
		def action = attrs.action ? attrs.remove("action") : (actionName ?: "list")

		def defaultOrder = attrs.remove("defaultOrder")
		if(defaultOrder != "desc") defaultOrder = "asc"

		// current sorting property and order
		def sort = params.sort
		def order = params.order

		// add sorting property and params to link params
		def linkParams = [:]
		if(params.id) linkParams.put("id",params.id)
		if(attrs.params) linkParams.putAll(attrs.remove("params"))
		linkParams.sort = property

		// determine and add sorting order for this column to link params
		attrs.class = (attrs.class ? "${attrs.class} sortable" : "sortable")
		if(property == sort) {
			attrs.class = attrs.class + " sorted " + order
			if(order == "asc") {
				linkParams.order = "desc"
			}
			else {
				linkParams.order = "asc"
			}
		}
		else {
			linkParams.order = defaultOrder
		}

		// determine column title
		def title = attrs.remove("title")
		def titleKey = attrs.remove("titleKey")
		if(titleKey) {
			if(!title) title = titleKey
			def messageSource = grailsAttributes.messageSource
			def locale = org.springframework.web.servlet.support.RequestContextUtils.getLocale(request)
			title = messageSource.getMessage(titleKey, null, title, locale)
		}

        def linkTagAttrs = [action:action]
        if(attrs.controller) {
            linkTagAttrs.controller = attrs.controller
        }
        if(attrs.id!=null) {
            linkTagAttrs.id = attrs.id
        }
        linkTagAttrs.params = linkParams

		writer << "<th "
		// process remaining attributes
		attrs.each { k, v ->
			writer << "${k}=\"${v.encodeAsHTML()}\" "
		}
		writer << ">${link(linkTagAttrs.clone()) { title }}</th>"
	}


  /**
   * TODO: use g:select instead as soon as
   * as http://jira.codehaus.org/browse/GRAILS-3961 get fixed 
   */
   def select = {attrs ->
        def messageSource = grailsAttributes.getApplicationContext().getBean("messageSource")
        def locale = org.springframework.web.servlet.support.RequestContextUtils.getLocale(request)
        def writer = out
        attrs.id = attrs.id ? attrs.id : attrs.name
        def from = attrs.remove('from')
        def keys = attrs.remove('keys')
        def optionKey = attrs.remove('optionKey')
        def optionValue = attrs.remove('optionValue')
        def value = attrs.remove('value')
        if (value instanceof Collection && attrs.multiple == null) {
            attrs.multiple = 'multiple'
        }
        if (value instanceof org.codehaus.groovy.grails.web.util.StreamCharBuffer) {
        	value = value.toString()
        }
        def valueMessagePrefix = attrs.remove('valueMessagePrefix')
        def noSelection = attrs.remove('noSelection')
        if (noSelection != null) {
            noSelection = noSelection.entrySet().iterator().next()
        }
        def disabled = attrs.remove('disabled')
        if (disabled && Boolean.valueOf(disabled)) {
            attrs.disabled = 'disabled'
        }

        def optionGroup = attrs.remove('optionGroup')

        writer << "<select name=\"${attrs.remove('name')?.encodeAsHTML()}\" "
        // process remaining attributes
        attrs.remove('tagName') // Just in case one is left
        attrs.each {k, v ->
           writer << "$k=\"${v.encodeAsHTML()}\" "
        }

        writer << '>'
        writer.println()

        if (noSelection) {
            writer << "<option value=\"${noSelection.key ?: ''}\"${noSelection.key == value ? ' selected="selected"' : ''}>${noSelection.value.encodeAsHTML()}</option>"
            writer.println()
        }

        // create options from list
        if (from) {
            // if group specified then order by group
            if (optionGroup != null) {
              from.sort() { a, b ->
                def groupA = a[optionGroup]
                def groupB = b[optionGroup]
                if (groupA == null) {
                  return groupB == null ? 0 : -1
                } else if (groupB == null) {
                  return 1
                } else {
                  return groupA.compareToIgnoreCase(groupB)
                }
              }
            }

            if (optionValue != null) {
              from.sort() { a, b ->
                def groupA = optionGroup ? a[optionGroup] : ''
                def groupB = optionGroup ? b[optionGroup] : ''
                def labelA = a[optionValue] ?: ''
                def labelB = b[optionValue] ?: ''
                if (groupA == groupB){
                  return labelA.compareToIgnoreCase(labelB)
                } else {
                  return 0
                }
              }
            }

            def lastGroup = ""
            from.eachWithIndex {el, i ->
                def currentGroup = ""
                if (optionGroup != null) {
                  currentGroup = el[optionGroup] ?: ""
                }
                if (currentGroup != lastGroup) {
                  if (lastGroup != "") {
                    writer << '</optgroup>'
                    writer.println()
                  }
                  writer << "<optgroup label=\"${currentGroup}\">"
                  writer.println()
                  lastGroup = currentGroup
                }

                def keyValue = null
                writer << '<option '
                if (keys) {
                    keyValue = keys[i]
                    writeValueAndCheckIfSelected(keyValue, value, writer)
                }
                else if (optionKey) {
                    def keyValueObject = null
                    if (optionKey instanceof Closure) {
                        keyValue = optionKey(el)
                    }
                    else if (el != null && optionKey == 'id' && grailsApplication.getArtefact(org.codehaus.groovy.grails.commons.DomainClassArtefactHandler.TYPE, el.getClass().name)) {
                        keyValue = el.ident()
                        keyValueObject = el
                    }
                    else {
                        keyValue = el[optionKey]
                        keyValueObject = el
                    }
                    writeValueAndCheckIfSelected(keyValue, value, writer, keyValueObject)
                }
                else {
                    keyValue = el
                    writeValueAndCheckIfSelected(keyValue, value, writer)
                }
                writer << '>'
                if (optionValue) {
                    if (optionValue instanceof Closure) {
                        writer << optionValue(el).toString().encodeAsHTML()
                    }
                    else {
                        writer << el[optionValue].toString().encodeAsHTML()
                    }
                }
                else if (valueMessagePrefix) {
                    def message = messageSource.getMessage("${valueMessagePrefix}.${keyValue}", null, null, locale)
                    if (message != null) {
                        writer << message.encodeAsHTML()
                    }
                    else if (keyValue) {
                        writer << keyValue.encodeAsHTML()
                    }
                    else {
                        def s = el.toString()
                        if (s) writer << s.encodeAsHTML()
                    }
                }
                else {
                    def s = el.toString()
                    if (s) writer << s.encodeAsHTML()
                }
                writer << '</option>'
                writer.println()
            }
            if (lastGroup != "") {
              writer << '</optgroup>'
              writer.println()
            }
        }
        // close tag
        writer << '</select>'
    }
    // helper for select 
    def typeConverter = new org.springframework.beans.SimpleTypeConverter()
    // helper method for select
    private writeValueAndCheckIfSelected(keyValue, value, writer) {
        writeValueAndCheckIfSelected(keyValue, value, writer, null)
    }
    // helper method for select
    private writeValueAndCheckIfSelected(keyValue, value, writer, el) {


        boolean selected = false
        def keyClass = keyValue?.getClass()
        if (keyClass.isInstance(value)) {
            selected = (keyValue == value)
        }
        else if (value instanceof Collection) {
            // first try keyValue
            selected = value.contains(keyValue)
            if (! selected && el != null) {
                selected = value.contains(el)
            }
        }
        else if (keyClass && value) {
            try {
                value = typeConverter.convertIfNecessary(value, keyClass)
                selected = (keyValue == value)
            } catch (Exception) {
                // ignore
            }
        }
        writer << "value=\"${keyValue}\" "
        if (selected) {
            writer << 'selected="selected" '
        }
    }


   /**
    * Renders link for workarea file
    *
    * parameters: 
    * - workareaPath    workarea-based path to file. mandatory
    * - label           link text. optional
    *
    */ 
    def workareaFileLink = { attrs->
      def workareaPath = attrs.workareaPath
      if (!workareaPath) {
        return
      }
      def fileUrl = workareaPathProvider.getResourceUrl(workareaPath)
      def fileName = attrs.label ? attrs.label : StringUtils.substringAfterLast(fileUrl, "/")
      out << """ <a href="${fileUrl?.encodeAsHTML()}" target="_blank">${fileName?.encodeAsHTML()}</a> """
    }

  /**
   *  Generate link to specify controller and action (process/openExternalUrl)
   *  and pass processNodeId as Id. Link will be generated in case of correspond Node
   *  contains externalUrl (not empty).
   *
   *  @todo we should evaluate externalUrl here and generate direct link by ExternalURL
   *  without usage ProcessController.openExternalUrl.
   *
   */
  def generateExternalUrl = { attrs ->
      //requred attributes
      def processNodeId = attrs.processNodeId
      def label = attrs.label
      //optional attributes
      def controller = attrs.controller ? attrs.controller : 'process'
      def action = attrs.action ? attrs.action : 'openExternalUrl'

      def node = ProcessNode.get(processNodeId)
      if (!node) {
          log.error("The Node for processNodeId($processNodeId) is missing.")
          return;
      }
      def basic = node.process
      def processClass = processManagerService.getProcessClass(basic.type)
      if (processClass) {
          def nodeDef = processClass.nodes[node.nodeID]
          def externalUrl = nodeDef?.externalUrl
          //check is externalURL available
          if (externalUrl) {
            //redirect tp controller/action/id
            out << """
                    <a href="${g.createLink(id:"${processNodeId}", controller:"${controller}", action:"${action}")}">${label}</a>
                    """
          }
      }
  }

    /**
     * Render template if exists.
     * Order of searching:
     * - [template] in the application
     * - [template] in the Grailsflow plugin
     * - [defaultTemplate] in the application
     * - [defaultTemplate] in the Grailsflow plugin
     * if nothing found then notFoundMessage (specified or default one) is printed
     *
     * attributes:
     * - template
     * - defaultTemplate (optional)
     * - model (optional)
     * - bean (optional)
     * - collection (optional)
     * - notFoundMessage (optional)
     * 
     * returns URI of template
     */
    def customizingTemplate = { attrs ->
      def template = attrs.template
      def defaultTemplate = attrs.defaultTemplate
      def notFoundMessage = attrs.notFoundMessage


      // Paths to look for template. Sorted by priority of searching.
      List parameters = new ArrayList()
      if (template) {
        parameters.add([contextPath: "", template: template])
        parameters.add([contextPath: "", template: template, plugin: 'grailsflow'])
      }
      if (defaultTemplate) {
        parameters.add([contextPath: "", template: defaultTemplate])
        parameters.add([contextPath: "", template: defaultTemplate, plugin: 'grailsflow'])
      }

      for (int i=0; i<parameters.size(); ++i) {
        def p = parameters[i]
        try {
          // Call render tag from Grails taglib
          out << g.render(template: p.template, contextPath: p.contextPath,
                          var: attrs.var, model: attrs.model, bean: attrs.bean,
                          plugin: p.plugin)
          return
        } catch (Exception e) {
          if (e.message?.startsWith("Template not found")) {
            log.info("There is no template '${p.template}' with contextPath '${p.contextPath}': ${e.message}")
          } else {
            log.info("Cannot render template '${p.template}' with contextPath '${p.contextPath}': ${e.message}")
            throw e
          }
        }
      }
      // Out notfound message
      if (!notFoundMessage) {
        if (template && defaultTemplate) {
         notFoundMessage = "Neither template '${template}' nor template '${defaultTemplate}' found."
        } else if (template) {
         notFoundMessage = "Template '${template}' not found. Default template is not specified or null."
        } else if (defaultTemplate) {
          notFoundMessage = "Template is not specified or null. Default template '${defaultTemplate}' not found."
        } else {
          notFoundMessage = "Both of template '${template}' and defaultTemplate '${defaultTemplate}' are not specified or null."
        }
      }
      out << notFoundMessage
    }

    /**
     * Render translation using current locale. If not found render default (not translatable) value
     *
     * attributes:
     * - translations - object that supports .get(lang) operation 
     * - default      - default value
     * - lang (optional) Default is request language
     * 
     * returns String
     */
    def translatedValue = { attrs ->
      def translations = attrs.translations
      def defaultValue = attrs.default
      def lang = attrs.lang ? attrs.lang : RCU.getLocale(request)?.language.toString()
      
      out << TranslationUtils.getTranslatedValue(translations, defaultValue, lang)
    }

    /**
     * Render tag body as section that's visibility can be toggled by clicking on it's header 
     *
     * attributes:
     * - title        - section header text. required
     * - selected     - initial visibility value. optional, default value 'true'
     * 
     */
    def section = {attrs, body ->
        def title = attrs.title
        if (!title) {
        }
        def sectionID = Math.abs(title.hashCode())
        def selected = attrs.selected != null ? Boolean.valueOf(attrs.selected) : true

        out << """<h4><a href="#${sectionID}" data-toggle="collapse" data-target="#${sectionID}"
           onclick="document.getElementById('span_${sectionID}').className=(document.getElementById('span_${sectionID}').className.indexOf('selected') > -1 ? 'fa fa-angle-up' : 'selected fa fa-angle-down');
           return false;">
           <span id="span_${sectionID}"  """
        if (selected) {
            out << """class="selected fa fa-angle-down" """
        } else {
            out << """class="fa fa-angle-up" """
        }
        out << "> </span> "
        out << title
        out << " </a></h4>"
        out << """<div id="${sectionID}" class="${selected ? 'collapse in' : 'collapse'}">
        """
        out << body()
        out << "</div>"
    }

}
