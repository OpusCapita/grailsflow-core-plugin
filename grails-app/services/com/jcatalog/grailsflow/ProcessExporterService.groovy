package com.jcatalog.grailsflow
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


import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import org.codehaus.groovy.grails.web.context.ServletContextHolder

import com.jcatalog.grailsflow.utils.TranslationUtils

import groovy.xml.MarkupBuilder

import org.apache.commons.io.FileUtils
import org.springframework.context.MessageSource

/**
 * ProcessExporter service is class that that writes the process definition
 * and a Gif/Jpeg file of the process graph into a file.
 * We use HTML as the output format, because it can easily be read into other
 * applications (like MS Word) and can be viewed and printed.
 *
 * The export should include the following information:
 * - process name and description
 * - list of process variables with name, type and description
 * - list of nodes; for each node a list of transitions to other nodes and the target node name
 * - list of nodes; for each node a list of form fields (if available) and the associated actions.
 *  The actions should be exported, so that they are easily readable by a user, similiar to the
 *  presentation in the action editor (not in builder syntax)
 *
 * Except for the graphical diagram of the process, all other information
 * should be plain HTML with text, tables and minimal CSS formatting.
 *
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class ProcessExporterService {
    private static final String GRAPHIC_FILE = "graphic.png"

    def messageSource
    public static final String CONTENT_TYPE_ARCHIVE = 'application/zip'

    public File exportAsHTML(def processDef, def user, def lang) {
        def writer = new StringWriter()
        def builder = new MarkupBuilder(writer)
        Locale locale = new Locale(lang)
        int i = 0
        builder.html(){
          head(){
            title(messageSource.getMessage("plugin.grailsflow.label.processType", null, locale) + ": ${TranslationUtils.getTranslatedValue(processDef.label, processDef.processID, lang)}")
            meta("http-equiv":"Content-Type", "content":"text/html; charset=UTF-8")
            style("""
                   table.standard thead { color : #5B5B5B; font-weight : bold; }
                   table.standard th {
                     padding: 5px 8px 6px 8px;
                     background-color: rgb(230,234,245);
                     repeat-x: top;
                     color: rgb(5,38,121);
                     border-right: 1px solid white;
                     border-left: 0px solid white;
                     border-top: 1px solid rgb(120,136,180);
                     border-bottom: 1px solid rgb(120,136,180);
                   }
                   table.standard td { padding: 5px 5px 5px 5px; }
                   table.standard tr.even { background-color:  rgb(235,238,247); }
                   table.standard tr.odd { background-color: white; }

                   h2.headline {
                     font-family: Tahoma;
                     font-weight: bold;
                     font-size: 16px;
                     text-align: left;
                     padding: 2px 0px 2px 10px;
                     color: #002276;
                     border-bottom: #C0C0C0 1px solid;
                     margin: 6px 0px 6px 0px;
                   }
                   .hint {
                     color: rgb(97,130,207);
                     font-size: smaller;
                     font-style: italic;
                     padding: 0px;
                     margin: 0px;
                   }
             """)
          }

          body() {
            h2("class": "headline") {
              font(messageSource.getMessage("plugin.grailsflow.label.processScript", null, locale))
            }
            p("align": "right") {
              font(new Date())
              br()
              font("${user}")
            }
            table("class": "standard") {
              tr() {
                td(messageSource.getMessage("plugin.grailsflow.label.processID", null, locale))
                td(TranslationUtils.getTranslatedValue(processDef.label, processDef.processID, lang))
              }
              tr() {
                td(messageSource.getMessage("plugin.grailsflow.label.description", null, locale))
                td(TranslationUtils.getTranslatedValue(processDef.description, '', lang))
              }
            } // </table>
            br()

            // write process variables
            h2("class": "headline") {
              font(messageSource.getMessage("plugin.grailsflow.label.processVars", null, locale))
            }
            table("class": "standard", "width": "100%") {
              thead() {
                th(messageSource.getMessage("plugin.grailsflow.label.name", null, locale))
                th(messageSource.getMessage("plugin.grailsflow.label.type", null, locale))
                th(messageSource.getMessage("plugin.grailsflow.label.value", null, locale))
                th(messageSource.getMessage("plugin.grailsflow.label.processIdentifier", null, locale))
              }
              tbody() {
                i = 0
                processDef?.variables?.each() { var ->
                  tr("class": "${ (i % 2) == 0 ? 'odd' : 'even'}") {
                    td(TranslationUtils.getTranslatedValue(var.label, var.name, lang))
                    td(var.type)
                    td(var.defaultValue)
                    td(var.isProcessIdentifier)
                  }
                  i++
                }
              }
            } // </table>
            br()

            // write process nodes
            h2("class": "headline") {
              font(messageSource.getMessage("plugin.grailsflow.label.processNodes", null, locale))
            }
            table("class": "standard", "width": "100%") {
              thead() {
                th(messageSource.getMessage("plugin.grailsflow.label.nodeID", null, locale))
                th(messageSource.getMessage("plugin.grailsflow.label.type", null, locale))
                th(messageSource.getMessage("plugin.grailsflow.label.dueDate", null, locale))
                th(messageSource.getMessage("plugin.grailsflow.label.transitions", null, locale))
              }
              tbody() {
                i = 0
                processDef?.nodes?.each() { node ->
                  tr("class": "${ (i % 2) == 0 ? 'odd' : 'even'}") {
                    td(TranslationUtils.getTranslatedValue(node.label, node.nodeID, lang))
                    td(node.type)
                    td(node.dueDate)
                    td() {
                      node.transitions?.each() { transition ->
                          b(transition.event)
                          p("class": "hint") {
                             font(">> "+transition.toNodes.collect() { TranslationUtils.getTranslatedValue(it.label, it.nodeID, lang) } )
                          }
                      }
                    }
                  }
                  i++
                }
              }
            } // </table>
            br()

            // write process nodes
            h2("class": "headline") {
              font(messageSource.getMessage("plugin.grailsflow.label.actions", null, locale))
            }
            table("class": "standard", "width": "100%") {
              thead() {
                th(messageSource.getMessage("plugin.grailsflow.label.nodeID", null, locale))
                th(messageSource.getMessage("plugin.grailsflow.label.actions", null, locale))
              }
              tbody() {
                i = 0
                processDef?.nodes?.each() { node ->
                  tr("class": "${ (i % 2) == 0 ? 'odd' : 'even'}") {
                    td("valign": "top") {
                      font( TranslationUtils.getTranslatedValue(node.label, node.nodeID, lang) )
                    }
                    td() {
                      node.actionStatements?.each() { action ->
                        p(action.content)
                      }
                    }
                  }
                  i++
                }
              }
            } // </table>
            br()

            // write process graphic representation
            // img(src: GRAPHIC_FILE)

          } // </body>
        }  // </html>

        // prepare zip with sources
        def filesDir = new File(ServletContextHolder.servletContext.getRealPath("/tmp"))
        if (!filesDir.exists()) filesDir.mkdir()
        def baseDir = new File("${filesDir.absolutePath}/export-${System.currentTimeMillis()}")
        baseDir.mkdir()

        // TODO: create screenshot from showGraphic page
        
        def file = new File("${baseDir.absolutePath}/index.html")
        FileUtils.writeStringToFile(file, writer.toString(), "UTF-8") 

        def ant = new groovy.util.AntBuilder()

        def tmpFile = File.createTempFile("export", ".zip")
        tmpFile.delete()
        ant.zip(destfile: tmpFile.absolutePath, basedir: baseDir.absolutePath)
        ant.delete(dir: baseDir.absolutePath)
        ant.delete(dir: filesDir.absolutePath)
        return tmpFile
    }
}