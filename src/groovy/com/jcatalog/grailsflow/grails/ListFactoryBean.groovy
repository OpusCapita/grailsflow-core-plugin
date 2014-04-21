package com.jcatalog.grailsflow.grails

import org.springframework.beans.factory.FactoryBean

/**
 *
 * @author Maria Voitovich
 */
class ListFactoryBean implements FactoryBean {
  List items = new ArrayList();

  public Object getObject() {
    return items;
  }

  public Class<?> getObjectType() {
    return List;
  }

  public boolean isSingleton() {
    return false;
  }

  public void setItems(List items){
    this.items = items;
  }
}
