package com.jcatalog.grailsflow.search

/**
 * SearchParameter for Date fields
 *
 * @author Maria Voitovich
 */
class DateSearchParameter extends DefaultSearchParameter {

  public Object getSearchValue(def params) {
    Date date = GrailsflowRequestUtils.getDateFromParams(params, getName())
    return date
  }

}
