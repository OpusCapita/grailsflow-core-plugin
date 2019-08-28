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
 * Provides different kinds of data formats that are configured
 * via grailsApplication.config.grailsflow.format.* closures:
 * - datePatterns - closure that returns Map where key is locale tag (String) and value is date pattern (String)
 * - dateTimePatterns - closure that returns Map where key is locale tag (String) and value is date & time pattern (String)
 * - numberPatterns - closure that returns Map where key is locale tag (String) and value is number pattern (String)
 * - decimalSeparators - closure that returns Map where key is locale tag (String) and value is decimal separator (String)
 *  Each closure can access 'applicationContext' that is part of closure execution binding
 */
class GrailsflowFormatPatternsService {
    boolean transactional = false
    def grailsApplication

    static defaultDatePatterns = [en: 'MM/dd/yyyy', de: 'dd.MM.yyyy']
    static defaultDateTimePatterns = [en: 'MM/dd/yy HH:mm', de: 'dd.MM.yy HH:mm']
    static defaultNumberPatterns = [en: '0.00', de: '0.00']
    static defaultDecimalSeparators = [en: '.', de: ',']

    Map<String, String> getDatePatterns() {
        return processConfiguration('datePatterns', defaultDatePatterns)
    }

    Map<String, String> getDateTimePatterns() {
        return processConfiguration('dateTimePatterns', defaultDateTimePatterns)
    }

    Map<String, String> getNumberPatterns() {
        return processConfiguration('numberPatterns', defaultNumberPatterns)
    }

    Map<String, String> getDecimalSeparators() {
        return processConfiguration('decimalSeparators', defaultDecimalSeparators)
    }

    private def processConfiguration(String name, defaultResult) {
        def closure = grailsApplication.config.grailsflow.format[name]
        if (closure) {
            // we don't touch/modify original closure but clone it
            def clone = closure.clone()
            // inject 'applicationContext' so it is available inside closure
            clone.delegate = ['applicationContext': grailsApplication.applicationContext]
            return clone()
        }
        return defaultResult
    }
}