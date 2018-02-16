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

import com.jcatalog.grailsflow.process.Link
import org.apache.commons.lang.builder.CompareToBuilder

/**
 * Process variable describes a variable for the process.
 * ProcessVarListItems class describes the variable item of List type.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class ProcessVarListItem implements Comparable {
    String content

    static belongsTo = [ processVariable: ProcessVariable ]

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

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ProcessVarListItem that = (ProcessVarListItem) o

        if (id == null) return GroovyObjectSupport.equals(that)
        if (id != that.id) return false

        return true
    }

    int hashCode() {
        if (id == null) {
            return GroovyObjectSupport.hashCode()
        }
        return id.hashCode()
    }

    @Override
    int compareTo(Object o) {
        ProcessVarListItem listItem = (ProcessVarListItem) o
        return new CompareToBuilder().append(this.id, listItem.id).append(this.content, listItem.content)
                .append(this.hashCode(), listItem.hashCode()).toComparison()
    }
}
