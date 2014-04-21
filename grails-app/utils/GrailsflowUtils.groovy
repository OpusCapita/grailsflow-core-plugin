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
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.text.ParseException

import org.springframework.web.servlet.support.RequestContextUtils as RCU
import org.springframework.web.context.request.RequestContextHolder

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory


/**
 * Helper methods 
 *
 * @author July Antonicheva
 */  
class GrailsflowUtils {
    static protected Log log = LogFactory.getLog(getClass())

    /**
     * Creates date from dateString using pattern
     */
    def static Date getParsedDate(String dateString, String pattern) {
        if (dateString) {
            if (!pattern) {
              def locale = RCU.getLocale(RequestContextHolder.requestAttributes.request)
              def format = DateFormat.getDateInstance(DateFormat.SHORT, locale)
              if (format && format instanceof SimpleDateFormat) {
                pattern = format.toPattern()
              }
            }
            def date
            def sdf = new SimpleDateFormat(pattern)
            try {
                date = sdf.parse(dateString.toString())
            } catch (ParseException e){
                log.error("Cannot convert ${dateString} to date.", e)
                return null
            }
            return date
        } else return null
    }

}