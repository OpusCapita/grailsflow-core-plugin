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

package com.jcatalog.grailsflow.bean;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import com.jcatalog.grailsflow.utils.ClassUtils

import org.hibernate.Hibernate

/**
 * Base class for creating domain object's readonly proxy
 *
 * @author Maria Voitovich
 */
class BeanProxySupport extends GroovyObjectSupport {
  private Map<Collection<String>, Object> _delegates

  public static Map<Collection<String>, Object> createDelegatesMap() {
    return new HashMap<Collection<String>, Object>()
  }

  public BeanProxySupport(Map<Collection<String>, Object> delegates) {
    this._delegates = delegates
  }

  public BeanProxySupport(Object domainObject) {
    def delegates = BeanProxySupport.createDelegatesMap()

    def domainClass = Hibernate.getClass(domainObject)

    def properties = ClassUtils.getAllDomainClassProperties(domainClass)
    delegates.put(properties, domainObject)
    delegates.put(["ident()"], ["ident()" : domainObject.ident()])
    this._delegates = delegates
  }

  public Object getProperty(String name) {
    def metaProperty =  this.getClass().metaClass.getMetaProperty(name)
    if ( metaProperty != null) {
      return metaProperty.getProperty(this)
    }
    def key = _delegates?.keySet()?.find() { keys ->
      keys && keys.contains(name)
    }
    if (key) {
      def property = _delegates.get(key)?."$name"
      if (DomainClassArtefactHandler.isDomainClass(property.getClass())){
        return new BeanProxySupport(property)
      } else {
        return property
      }
    } else {
      throw new MissingPropertyException(name, this.getClass())
    }
  }

  public void getProperty(String name, Object value) {
    getProperty(name) // check for MissingPropertyException
    throw new ReadOnlyPropertyException(name, this.getClass())
  }

  public Object invokeMethod(String name, Object args) {
    def metaMethod = this.getClass().metaClass.getMetaMethod(name, args)
    if (metaMethod) {
      return metaMethod.invoke(delegate,args)
    } else if (name == "ident") {
      return getProperty("ident()")
    } else {
      throw MissingMethodException(name, this.getClass(), InvokerHelper.asList(args).toArray())
    }
  }

}
