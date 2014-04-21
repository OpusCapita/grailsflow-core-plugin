package com.jcatalog.grailsflow.search

/**
 * Default bean for configuring display column for advanced search
 *
 * @author Maria Voitovich
 */
class DefaultDisplayParameter implements DisplayParameter {
  String name
  boolean sortable = true
  String displayProperty
  String displayTemplate

  public String getName() {
    return name
  }

  public boolean isSortable() {
    return sortable
  }

  public Object getDisplayValue(def searchResultItem) {
    if (!searchResultItem) return null;
    def value = searchResultItem
    displayProperty?.split("\\.").each{ String property ->
      value = value?."$property"
    }
    return value;
  }

  public String getDisplayTemplate() {
    return displayTemplate
  }
}
