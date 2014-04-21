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
import com.jcatalog.grailsflow.model.process.FlowStatus
import com.jcatalog.grailsflow.process.ProcessSearchParameters

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Process Worklist service provides Process list and worklist filtered by process types, users, variables, etc.
 *
 * @author July Karpey
 * @author Maria Voitovich
 */
class ProcessWorklistService {
    def worklistProvider
    def processProvider
    
    /**
     * Searching for all open process nodes that are available for the requester
     *
     * @param authorities (assignees)
     * @param variablesFilter (varName, set of varValues)
     * @param sortBy  - processNode field
     * @param ascending
     * @param maxResult - to restrict the maximum size of the list
     * @param offset  - for paging
     *
     * @return list of open nodes according to node authorities and filters
     */
    public List getWorklist(List authorities, Map variablesFilter, String sortBy, String ascending,
                            Integer maxResult, Integer offset) {

        if (worklistProvider) {
            try {
                return worklistProvider.getWorklist(authorities, variablesFilter, sortBy,
                                                ascending, maxResult, offset)
            } catch (Exception e) {
                log.error("Exception occurred while getting worklist for authorities ${authorities} and variables ${variablesFilter}", e)
                return null
            }
        }  else {
            log.error("WorklistProvider is not defined!")
        }

        return null
    }

    /**
     * Searching for count of all open process nodes that are available for the requester
     *
     * @param authorities (assignees)
     * @param variablesFilter (varName, set of varValues)
     * @param sortBy  - processNode field
     * @param ascending
     *
     * @return list of open nodes according to node authorities and filters
     */
    public Integer getWorklistSize(List authorities, Map variablesFilter) {

        if (worklistProvider) {
            try {
                def count = worklistProvider.getWorklistSize(authorities, variablesFilter)
                log.debug("Worklist size for authorities ${authorities} and variables ${variablesFilter} is ${count}")
                return count
            } catch (Exception e) {
                log.error("Exception occurred while getting worklist size for authorities ${authorities} and variables ${variablesFilter}", e)
                return 0
            }
        }  else {
            log.error("WorklistProvider is not defined!")
            return 0
        }
    }


    /**
     * Finds processes and theirs nodes according to authorities, statuses and
     * variable filter.
     *
     * @param type 
     * @param status
     * @param username
     * @param startedFrom
     * @param finishedFrom
     * @param variablesFilter (varName : set of varValues)
     * @param sortBy  - processNode field or process variable value if sortBy="var.<variableName>"
     * @param ascending
     * @param maxResult - to restrict the maximum size of the list
     * @param offset  - for paging
      *
      * @return list of processes according to filters
      */

    public def getProcessList(String type, String statusID, String username,
                             Date startedFrom, Date finishedFrom,
                             Map variablesFilter, String sortBy, String ascending,
                             Integer maxResult, Integer offset) {
        ProcessSearchParameters searchParameters  = new ProcessSearchParameters()
        searchParameters.type = type
        searchParameters.statusID = statusID
        searchParameters.startedBy = username
        searchParameters.startedFrom = startedFrom
        searchParameters.finishedFrom = finishedFrom
        searchParameters.variablesFilter = variablesFilter
        searchParameters.sortBy = sortBy
        searchParameters.ascending = ascending
        searchParameters.maxResult = maxResult
        searchParameters.offset = offset
        
        return getProcessList(searchParameters)
    }

    public def getProcessList(ProcessSearchParameters searchParameters) {

       if (processProvider) {
            try {
                // getting processes
                return processProvider
                    .getProcessList(searchParameters)
            } catch (Exception e) {
                log.error("There were exceptions in finding process list ", e)
            }
        }  else {
            log.error("ProcessProvider is not defined!")
        }

        return null
    }

    /**
     * Finds size of process list according to conditions and
     * variable filter.
     *
     * @param type
     * @param status
     * @param username
     * @param startedFrom
     * @param finishedFrom
     * @param variablesFilter (varName : set of varValues)
     *
     * @return size of process list of processes according to filters
     */
    public Integer getProcessListSize(String type, String statusID,String username,
                             Date startedFrom, Date finishedFrom, Map variablesFilter) {
        ProcessSearchParameters searchParameters  = new ProcessSearchParameters()
        searchParameters.type = type
        searchParameters.statusID = statusID
        searchParameters.startedBy = username
        searchParameters.startedFrom = startedFrom
        searchParameters.finishedFrom = finishedFrom
        searchParameters.variablesFilter = variablesFilter

        return getProcessListSize(searchParameters)
    }

    public Integer getProcessListSize(ProcessSearchParameters searchParameters) {

       if (processProvider) {
            try {
                // getting processes
                return processProvider
                    .getProcessListSize(searchParameters)
            } catch (Exception e) {
                log.error("There were exceptions in finding process list", e)
                return 0
            }
        }  else {
            log.error("ProcessProvider is not defined!")
            return 0
        }

    }

}
