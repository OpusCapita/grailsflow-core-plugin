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
package com.jcatalog.grailsflow.model.definition

import com.jcatalog.grailsflow.process.Link

import com.jcatalog.grailsflow.model.view.VariableView

import org.hibernate.type.YesNoType

/**
 * Process Variable Definition class
 *
 * Describes the process variable definition from UI
 *
 * Process variable definition has type, variable name and value fields.
 * When the process variable definition is created there is a possibility
 * to define default value for process variable.
 * It will be stored in process definition like
 * " ...
 *   public String foo = 'Some text'
 *   ... "
 * Where 'Some text' is a default value for 'foo' variable.
 * The value may be a constant value or a piece of code that
 * will be executed during parsing process definition and initializing
 * process variables with start values.
 *
 * For example:
 * " class TestProcess extends ...{
 *       ...
 *       String exampleString = "abc"
 *       Integer exampleInt = MAX_COUNT + (12 +14 * 26)
 *       String e2String = "abc" + "def"
 *       ...
 *   } "
 * 	then we load and parse TestProcess class and create an instance of it,
 *  then with reflection we will store process variables in DB.
 *  E.g we will have a ProcessVariableDef with
 *    type  =  Integer
 *    name  = exampleInt
 *    value = 23556
 *
 * @author Stephan Albers
 * @author July Karpey
 * @author Maria Voitovich
 */
class ProcessVariableDef {
    String type

    // this property is filled when type of variable is List,
    // it is a type of List elements
    String subType

    String name
    String defaultValue
    Boolean isProcessIdentifier = Boolean.FALSE
    Boolean required = Boolean.FALSE

    Map label = [:]
    
    // BAD HACK: generated table name 'process_variable_def_description' is too long
    // "description joinTable:[name:'process_variable_def_desc']" did not work in grails 1.1.1
    // that's why we use property "desc" and 'proxify' its usage with transients property "description"
    // Rename property to description properties when joinTable[name:""] will work
    Map desc = [:]
    Map getDescription(){
      return this.desc
    }
    void setDescription(Map desc) {
      this.desc = desc
    }

    VariableView variableView

    static belongsTo = [ processDef: ProcessDef ]

    static hasMany = [items: ProcessVarDefListItem, variable2NodesVisibility: Variable2NodeVisibility ]

    static mappedBy = [ variable2NodesVisibility: "variable" ]
 
    //Set constraints
    static constraints = {
        name(unique: 'processDef')
        defaultValue(nullable:true)
        variableView(nullable:true)
        subType(nullable:true)
    }

    static mapping = {
        columns {
            isProcessIdentifier type:YesNoType
            required type:YesNoType
        }
        label indexColumn:[name:"language", type:String, length:2],joinTable:[key:'process_variable_def_id', column:'label'],length:255
        // TODO: enable when table name generation will work
        //description indexColumn:[name:"language", type:String, length:2],joinTable:[name:'process_variable_def_desc', key:'process_variable_def_id', column:'description'],length:255
        desc indexColumn:[name:"language", type:String, length:2],joinTable:[key:'process_variable_def_id', column:'description'],length:255
        variable2NodesVisibility cascade: "all,delete-orphan"
        items cascade: "all,delete-orphan"
    }
    
    static transients = [ "suitableValue" , "view", "description", "value"]

    /**
	   * Use this method instead of getVariableView() in your code.
	   */ 
	  VariableView getView(){
      return this.variableView
    }


	  /**
	   * IMPORTANT: use this method instead of setVariableView() in your code
	   * for correct updating of view properties or deleting of previous view value
	   */ 
	  void setView(VariableView view){
	    if (this.variableView?.id != null) { // if variable already has persistent view
	      def oldView = this.variableView
	      if (view!= null && oldView.type == view.type) { // if view type doesn't change then simply update its properties
	        this.variableView.mergeChanges(view)
	        return
	      } else {  // if we change view type then delete old view
		      if (view != null) {
		        view.variable = this
		      }
          this.variableView = view
		      oldView.delete()
	      }
	    } else { // if variable doesn't have persistent view
        if (view != null) {
          view.variable = this
        }
	      this.variableView = view
	    }
	  }

	  void removeFromAssociations() {
	    this.variable2NodesVisibility?.each() {
	      if (it.node) {
	        it.node.removeFromVariables2NodeVisibility(it)
	      }
	    }
	    this.variable2NodesVisibility?.clear()

      this.save(flush: true)
      this.processDef?.removeFromVariables(this)
	  }

    public static List getTypes(){
        return ["Boolean","Double","String","Long","Integer","Date","Object","Document","Link","List"]
    }

    public static List getListTypes(){
        return ["Boolean","Double","String","Long","Integer","Date","Link"]
    }

    public def getLinkValue() {
        def o
        if (!defaultValue) {
            o = new Link()
        } else {
            GroovyShell gs = new GroovyShell()
            def properties = gs.evaluate(defaultValue)

            if (properties instanceof Map) {
                o = new Link(path: properties.path, description: properties.description)
            } else o = new Link()
        }
        return o
    }

    public String toString() {
        if (type.equals("Date")){
            return "new Date("+defaultValue+")"
        } else if (type.equals("Object")) {
            return "defaultValue.inspect()"
        } else if (type.equals("Link")) {
            return "new Link(path: '${linkValue.path}', description: '${linkValue.description}')"
        } else if (type.equals("Boolean")) {
            if (defaultValue.equals("true")) {
                return "new Boolean('true')"
            }
          else return "new Boolean('false')"
        } else if (type.equals("List")) {
            List values = []
            ProcessVariableDef tempVariable = new ProcessVariableDef()
            items?.each() {
                tempVariable.defaultValue = it.content
                tempVariable.type = this.subType
                values << tempVariable.toString()
            }
            return values.toString()
        } else {
            return "new ${type}(${defaultValue.inspect()})"
        }
    }

    public boolean isSuitableValue() {
        if (defaultValue) {
            try {
                if (type.equals("Integer")){
                    Integer.parseInt(defaultValue)
                }
                if (type.equals("Long") || type.equals("Date")){
                    Long.parseLong(defaultValue)
                }
                if (type.equals("Double")){
                    Double.parseDouble(defaultValue)
                }
                return true
            } catch (NumberFormatException e) {
                return false
            }
        } else if (items) {
            ProcessVariableDef tempVariable = new ProcessVariableDef()
            tempVariable.type = this.subType
            items?.each() {
                tempVariable.defaultValue = it.content
                if (!tempVariable.isSuitableValue()) { return false }
            }
        } else {
            return true
        }
    }

    public setValue(Object value) {
        if (value instanceof List) {
            this.items?.clear()
            value?.each() {
                String tempValue
                if (it.class.simpleName == 'Date') {
                    tempValue = it.time
                } else {
                    tempValue = it.toString()
                }
                this.subType = this.subType ?: it.class.simpleName
                this.addToItems(new ProcessVarDefListItem(content: tempValue))
            }
        } else if (value instanceof Date){
            this.defaultValue = value?.time
        } else {
            this.defaultValue = value?.toString()
        }
    }

    public Object getValue() {
        return (type == 'List' && items) ? this.items*.content?.join(",") : defaultValue
    }

}
