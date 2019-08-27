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

import com.jcatalog.grailsflow.format.GrailsflowFormatPatternsProvider

import java.text.DateFormat
import java.text.SimpleDateFormat

import java.text.NumberFormat 
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

import org.springframework.web.servlet.support.RequestContextUtils as RCU

/**
 * Format date and number instances. 
 */
class GrailsflowFormatTagLib {

    static namespace = "gf"

    GrailsflowFormatPatternsProvider grailsflowFormatPatternsProvider

    def defaultLocale

    /**
     * Displays formatted Date as date.
     *
     * parameters:
     * - value       Date to display
     * - pattern     format pattern. Optional. If not specified then datePattern() value is used
     */
    def displayDate = { attrs ->
      out << dateToString(attrs.value, attrs.pattern ? attrs.pattern : datePattern()?.toString() )
    }

    /**
     * Displays formatted Date as date and time.
     *
     * parameters:
     * - value       Date to display
     * - pattern     format pattern. Optional. If not specified then dateTimePattern() value is used
     */
    def displayDateTime = { attrs ->
      out << dateTimeToString(attrs.value, attrs.pattern ? attrs.pattern : dateTimePattern()?.toString() )
    }

    /**
     * Displays formatted Double.
     *
     * parameters:
     * - value                Double to display
     * - pattern              format pattern. Optional. If not specified then numberPattern() value is used
     * - decimalSeparator     separator between integer and fraction parts. Optional.
     *                                      If not specified then decimalSeparator() value is used
     */
    def displayDouble = { attrs ->
       out << numberToString(attrs.value, attrs.pattern ? attrs.pattern : numberPattern()?.toString(),
                     attrs.decimalSeparator ?  attr.decimalSeparator : decimalSeparator()?.toString())
    }

    /**
     * Returns Date format pattern configured in system for current locale.
     * Pattern are stored in applicationContext as datePatterns map,
     * where key is language and value is format pattern.
     * For example:
     *    ['en':'MM/dd/yyyy', 'de':'dd.MM.yyyy']
     *
     * If pattern for appropriate language is not specified then
     * use pattern for default locale.
     *
     * If default locale  is not specified or there is no pattern for default locale then
     * DateFormat.getDateInstance(DateFormat.SHORT, locale) will be used
     *
     * Tag parameters:
     * - locale      locale to get pattern for. optional
     */
    def datePattern = { attrs ->
      def locale = attrs.locale ? attrs.locale : RCU.getLocale(request)
      Map<String, String> datePatterns = grailsflowFormatPatternsProvider.datePatterns
      def pattern = datePatterns?.get(locale?.language)

      // if no pattern for specified locale, then use default
      if (pattern == null && defaultLocale) {
          pattern = datePatterns?.get(defaultLocale)
      }
      // if there is no default locale then use standard mechanism
      if (pattern == null) {
        def format = DateFormat.getDateInstance(DateFormat.SHORT, locale)
        if (format && format instanceof SimpleDateFormat) {
          pattern = format.toPattern()
        }
      }
      out << pattern
    }

    /**
     * Returns Date and time format pattern configured in system for current locale.
     * Pattern are stored in applicationContext as dateTimePatterns map,
     * where key is language and value is format pattern.
     * For example:
     *    ['en':'MM/dd/yy HH:mm', 'de':'dd.MM.yy HH:mm']
     *
     * If pattern for appropriate language is not specified then
     * use pattern for default locale.
     *
     * If default locale  is not specified or there is no pattern for default locale then
     * DateFormat.getDateInstance(DateFormat.SHORT, locale) will be used
     *
     * Tag parameters:
     * - locale      locale to get pattern for. optional
     */
    def dateTimePattern = { attrs ->
      def locale = attrs.locale ? attrs.locale : RCU.getLocale(request)
      Map<String, String> dateTimePatterns = grailsflowFormatPatternsProvider.dateTimePatterns
      def pattern = dateTimePatterns?.get(locale.language)

      // if no pattern for specified locale, then use default
      if (pattern == null && defaultLocale) {
          pattern = dateTimePatterns?.get(defaultLocale)
      }
      // if there is no default locale then use standard mechanism
      if (pattern == null) {
        def format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale)
        if (format && format instanceof SimpleDateFormat) {
          pattern = format.toPattern()
        }
      }
      out << pattern
    }

    /**
     * Returns Number format pattern configured in system for current locale.
     * Pattern are stored in applicationContext as numberPatterns map,
     * where key is language and value is format pattern.
     * For example:
     *    ['en':'0.00', 'de':'0.00']
     *
     * If pattern for appropriate language is not specified then
     * use pattern for default locale.
     *
     * If default locale  is not specified or there is no pattern for default locale then
     * DateFormat.getDateInstance(DateFormat.SHORT, locale) will be used
     *
     * Tag parameters:
     * - locale      locale to get pattern for. optional.
     */
    def numberPattern = { attrs ->
      def locale = attrs.locale ? attrs.locale : RCU.getLocale(request)
      Map<String, String> numberPatterns = grailsflowFormatPatternsProvider.numberPatterns
      def pattern = numberPatterns?.get(locale.language)
      // if no pattern for specified locale, then use default
      if (pattern == null && defaultLocale) {
          pattern = numberPatterns?.get(defaultLocale)
      }
      // if there is no default locale then use standard mechanism
      if (pattern == null) {
        def format = NumberFormat.getNumberInstance(locale)
        if (format && format instanceof SimpleDateFormat) {
          format.groupingSize = 0
          pattern = format.toPattern()
        }
      }
      out << pattern
    }

    /**
     * Returns decimal separator sign for Number format configured in system for current locale.
     * Pattern are stored in applicationContext as decimalSeparators map,
     * where key is language and value is separator sign.
     * For example:
     *    ['en':'.', 'de':',']
     *
     * If pattern for appropriate language is not specified then
     * use pattern for default locale.
     *
     * If default locale  is not specified or there is no pattern for default locale then
     * NumberFormat.getNumberInstance(locale).symbols.decimalSeparator will be used
     *
     * Tag parameters:
     * - locale      locale to get decimalSeparator for. optional.
     */
    def decimalSeparator = { attrs ->
      def locale = attrs.locale ? attrs.locale : RCU.getLocale(request)
      Map<String, String> decimalSeparators = grailsflowFormatPatternsProvider.decimalSeparators
      def decimalSeparator = decimalSeparators?.get(locale.language)

      // if no pattern for specified locale, then use default
      if (decimalSeparator == null && defaultLocale) {
          decimalSeparator = decimalSeparators?.get(defaultLocale)
      }
      // if there is no default locale then use standard mechanism
      if (decimalSeparator == null) {
        def format = NumberFormat.getNumberInstance(locale)
        if (format && format instanceof DecimalFormat) {
          decimalSeparator = format.symbols.decimalSeparator
        }
      }
      out << decimalSeparator
    }

    private def dateToString(Date date, String pattern) {
      if (!pattern) {
        pattern = 'MM/dd/yy'
      }
      date ? new SimpleDateFormat(pattern).format(date) : ''
    }

    private def dateTimeToString(Date date, String pattern) {
      if (!pattern) {
        pattern = 'MM/dd/yy HH:mm'
      }
      date ? new SimpleDateFormat(pattern).format(date) : ''
    }

    private def numberToString(def number, String pattern, String decimalSeparator) {
      def locale = RCU.getLocale(request)
      if (!pattern) {
        pattern = '0.00'
      }
      if (!decimalSeparator) {
        decimalSeparator = '.'
      }
      DecimalFormatSymbols symbols = new DecimalFormatSymbols()
      symbols.decimalSeparator = decimalSeparator.charAt(0)
      def df = new DecimalFormat(pattern, symbols)
      number ? df.format(number) : ''
  }

}