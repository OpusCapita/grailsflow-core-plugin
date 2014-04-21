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

/**
 * Process variable def describes a variable for the process.
 * ProcessVarDefListItem class describes the definition of variable item (of List type).
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class ProcessVarDefListItem {
    String content

    static belongsTo = [ processVariableDef: ProcessVariableDef ]

    static constraints = {
        content(nullable:true, maxSize:2000)
    }


    static transients = [ "linkValue"]

    public Link getLinkValue() {
        def o
        if (!content) {
            o = new Link()
        } else {
            GroovyShell gs = new GroovyShell()
            def properties = gs.evaluate(content)

            if (properties instanceof Map) {
                o = new Link(path: properties.path, description: properties.description)
            } else o = new Link()
        }
        return o
    }
}
