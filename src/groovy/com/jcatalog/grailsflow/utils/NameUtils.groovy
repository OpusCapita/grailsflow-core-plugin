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
package com.jcatalog.grailsflow.utils;

import org.apache.commons.lang.StringUtils


class NameUtils {

    def static upCase(String string) {
        return StringUtils.capitalize(string) 
    }

    def static downCase(String string) {
        return StringUtils.uncapitalize(string)
    }

    // the function will return true if parameter 's' is
    // a legal Groovy identifier in process script definition;
    // added restriction for length (255 characters)
    def static isValidIdentifier(String s) {
        if (!JavaUtils.isJavaId(s) || !(s ==~ /[A-Za-z_][A-Za-z0-9_]*/)
            || (s.length() == 0 || s.length() > 255)) {
            return false
        }
        return true
    }

    // the function will return true if parameter 's' is
    // a legal Groovy class name;
    // added restriction for length (255 characters)
    def static isValidProcessName(String s) {
        if (JavaUtils.isJavaKeyword(s) || !(s ==~ /[A-Za-z][A-Za-z0-9_]*/)
            || (s.length() == 0 || s.length() > 255)) {
            return false
        }
        
        return true
    }
}