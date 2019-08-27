package com.jcatalog.grailsflow.format

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
 * Stores format patterns for different locales
 */
class DefaultGrailsflowFormatPatternsProvider implements GrailsflowFormatPatternsProvider {

    Map<String, String> datePatterns

    Map<String, String> dateTimePatterns

    Map<String, String> numberPatterns

    Map<String, String> decimalSeparators

    DefaultGrailsflowFormatPatternsProvider() {
        datePatterns = [en: 'MM/dd/yyyy', de: 'dd.MM.yyyy']
        dateTimePatterns = [en: 'MM/dd/yy HH:mm', de: 'dd.MM.yy HH:mm']
        numberPatterns = [en: '0.00', de: '0.00']
        decimalSeparators = [en: '.', de: ',']
    }
}