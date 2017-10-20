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

package com.jcatalog.grailsflow.process

import com.jcatalog.grailsflow.model.process.BasicProcess
import com.jcatalog.grailsflow.model.process.ProcessVariable

import org.apache.commons.lang.StringUtils


/**
 * ProcessProvider class is used for getting processes by conditions and  variablesFilter
 *
 * @author Stephan Albers
 * @author July Karpey
 * @author Maria Voitovich
 */
class ProcessProvider {
    def appExternalID

    private Collection<String> buildProcessConditions(ProcessSearchParameters searchParameters, def params) {
        def conditions = []

        params['appGroupID'] = appExternalID
        conditions << " p.appGroupID = :appGroupID "

        if (searchParameters.type) {
            params['type'] = searchParameters.type.split(",").toList()*.trim()
            conditions << " (p.type in ( :type ) ) "
        }

        if (searchParameters.processID) {
            params['processID'] = searchParameters.processID
            conditions << " (p.id = :processID ) "
        }

        if (searchParameters.statusID) {
            params['status'] = searchParameters.statusID.split(",").toList()*.trim()
            conditions << " (p.status.statusID in ( :status ) ) "
        }

        if (searchParameters.startedBy) {
            params['createdBy'] = searchParameters.startedBy
            conditions << " (p.createdBy = :createdBy ) "
        }

        if (searchParameters.startedFrom && searchParameters.startedTo) {
            params['startedFrom'] = searchParameters.startedFrom
            params['startedTo'] = searchParameters.startedTo
            conditions << " (p.createdOn between :startedFrom and :startedTo ) "
        } else if (searchParameters.startedFrom) {
            params['createdOn'] = searchParameters.startedFrom
            conditions << " (p.createdOn >= :createdOn ) "
        } else if (searchParameters.startedTo) {
            params['createdOn'] = searchParameters.startedTo
            conditions << " (p.createdOn <= :createdOn ) "
        }

        if (searchParameters.finishedFrom && searchParameters.finishedTo) {
            params['finishedFrom'] = searchParameters.finishedFrom
            params['finishedTo'] = searchParameters.finishedTo
            conditions << " (p.finishedOn between :finishedFrom and :finishedTo ) "
        } else if (searchParameters.finishedFrom) {
            params['finishedOn'] = searchParameters.finishedFrom
            conditions << " (p.finishedOn >= :finishedOn ) "
        } else if (searchParameters.finishedTo) {
            params['finishedOn'] = searchParameters.finishedTo
            conditions << " (p.finishedOn <= :finishedOn ) "
        }

        if (searchParameters.modifiedBy) {
            params['modifiedBy'] = searchParameters.modifiedBy
            conditions << " (p.lastModifiedBy = :modifiedBy ) "
        }
        if (searchParameters.modifiedFrom && searchParameters.modifiedTo) {
            params['modifiedFrom'] = searchParameters.modifiedFrom
            params['modifiedTo'] = searchParameters.modifiedTo
            conditions << " (p.lastModifiedOn between :modifiedFrom and :modifiedTo ) "
        } else if (searchParameters.modifiedFrom) {
            params['modifiedOn'] = searchParameters.modifiedFrom
            conditions << " (p.lastModifiedOn >= :modifiedOn ) "
        } else if (searchParameters.modifiedTo) {
            params['modifiedOn'] = searchParameters.modifiedTo
            conditions << " (p.lastModifiedOn <= :modifiedOn ) "
        }
        return conditions
    }

    // NOTE: can be null
    private String buildVariablesFilterCondition(Map variablesFilter, def params){
        // TODO: add additional mechanism for getting string representation
        // of ProcessVariable values
        def varClass = new ProcessVariable()
        StringBuffer where = new StringBuffer()
        StringBuffer restrictions = new StringBuffer()
        restrictions.append("""select pvar0.process from com.jcatalog.grailsflow.model.process.ProcessVariable as pvar0""")
        def index = 1
        variablesFilter.keySet().each() { parameter ->
            restrictions.append(" ${index == 1 ? "where" : "and"} pvar${index - 1}.process in (")
            restrictions.append("""select pvar${index}.process from com.jcatalog.grailsflow.model.process.ProcessVariable as pvar${index}
                                       where pvar${index}.name = (:${parameter}_name)
                                       and """)
            params["${parameter}_name"] = parameter

            def filter = variablesFilter[parameter] instanceof List ?
                    variablesFilter[parameter] : [variablesFilter[parameter]]
            filter.eachWithIndex() { obj, filterIndex ->
                if (filterIndex > 0) {
                    restrictions.append(" or ")
                } else if (filterIndex == 0) {
                    restrictions.append(" ( ")
                }
                restrictions.append(" pvar${index}.variableValue like (:${parameter}_${filterIndex}_value) ")
                varClass.value = obj
                params["${parameter}_${filterIndex}_value"] = varClass.variableValue
                if (filterIndex == filter.size() - 1) {
                    restrictions.append(" ) ")
                }
            }
            index++
        }

        for (int i = 1; i <= index - 1; i++) {
            restrictions.append(")")
        }
        where.append(" (p in (${restrictions})) ")
        return where.toString()

    }

    // NOTE: can be null
    private String buildSortVariableCondition(String variableName, def params) {
        if (variableName) {
          params["sortVariableName"] = variableName
          return " (sort_${variableName}.process = p and sort_${variableName}.name = :sortVariableName) "
        } else {
          return null
        }
    }

    // NOTE: can be null
    private String buildHasNoSortVariableCondition(String variableName, def params) {
        if (variableName) {
          params["sortVariableName"] = variableName
          return """ (not exists (select sort_${variableName} from com.jcatalog.grailsflow.model.process.ProcessVariable as sort_${variableName}
                             where sort_${variableName}.process = p and sort_${variableName}.name = :sortVariableName)) """
        } else {
          return null
        }
    }

    // NOTE: can be null
    private String buildOrderCondition(String sortByProperty, String ascending) {
      return sortByProperty ? " order by ${sortByProperty} ${ascending ? ascending : 'desc'} " : null
    }

    Integer getProcessListSize(ProcessSearchParameters searchParameters) {
        if (!searchParameters) return null

        def params = [:]

        Collection<String> processConditions =
            buildProcessConditions(searchParameters, params)
        // NOTE: can be null
        String variableFilterCondition = buildVariablesFilterCondition(searchParameters.variablesFilter, params)
        if (variableFilterCondition) {
          processConditions << variableFilterCondition
        }

        StringBuffer query = new StringBuffer()
        query.append("select count(*) from com.jcatalog.grailsflow.model.process.BasicProcess p")
        if (processConditions) {
          query.append(" where ${processConditions.join(' and ')} ")
        }

        return BasicProcess.executeQuery(query.toString(), params).get(0)
    }


    /**
     * Finds processes and theirs nodes according to authorities, statuses and
     * variable filter.
     *
     * @param type
     * @param status
     * @param username
     * @param startedFrom
     * @param startedTo
     * @param finishedFrom
     * @param finishedTo
     * @param variablesFilter (varName : set of varValues)
     * @param sortBy  - process field or process variable value if sortBy="var.<variableName>"
     * @param ascending
     * @param maxResult - to restrict the maximum size of the list
     * @param offset  - for paging
     *
     * @return list of processes according to filters
     */
    List getProcessList(ProcessSearchParameters searchParameters) {
      if (!searchParameters) return null
      def params = [:]

      def sortByProperty = "p.type";  // default HQL property for sorting list
      def variableName = null;    // Name of variable if sorting is by variable value

      String sortBy = searchParameters.sortBy
      if (sortBy) {
          if (sortBy.startsWith("vars.") && sortBy.size() > "vars.".size()) {
            variableName = StringUtils.substringAfter(sortBy, "vars.")
            sortByProperty = "sort_${variableName}.variableValue"
          } else {
            sortByProperty = "p.${sortBy}"
          }
      }

      if (searchParameters.maxResult) params.max = searchParameters.maxResult
      if (searchParameters.offset) params.offset = searchParameters.offset

      Collection<String> processConditions =
          buildProcessConditions(searchParameters, params)
      // NOTE: can be null
      String variableFilterCondition = buildVariablesFilterCondition(searchParameters.variablesFilter, params)
      if (variableFilterCondition) {
        processConditions << variableFilterCondition
      }

      String sortCondition = buildSortVariableCondition(variableName, params)
      // NOTE: can be null
      String orderCondition = buildOrderCondition(sortByProperty, searchParameters.ascending)

      StringBuffer query = new StringBuffer()
      query.append("select p from com.jcatalog.grailsflow.model.process.BasicProcess p")
      if (variableName) {
        query.append(", com.jcatalog.grailsflow.model.process.ProcessVariable as sort_${variableName}")
      }
      def conditions = []
      if (processConditions) {
        conditions.addAll(processConditions)
      }
      if (sortCondition) {
        conditions.add(sortCondition)
      }
      if (conditions) {
        query.append(" where ${conditions.join(' and ')} ")
      }
      if (orderCondition) {
        query.append(" ${orderCondition} ")
      }

      if (! variableName) {
        return BasicProcess.executeQuery(query.toString(), params)
      } else {
        // Take into account processes that does not have sort variable
        StringBuffer queryNoVars = new StringBuffer()
        queryNoVars.append("select p from com.jcatalog.grailsflow.model.process.BasicProcess p")
        def noVarConditions = []
        if (processConditions) {
           noVarConditions.addAll(processConditions)
        }
        // NOTE: can be null
        String hasNoSortCondition = buildHasNoSortVariableCondition(variableName, params)
        if (hasNoSortCondition) {
          noVarConditions.add(hasNoSortCondition)
        }
        if (noVarConditions) {
          queryNoVars.append(" where ${noVarConditions.join(' and ')} ")
        }


        def queries = null
        if (searchParameters.ascending == 'asc') {
          // first select items with no variable value
          queries = ["${queryNoVars.toString()}", "${query.toString()}"]
        } else {
          // first select items with variable value
          queries = ["${query.toString()}", "${queryNoVars.toString()}"]
        }
        def result = []
        queries.each() {
          if (result.size() != 0) {
            params.offset = 0
          }
          if (searchParameters.maxResult) {
            params.max = searchParameters.maxResult - result.size()
          }
          if (params.max && params.max <= 0){
            return result;
          }
          result += BasicProcess.executeQuery(it, params)
          }
        return result;
      }
    }

}