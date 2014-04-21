package com.jcatalog.grailsflow.search

/**
 * Interface for supporting configurable UI for showing search results
 *
 * @author Maria Voitovich
 */
public interface DisplayParameter {

  /**
   * Name of the display parameter.
   * Name is used as column label.
   * If DisplayParameter is sortable and then name is also used as value for sort parameter
   *
   * @return name
   */

  public String getName()

  /**
   * Indicates whether search results can be sorted by this parameter
   * @return
   */
  public boolean isSortable()

  /**
   * GSP template that renders display parameter value.
   *
   * Template parameters:
   *  - value  -- display parameter value for current searchResultItem
   *
   * @return path to GSP template
   */
  public String getDisplayTemplate()

  /**
   *
   * @param searchResultItem
   * @return object to pass to displayTemplate (if specified)
   *         or to render on the UI (if displayTemplate is not specified)
   */
  public Object getDisplayValue(Object searchResultItem)

}