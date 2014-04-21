import com.jcatalog.grailsflow.bean.NodeDetails
import com.jcatalog.grailsflow.search.DisplayParameter
import com.jcatalog.grailsflow.search.SearchParameter
import com.jcatalog.grailsflow.utils.AuthoritiesUtils

/**
 * Configurable worklist.
 *
 * @author Maria Voitovich
 */
class WorklistController extends GrailsFlowSecureController {
  static String DEFAULT_VIEW = "worklist";

  List<SearchParameter> worklistSearchParameters
  List<DisplayParameter> worklistDisplayParameters

  Integer maxResultSize

  def processManagerService
  def processWorklistService

  /**
   * Search and display action for worklist
   *
   * Supported search parameters:
   *   - query:
   *     -- vars.<varName>
   *
   *   - paging:
   *     -- sort  -- can be either ProcessNode property or vars.<variableName>
   *     -- order -- "asc" or "desc"
   *     -- max   -- max items per page
   *     -- offset  -- index of first item on the page
   *
   * Action can be called from other controllers/actions via forward().
   * In this case some data is taken form flash object:
   *  - flash.searchFilter -- map of name -> value parameters
   *                          that complements/overwrites request search parameters
   *  - flash.view  -- view that will be rendered.
   *                   If not specified then DEFAULT_VIEW is rendered
   *  - flash.searchParameters -- List of SearchParameter objects.
   *                              If not specified then worklistSearchParameters is user
   *  - flash.displayParameters -- List of DisplayParameter objects
   *                               If not specified then worklistDisplayParameters is user
   *
   */
   def index = {
       // define paging parameters
       def pagingParameters = getPagingParameters(params)
       if (!pagingParameters.sort) { pagingParameters.sort = "startedOn" }
       if (!pagingParameters.order) { pagingParameters.order = "desc" }

       def searchParametersList = getSearchParametersList();

       def searchParameters = getSearchParameters(searchParametersList, params);

       if (flash?.searchFilter) {
        applySearchFilter(searchParameters, flash.searchFilter)
       }

       def varsFilter = searchParameters.findAll() {key, value -> key.startsWith('vars.')}

       searchParameters -= varsFilter
       searchParameters.each() { key, value ->
         log.warn "Unknown search parameter ${key.inspect()} is ignored."
       }

       // get authorities
       def authorities = getUserAuthorities(session)

       // get size of list
       def itemsTotal = processWorklistService
                  .getWorklistSize(authorities, varsFilter)

       // get current page items
       def worklist = processWorklistService
                  .getWorklist(authorities, varsFilter,
               pagingParameters.sort, pagingParameters.order, pagingParameters.max, pagingParameters.offset)

       def displayParametersList = getDisplayParametersList()

       def nodeDetailsList = []
       worklist?.each() { processNode ->
         def process = processNode.process
         def processInstance = processManagerService.getRunningProcessInstance(process.id)
         nodeDetailsList << new NodeDetails(processNode, processInstance)
       }

       render(view: flash?.view ?: DEFAULT_VIEW,
              model: [nodeDetailsList: nodeDetailsList,
                      itemsTotal: itemsTotal ? itemsTotal : 0,
                      searchParameters:  searchParametersList,
                      displayParameters: displayParametersList])
   }

   private List<SearchParameter> getSearchParametersList() {
     return flash?.searchParameters ?: worklistSearchParameters
   }

   private List<SearchParameter> getDisplayParametersList() {
     return flash?.displayParameters ?: worklistDisplayParameters
   }

   private Map<String, Object> getSearchParameters(List<SearchParameter> searchParametersList, def params) {
     def searchParameters = [:]
     searchParametersList?.each() { searchParameter ->
       searchParameters[searchParameter.name] = searchParameter.getSearchValue(params)
     }
     return searchParameters;
   }

   private void applySearchFilter(def searchParameters, searchFilter) {
     searchFilter?.each() { key, value ->
       searchParameters[key] = value;
     }
   }

   private Map<String, Object> getPagingParameters(def params) {
     def pagingParameters = [:]
     pagingParameters.sort = params.sort
     pagingParameters.order = params.order
     if (params.max){
       pagingParameters.max = params.max.toInteger()
     } else {
       pagingParameters.max = maxResultSize
     }
     if (params.offset) {
       pagingParameters.offset = params.offset.toInteger()
     } else {
       pagingParameters.offset = 0
     }
     return pagingParameters;
   }


}
