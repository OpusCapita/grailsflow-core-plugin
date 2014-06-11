import com.jcatalog.grailsflow.search.SearchParameter
import com.jcatalog.grailsflow.search.DisplayParameter

/**
 * Taglib for supporting search UIs
 *
 * @author Maria Voitovich
 */
class GrailsflowAdvancedSearchTagLib {
  static namespace = "gf"


  def advancedSearchResources = {
    out << g.javascript(plugin:"grailsflow", src:"grailsflow/advancedSearch/searchCriterion.js")
    out << g.javascript(plugin:"grailsflow", src:"grailsflow/advancedSearch/searchItem.js")
    out << g.javascript(plugin:"grailsflow", src:"grailsflow/advancedSearch/search.js")
    out << g.javascript(plugin:"grailsflow", src:"grailsflow/advancedSearch/searchTable.js")

    out << g.javascript() {
             """
             function addCriterionToSearchTable(searchTable, id, label) {
                var div = document.getElementById(id);
                div.parentNode.removeChild(div);
                var inputElements = [];
                for (var i=0; i<div.childNodes.length; ++i) {
                  // get only element nodes
                  if(div.childNodes[i].nodeType == 1) {
                    inputElements.push(div.childNodes[i]);
                  }
                }
                searchTable.addSearchCriterion(id, label, inputElements);
             } """
           }
  }

  /**
   * Binds advanced search to the HTML table.
   *
   * Tag attributes:
   *  - name    - parameter name to store selected criterions
   *  - tableId - id of existing HTML table to bind advancedSearch to
   *  - searchParameters - List<SearchParameter>
   *  - bundle(optional) - bundle to take search parameters labels from
   *  - labelPrefix(optional) - prefix key for label in bundle,
   *  - labelSuffix(optional) - suffix key for label in bundle,
   *
   */
  def advancedSearch = { attrs ->
    String name = attrs?.name
    if (!name) {
      throwTagError("Tag [advancedSearch] is missing required attribute [name]");
    }

    String tableId = attrs?.tableId
    if (!tableId) {
      throwTagError("Tag [advancedSearch] is missing required attribute [tableId]");
    }

    List<SearchParameter> searchParameters = attrs?.searchParameters
    if (!searchParameters) {
      throwTagError("Tag [advancedSearch] is missing required attribute [searchParameters]");
    }

    def bundle = attrs?.bundle
    def labelPrefix = attrs?.labelPrefix ?: ''
    def labelSuffix = attrs?.labelSuffix ?: ''

    out << g.javascript() {
        """
          var addIcon = document.createElement("img");
          addIcon.alt = "add";
          addIcon.src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/editor',file:'add.gif')}"

          var deleteIcon = document.createElement("img");
          deleteIcon.alt = "delete";
          deleteIcon.src="${g.resource(plugin: 'grailsflow', dir:'images/grailsflow/editor',file:'delete.gif')}"

          var searchTable = new SearchTable("search_table", "${name}");
          searchTable.setIcons(addIcon, deleteIcon);
        """
    }
    searchParameters.each() { searchParameter ->
      def searchParameterName = searchParameter.name
      def searchParameterValue = searchParameter.getSearchValue(params)
      def defaultInput = """<input type="text" name="${searchParameterName}" value="${searchParameterValue ?: ''}" />"""

      out << """<div id="${searchParameter.name}">"""
      out << gf.customizingTemplate(template: searchParameter.searchTemplate,
              model: [name: searchParameterName, value: searchParameterValue],
              notFoundMessage: defaultInput)
      out << """</div>"""
      def label = bundle ? bundle["${labelPrefix}${searchParameter.name}${labelSuffix}".toString()] : searchParameter.name
      out << g.javascript() {
        """
          addCriterionToSearchTable(searchTable, "${searchParameter.name}", "${label}");
        """
      }
    }
    def criterias = [params[name]].flatten() ?: [ searchParameters[0].name ]
    criterias.each() { criteria ->
      out << g.javascript() {"searchTable.addCriteriaRow('${criteria}');"}
    }
  }

  /**
   * - displayParameters - List of DisplayParameter objects
   * - bundle(optional)
   * - labelPrefix(optional) - prefix key for label in bundle,
   * - labelSuffix(optional) - suffix key for label in bundle,
   *
   */
  def displayHeaders = { attrs ->
    List<DisplayParameter> displayParameters = attrs?.displayParameters
    if (!displayParameters) {
      throwTagError("Tag [displayHeaders] is missing required attribute [displayParameters]");
    }

    def bundle = attrs?.bundle
    def labelPrefix = attrs?.labelPrefix ?: ''
    def labelSuffix = attrs?.labelSuffix ?: ''

    displayParameters.each() { displayParameter ->
      def label = bundle ? bundle["${labelPrefix}${displayParameter.name}${labelSuffix}".toString()] : displayParameter.name

      if (displayParameter.sortable) {
        out << gf.sortableColumn(controller: gf.currentController(), action: gf.currentAction(), params: gf.currentParams(),
            property: displayParameter.name, title: label)
      } else {
        out << "<th>${label}</th>"
      }

    }
  }

  /**
   * - resultItem       - object
   * - displayParameters - List of DisplayParameter objects
   */
  def displayCells = { attrs ->

    def resultItem = attrs?.resultItem
    if (!resultItem) {
      throwTagError("Tag [displayCells] is missing required attribute [resultItem]");
    }

    List<DisplayParameter> displayParameters = attrs?.displayParameters
    if (!displayParameters) {
      throwTagError("Tag [displayCells] is missing required attribute [displayParameters]");
    }

    displayParameters.each() { displayParameter ->
      def value = displayParameter.getDisplayValue(resultItem);
      def defaultOutput = value?.toString() ?: '&nbsp;';

      out << "<td>"
      out << gf.customizingTemplate(template : displayParameter.displayTemplate,
          model: [value: value],  notFoundMessage: defaultOutput)
      out << "</td>"
    }

  }

}
