package com.jcatalog.grailsflow.search

/**
 * Default bean for configuring search parameter for advanced search
 *
 * @author Maria Voitovich
 */
class DefaultSearchParameter implements SearchParameter {
  String name
  String searchTemplate

  public String getName() {
    return name
  }

  public String getSearchTemplate() {
    return searchTemplate
  }

  public Object getSearchValue(def params) {
    return params[getName()]
  }

}
