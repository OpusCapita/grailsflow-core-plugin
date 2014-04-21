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

import java.text.Collator; 
import java.util.Arrays; 
import java.util.Locale;

/**
 * Copied form org.apache.axis.utils.JavaUtils (axis/axis/1.4)
 *
 * Original sources:
 * @author Glen Daniels (gdaniels@apache.org) 
 */
public class JavaUtils {
  /*
   * These are java keywords as specified at the following URL (sorted alphabetically). 
   * http://java.sun.com/docs/books/jls/second_edition/html/lexical.doc.html#229308 
   * Note that false, true, and null are not strictly keywords; they are literal values, 
   * but for the purposes of this array, they can be treated as literals. 
   * PLEASE KEEP THIS LIST SORTED IN ASCENDING ORDER ******
   */
    public static final String keywords[] = { 
        "abstract",  "assert",       "boolean",    "break",      "byte",      "case", 
        "catch",     "char",         "class",      "const",     "continue", 
        "default",   "do",           "double",     "else",      "extends", 
        "false",     "final",        "finally",    "float",     "for", 
        "goto",      "if",           "implements", "import",    "instanceof", 
        "int",       "interface",    "long",       "native",    "new", 
        "null",      "package",      "private",    "protected", "public", 
        "return",    "short",        "static",     "strictfp",  "super", 
        "switch",    "synchronized", "this",       "throw",     "throws", 
        "transient", "true",         "try",        "void",      "volatile", 
        "while" 
    }; 

    /* Collator for comparing the strings */
    public static final Collator englishCollator = Collator.getInstance(Locale.ENGLISH); 

    
    /**
     * isJavaId Returns true if the name is a valid java identifier.
     * Parameters:
     * id to check
     * Returns:
     * boolean true/false
     */
    public static boolean isJavaId(String id) { 
        if (id == null || id.equals("") || isJavaKeyword(id)) 
            return false; 
        if (!Character.isJavaIdentifierStart(id.charAt(0))) 
            return false; 
        for (int i=1; i<id.length(); i++) 
            if (!Character.isJavaIdentifierPart(id.charAt(i))) 
                return false; 
        return true; 
    } 

    
    /**
     * Checks if the input string is a valid java keyword.
     * Returns:
     * boolean true/false
     */
    public static boolean isJavaKeyword(String keyword) { 
      return (Arrays.binarySearch(keywords, keyword, englishCollator) >= 0); 
    } 

}