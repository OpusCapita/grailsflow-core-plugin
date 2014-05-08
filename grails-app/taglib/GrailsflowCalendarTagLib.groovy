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

/**
 * Renders JSCal2 calendar as date picker
 *
 * @author Maria Voitovich
 */
import org.springframework.context.i18n.LocaleContextHolder

class GrailsflowCalendarTagLib {

    static namespace = "gf"

    private String convertDatePattern(String datePattern) {
      return datePattern.replaceAll("MM", "%m").replaceAll("M", "%o").
          replaceAll("yyyy", "%Y").replaceAll("yy", "%y").
          replaceAll("d", "%e").replaceAll("%e%e", "%d")
    }

    def datePicker = { attrs ->

        if (!attrs.name)
            throwTagError("Tag [datePicker] is missing required attribute [name]")

        String name = attrs.remove('name')

        String format = attrs.format ? attrs.remove("format") : gf.datePattern()

        String dateFormat =  convertDatePattern(format)

        def value = attrs.remove('value')

        def calendar = null

        def day = ''
		def month = ''
		def year = ''

        if(value) {

            if(value instanceof Calendar) {
			    calendar = value
		    } else {
			    calendar = new GregorianCalendar();
			    calendar.setTime(value)
		    }

		    day = calendar.get(GregorianCalendar.DAY_OF_MONTH)
			month = calendar.get(GregorianCalendar.MONTH)+1
			year = calendar.get(GregorianCalendar.YEAR)
		}

        def dateParam = value ? "new Date(${year},${month -1},$day,0,0)" : null


        out << """
            <input type="hidden" name="${name}_year" id="${name}_year" value="$year"/>
            <input type="hidden" name="${name}_month" id="${name}_month" value="$month"/>
            <input type="hidden" name="${name}_day" id="${name}_day" value="$day"/>
            <input type="hidden" name="${name}" id="${name}" value=""/>

            <input type="text" id="${name}_value" readonly="true"/>

            <script type="text/javascript">

                ${dateParam ? "document.getElementById('${name}_value').value = Calendar.printDate(${dateParam}, '${dateFormat}')" : "" }

                var ${name}_calendar = Calendar.setup({
                    name:"$name",
                    inputField:"${name}_value",
                    ${dateParam ? "date:${dateParam}," : "" }
                    ${dateParam ? "selection:Calendar.dateToInt(${dateParam})," : ""}
                    dateFormat:"${dateFormat}",
                    onSelect:${name}_select
                });

                function ${name}_select(calendar) {
                  var date = Calendar.intToDate(calendar.selection.get());
                  document.getElementById('${name}_value').value=Calendar.printDate(date, '${dateFormat}');
                  document.getElementById('${name}').value='struct'
                  document.getElementById('${name}_year').value= date.getFullYear();
                  document.getElementById('${name}_month').value= date.getMonth()+1;
                  document.getElementById('${name}_day').value= date.getDate();
                  calendar.hide();
                }

                function ${name}_clean() {
                  document.getElementById('${name}').value='';
                  document.getElementById('${name}_value').value=''
                  document.getElementById('${name}_year').value= '';
                  document.getElementById('${name}_month').value= '';
                  document.getElementById('${name}_day').value= '';
                }

            </script>
            <img src="${g.resource(dir:pluginContextPath,file:"images/calendar/calendar.png")}" id="$name-trigger" alt="Date" onclick="${name}_calendar.popup('$name-trigger');"/>

       """

    }

    def calendarResources = { attrs ->

      String lang = LocaleContextHolder.locale.language.toString()

      out << """
        <style type='text/css'>@import url(${g.resource(dir:pluginContextPath,file:"css/calendar/jscal2.css")});</style>
        <style type='text/css'>@import url(${g.resource(dir:pluginContextPath,file:"css/calendar/border-radius.css")});</style>
        <style type='text/css'>@import url(${g.resource(dir:pluginContextPath,file:"css/calendar/grailsflow.css")});</style>
        <script type="text/javascript" src="${g.resource(dir:pluginContextPath,file:"js/JSCal2-1.7/jscal2.js")}"></script>\n
        <script type="text/javascript" src="${g.resource(dir:pluginContextPath,file:"js/JSCal2-1.7/lang/")}${lang}.js"></script>\n
       """
    }

    /**
     * @deprecated use <r:require modules="grailsflowCalendar" />
     */
    @Deprecated
    def jQueryCalendarResources = { attrs ->
        r.require(module: "grailsflowCalendar")
    }

    def jQueryCalendar = { attrs ->
       out << """
         <script type="text/javascript">
           jQuery.noConflict();
           jQuery(document).ready(function(\$){
             \$('#${attrs.property}').datepicker({dateFormat: convertDatePatternFromJavaToJqueryDatePicker('${gf.datePattern()}'), showOn: "button"});
           })
         </script>
         <span class="jQueryCalendar" >
           <input name="${attrs.property}" id="${attrs.property}" value = "${(attrs.value instanceof Date) ? gf.displayDate(value: attrs.value) : (attrs.value ?: '')}"
                maxlength="${attrs.maxlength ? attrs.maxlength : "20"}" size="${attrs.size}" class="${attrs.class}" style="${attrs.style}" readonly="true" />
           <a href="javascript: void(0)" onclick="jQuery('#${attrs.property}').val('')"><img src="${g.resource(plugin: 'grailsflow-core', dir:'images/grailsflow/editor',file:'delete.gif')}" alt="Delete"/></a>
         </span>
       """

    }

}




