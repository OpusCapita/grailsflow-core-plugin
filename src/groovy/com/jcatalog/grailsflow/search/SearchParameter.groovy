package com.jcatalog.grailsflow.search

/**
 * Interface for supporting configurable search UI
 *
 * @author Maria Voitovich
 */
public interface SearchParameter {

  public String getName()

  /**
   * GSP template that renders input for search parameter value.
   *
   * Template parameters:
   *  - name   -- search property name
   *  - value  -- search value from current request parameters
   *
   * @return path to GSP template
   */
  public String getSearchTemplate()

  /**
   * Retrieves search parameters value form current request.
   * Typically returns params[name],
   * but may have other implementation for dates, lists, etc
   *
   * @param params  -- request parameters
   *
   * @return search parameter value
   */
  public Object getSearchValue(def params)

}