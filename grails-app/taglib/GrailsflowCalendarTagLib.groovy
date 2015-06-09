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

import org.springframework.web.servlet.support.RequestContextUtils as RCU

/**
 * Renders Bootstrap calendar as date picker
 *
 * @author Maria Voitovich
 */

class GrailsflowCalendarTagLib {

    static namespace = "gf"

    def bootstrapCalendar = { attrs ->
     out << """
      <script type="text/javascript">
        jQuery.noConflict();
        jQuery(document).ready(function(\$){
            var datePickerDomElement = \$(document.getElementById("${attrs.property}"));
            var format = convertDatePatternFromJavaToJqueryDatePicker("${gf.datePattern()}")

            var options = {
                autoclose : true,
                todayHighlight: true,
                todayBtn: 'linked',
                format: format,
                language:"${RCU.getLocale(request)}",
                clearBtn: true
            };
            var dataPickerOptions = jQuery.extend(options, {});

            datePickerDomElement.parent().datepicker(dataPickerOptions);
        });
        </script>
         <div class='input-group date' >
           <input type='text' size="${attrs.size ?: 20}" class="form-control" id="${attrs.property}" name="${attrs.property}" value = "${(attrs.value instanceof Date) ? gf.displayDate(value: attrs.value) : (attrs.value ?: '')}">
           <span class="input-group-addon">
            <span class="glyphicon glyphicon-calendar"></span>
           </span>
         </div>
       """

    }


    /**
     * Renders range datepicker
     *
     * parameters:
     * - fromId  property name, used as id/name for 'from' datepicker
     * - toId  property name, used as id/name for 'to' datepicker
     * - fromLabel  optional, by default 'from' is used
     * - toLabel  optional, by default 'to' is used
     * - fromValue  optional, by default 'from' is used
     * - toValue  optional, by default 'to' is used
     * - fromName  optional, by default the fromId value is used
     * - toName  optional, by default the toId value is used
     *
     */
    def dateRangePicker = { attrs ->
      out << """
      <script type="text/javascript">
        jQuery.noConflict();
        jQuery(document).ready(function(\$){
                var elFrom = document.getElementById("${attrs.fromId}");
                var elTo =document.getElementById("${attrs.toId}");

                //After the 'from' date was selected we need to focus on 'to' field.
                var onChangeDateHandler = function(e){
                    if(e.target == elFrom && !\$(elFrom).is(':focus')){
                        \$(elTo).focus();
                    }
                };
                var format = convertDatePatternFromJavaToJqueryDatePicker("${gf.datePattern()}")
                var datepickerProps = {"autoclose":true,"todayHighlight":true,"todayBtn":"linked","clearBtn":true,
                "format": format,"language":"${RCU.getLocale(request)}"};

                \$(elFrom).parent().datepicker(datepickerProps).on('changeDate', onChangeDateHandler);
            });
        </script>
        <div class="input-daterange input-group">
           <input type="text" id="${attrs.fromId}" placeholder="${attrs.fromLabel ?: 'from'}" class="form-control" value="${attrs.fromValue ?: ''}" name="${attrs.fromName ?: attrs.fromId}">
           <span class="input-group-addon"> — </span>
           <input id="${attrs.toId}" class="form-control" type="text" placeholder="${attrs.toLabel ?: 'to'}" value="${attrs.toValue ?: ''}"  name="${attrs.toName ?: attrs.toId}">
        </div>
       """

    }

}




