import com.jcatalog.grailsflow.bean.ProcessDetails
import com.jcatalog.grailsflow.search.SearchParameter
import com.jcatalog.grailsflow.search.DisplayParameter

/**
 * Configurable process list.
 *
 * @author Maria Voitovich
 */
class ProcessListController extends GrailsFlowSecureController {
  static String DEFAULT_VIEW = "processList";

  List<SearchParameter> processListSearchParameters
  List<DisplayParameter> processListDisplayParameters

  Integer maxResultSize

  def processManagerService
  def processWorklistService

  /**
   * Search and display action for processList
   *
   * Supported search parameters:
   *   - query:
   *     -- type
   *     -- status   -- statusID
   *     -- startedFrom  -- Date
   *     -- finishedFrom -- Date
   *     -- startUser
   *     -- vars.<varName>
   *
   *   - paging:
   *     -- sort  -- can be either ProcessNode property or var.<variableName>
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
   *                              If not specified then processListSearchParameters is user
   *  - flash.displayParameters -- List of DisplayParameter objects
   *                               If not specified then processListDisplayParameters is user
   *
   */
   def index = {
       // define paging parameters
       def pagingParameters = getPagingParameters(params)
       if (!pagingParameters.sort) { pagingParameters.sort = "createdOn" }
       if (!pagingParameters.order) { pagingParameters.order = "desc" }

       def searchParametersList = getSearchParametersList();

       def searchParameters = getSearchParameters(searchParametersList, params);

       if (flash?.searchFilter) {
        applySearchFilter(searchParameters, flash.searchFilter)
       }

       def processClasses = processManagerService.supportedProcessClasses

       def type = searchParameters.remove('type') ?: processClasses*.processType
       if (type instanceof Collection || type.class.isArray()) {
         type = type.join(",")
       }

       def status = searchParameters.remove('status');
       if (status != null && (status instanceof Collection || status.class.isArray())) {
         status = status.join(",")
       }

       def startedFromDate = searchParameters.remove('startedFrom');
       def finishedFromDate = searchParameters.remove('finishedFrom');

       def startUser = searchParameters.remove('startUser');

       def varsFilter = searchParameters.findAll() {key, value -> key.startsWith('vars.')}

       searchParameters -= varsFilter
       searchParameters.each() { key, value ->
         log.warn "Unknown search parameter ${key.inspect()} is ignored."
       }

       // get size of list
       def itemsTotal = processWorklistService
                  .getProcessListSize(type, status, startUser,
               startedFromDate, finishedFromDate, varsFilter)

       // get current page items
       def processList = processWorklistService
                  .getProcessList(type, status, startUser,
               startedFromDate, finishedFromDate, varsFilter,
               pagingParameters.sort, pagingParameters.order, pagingParameters.max, pagingParameters.offset)

       def displayParametersList = getDisplayParametersList()

       def processDetailsList = []
       processList?.each() { basicProcess ->
         def processClass = processClasses?.find() { it.processType == basicProcess.type }
         processDetailsList << new ProcessDetails(basicProcess, processClass)
       }

       render(view: flash?.view ?: DEFAULT_VIEW,
              model: [processDetailsList: processDetailsList,
                      itemsTotal: itemsTotal ? itemsTotal : 0,
                      searchParameters:  searchParametersList,
                      displayParameters: displayParametersList,
                      processClasses: processClasses])
   }

   private List<SearchParameter> getSearchParametersList() {
     return flash?.searchParameters ?: processListSearchParameters
   }

   private List<SearchParameter> getDisplayParametersList() {
     return flash?.displayParameters ?: processListDisplayParameters
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
