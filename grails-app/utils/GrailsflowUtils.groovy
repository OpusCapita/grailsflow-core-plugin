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
                throw e
            }
            return date
        } else return null
    }

    /**
     *  Returns the given date with the time set to the start of the day.
     */
    def static Date getStartOfDate(Date date){
        if (date == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * Returns the given date with time set to the end of the day.
     */
    def static Date getEndOfDate(Date date) {
        if (date == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

}