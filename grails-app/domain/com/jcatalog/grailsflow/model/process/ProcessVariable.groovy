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
package com.jcatalog.grailsflow.model.process

import com.jcatalog.grailsflow.process.Document
import com.jcatalog.grailsflow.process.Link

import org.apache.commons.lang.StringUtils
import org.hibernate.type.YesNoType
/**
 * Process variable describes a variable for the process.
 * The variable has name and value, value can contains information
 * of different types. The type definition is stored in field 'type'
 * as INT value. According to value of 'type' the variable value
 * can be defined and received.
 *
 * The following values of 'type' are supported:
 *    type = 1 - the value of type Boolean
 *    type = 2 - the value of type Double
 *    type = 3 - the value of type String
 *    type = 4 - the value of type Long
 *    type = 5 - the value of type Integer
 *    type = 6 - the value of type Date
 *    type = 7 - the value of type different from previous,
 *               but instance of Object class
 *    type = 8 - the value of type Document
 *    type = 9 - the value of type Link
 *    type = 0 - the type is not supported
 *               or the value of variable is null
 *
 * TODO: We need to discuss, if we need to store the variable type definition in
 * an additional table of if we just store the values, like we do it right
 * now and read the type definition from the process class
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class ProcessVariable {
    String name
    String variableValue

    Boolean isProcessIdentifier = Boolean.FALSE
    String typeName

    // this property is filled when type of variable is List,
    // it is a type of List elements
    String subTypeName
    int type

    static hasMany = [items: ProcessVarListItem]
    static belongsTo = [ process: BasicProcess ]
    
    static int BOOLEAN = 1
    static int DOUBLE = 2
    static int STRING = 3
    static int LONG = 4
    static int INTEGER = 5
    static int DATE = 6
    static int OBJECT = 7
    static int DOCUMENT = 8
    static int LINK = 9
    static int LIST = 10

    static constraints = {
        name(unique: 'process')
        variableValue(nullable:true, maxSize:2000)
        typeName(nullable:true)
        subTypeName(nullable:true)
    }
    
    static mapping = {
        name index: 'IDX_PROCESS_VARIABLE_2'
        columns {
            isProcessIdentifier type:YesNoType
        }
        items cascade: "all,delete-orphan"
    }

    static transients = [ "value", "isValueIdentifier"]

    public static Map getTypes(){
        return [BOOLEAN: "Boolean",
                DOUBLE: "Double",
                STRING: "String",
                LONG: "Long",
                INTEGER: "Integer",
                DATE: "Date",
                OBJECT: "Object",
                DOCUMENT: "Document",
                LINK: "Link",
                LIST: "List"]
    }

    public void setValue(Object obj) {
        if (obj == null) {
            variableValue = null
        } else if (obj instanceof Boolean) {
            variableValue = obj.toString()
        } else if (obj instanceof Double
            || obj instanceof BigDecimal) {
            variableValue = obj.toString()
        } else if (obj instanceof String) {
            variableValue = obj
        } else if (obj instanceof Long) {
            variableValue = obj.toString()
        } else if (obj instanceof Integer) {
            variableValue = obj.toString()
        } else if (obj instanceof Date) {
            variableValue = ((Date)obj).getTime().toString()
        } else if (obj instanceof Document) {
            variableValue =  obj.documentUrl
        } else if (obj instanceof Link) {
            def string = ""

            if (obj.path) {
                string = string + "'path':'"+obj.path+"'"
                if (obj.description) string = string + ","
            }
            if (obj.description) string = string + "'description':'"+obj.description+"'"

            variableValue = "[ $string ]"
        } else if (obj instanceof List) {
            items?.clear()
            ProcessVariable tempVariable = new ProcessVariable()
            obj.collect {
                tempVariable.setValue(it)
                this.addToItems(new ProcessVarListItem(content: tempVariable.variableValue))
            }
        } else if (obj instanceof Object) {
            if (obj.getClass().simpleName != 'Object') {
                // try to get object value from DB
                try {
                    typeName = obj.getClass().name
                    if (obj.ident()) {
                        variableValue = obj.ident().toString()
                        type = ProcessVariable.defineType(obj.ident().getClass())
                    }
                } catch (Exception e) {
                    println("Error while getting identity of object $e")
                }
            } else if (obj != null) {
                println("type " + type + " not supported for VALUE  = "+obj)
            }
        }
    }

    public Object getValue() {
        def o = getConvertedValue(variableValue, type, ProcessVariable.get(this.id))
        def returnedValue = o
        if (isValueIdentifier(typeName)) {
            try {
                def domainClass = getClass().getClassLoader().loadClass(typeName, false)

                if (domainClass && domainClass.list(max:1)) {
                    def ident = domainClass.list(max:1).get(0).ident()
                    type = ProcessVariable.defineType(ident.getClass())
                    o = getConvertedValue(variableValue, type, this)
                    returnedValue = domainClass.get(o)
                }
            } catch (Exception e) {
                println("Exception occured in getting by key of type '$type': $e")
                returnedValue = o
            }
        }

        return returnedValue
    }

    public static Object getConvertedValue(def value, def type) {
        return getConvertedValue(value, type, null)
    }

    public static Object getConvertedValue(def value, def type, ProcessVariable variable) {
        if ((!value && type != LIST) || (value == 'null')) {
            return null;
        }
        if (!(type instanceof Integer) ) {
            type = defineType(type)
        }
        try {
	        switch (type) {
	            case BOOLEAN:
	                return new Boolean(value)
	            case DATE:
	                return new Date(Long.parseLong(value))
	            case DOUBLE:
	                return new Double(value)
	            case INTEGER:
	                return new Integer(value)
	            case LONG:
	                return new Long(value)
	            case STRING:
	                return value
	            case DOCUMENT:
	                return (value != 'null') ? new Document(documentUrl: value) : null
	            case LINK:
	                if (value == 'null') {
	                    return null
	                } else {
	                    GroovyShell gs = new GroovyShell()
	                    def properties = gs.evaluate(value)
	
	                    if (properties instanceof Map) {
	                        return new Link(path: properties.path, description: properties.description)
	                    } else {
	                        return new Link()
	                    }
	                }
                case LIST:
                    if (variable) {
                        Set<ProcessVarListItem> items = variable?.items
                        List values = []
                        items?.each() {
                            values << getConvertedValue(it.content, variable?.subTypeName, variable)
                        }
                        return values
                    }
	            case OBJECT:
	                return value
	            default:
	                return value
	        }
        } catch (Exception e) {
             throw e
        }
        return null      
    }

    public static boolean isValueIdentifier(className) {
        if (!className) return false
        def type
        if (className.contains(".")) {
            type = StringUtils.substringAfterLast(className, ".")
        } else {
            type = className
        }
        if (getTypes().containsValue(type) ) {
            return false
        } else return true
    }


    public static int defineType(def fullClassName) {
        int type = 0
        if (!fullClassName) {
            return type
        }

        if (fullClassName instanceof Class){
          fullClassName = fullClassName.simpleName
        }

        def classValue
        if (fullClassName.contains(".")) {
            classValue = StringUtils.substringAfterLast(fullClassName, ".")
        } else {
            classValue = fullClassName
        }
        if (!classValue)  {
            return type
        }

        if (classValue.equals("Boolean")) {
            return BOOLEAN
        } else if (classValue.equals("Long")) {
            return LONG
        } else if (classValue.equals("Double")) {
            return DOUBLE
        } else if (classValue.equals("String")) {
            return STRING
        } else if (classValue.equals("Date")) {
            return DATE
        } else if (classValue.equals("Integer")) {
            return INTEGER
        } else if (classValue.equals("Object")) {
            return OBJECT
        } else if (classValue.equals("Document")) {
            return DOCUMENT
        } else if (classValue.equals("Link")) {
            return LINK
        }  else if (classValue.indexOf("List") != -1) {
            return LIST
        }
        return type
    }
}
