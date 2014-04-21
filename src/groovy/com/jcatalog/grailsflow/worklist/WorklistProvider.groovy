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
 
package com.jcatalog.grailsflow.worklist

import org.apache.commons.lang.StringUtils

import com.jcatalog.grailsflow.model.process.ProcessAssignee
import com.jcatalog.grailsflow.model.process.ProcessNode
import com.jcatalog.grailsflow.model.process.ProcessVariable

import com.jcatalog.grailsflow.utils.ConstantUtils

/**
 * WorklistProvider class is used for getting nodes that are available for the
 * specified authorities and where for all variables from variablesFilter
 * (var.value is in variablesFilter.var.values).  
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class WorklistProvider {
    def appExternalID

    private String buildProcessCondition(def params) {
      if (appExternalID != null) {
        params['appGroupID'] = appExternalID
        return " p.appGroupID = :appGroupID "
      } else {
        return " p.appGroupID is null "
      }
    }

    private String buildStatusCondition(def params) {
      params['status'] = 'ACTIVATED'
      return " (pn.status.statusID = (:status) ) "
    }
    
    private String buildTypeCondition(def params) {
      params['type'] = ConstantUtils.NODE_TYPE_WAIT
      return " (pn.type = :type ) "
    }

    private String buildAuthoritiesFilterCondition(Collection<String> authorities, def params) {
      params['authorities'] = authorities
      // there's no assignees for node or assignees contain some of authorities
      return """ ((not exists (select pa1.id from com.jcatalog.grailsflow.model.process.ProcessAssignee pa1 where pa1.process=pn.process and pa1.nodeID=pn.nodeID)) 
              or (exists (select pa2.id from com.jcatalog.grailsflow.model.process.ProcessAssignee pa2 where pa2.process=pn.process 
                              and pa2.nodeID=pn.nodeID and pa2.assigneeID in (:authorities))) ) """
    }

    // NOTE: can be null
    private String buildVariablesFilterCondition(Map variablesFilter, def params){
        // TODO: add additional mechanism for getting string representation
        // of ProcessVariable values
        def varClass = new ProcessVariable()
        if (variablesFilter && !variablesFilter.isEmpty()) {
            StringBuffer where = new StringBuffer()
            StringBuffer restrictions = new StringBuffer()
            def index = 0
            variablesFilter.keySet().each() { parameter ->
                if (index != 0) restrictions.append(" and pvar${index-1}.process in (")
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
                    if (filterIndex == filter.size()-1) {
                       restrictions.append(" ) ")
                    }
                }
                index ++
            }

            for (int i=1; i<=index-1; i++) {
                restrictions.append(")")
            }
            where.append(" (pn.process in (${restrictions})) ")
            return where.toString()
        } else {
          return null
        }
    }
    
    // NOTE: can be null
    private String buildSortVariableCondition(String variableName, def params) {
        if (variableName) {
          params["sortVariableName"] = variableName
          return " (sort_${variableName}.process = pn.process and sort_${variableName}.name = :sortVariableName) "
        } else {
          return null
        }
    }

    // NOTE: can be null
    private String buildHasNoSortVariableCondition(String variableName, def params) {
        if (variableName) {
          params["sortVariableName"] = variableName
          return """ (not exists (select sort_${variableName} from com.jcatalog.grailsflow.model.process.ProcessVariable as sort_${variableName}
                             where sort_${variableName}.process = pn.process and sort_${variableName}.name = :sortVariableName)) """
        } else {
          return null
        }
    }
    
    // NOTE: can be null
    private String buildOrderCondition(String sortByProperty, String ascending) {
      return sortByProperty ? " order by ${sortByProperty} ${ascending ? ascending : 'desc'} " : null
    }
    
    Integer getWorklistSize(Collection<String> authorities, Map variablesFilter) {
        def params = [:]

        String processCondition = buildProcessCondition(params)
        String typeCondition = buildTypeCondition(params)
        String statusCondition = buildStatusCondition(params)
        String authoritiesCondition = buildAuthoritiesFilterCondition(authorities, params)
        // NOTE: can be null
        String variableFilterCondition = buildVariablesFilterCondition(variablesFilter, params)

        StringBuffer query = new StringBuffer()
        query.append("""select count(*) from com.jcatalog.grailsflow.model.process.ProcessNode pn inner join pn.process as p
                        where ${processCondition} and ${typeCondition} and ${statusCondition} and ${authoritiesCondition} """)
        if (variableFilterCondition) {
          query.append(" and ${variableFilterCondition} ")
        }
        
        return ProcessNode.executeQuery(query.toString(), params).get(0)
    }


    List getWorklist(Collection<String> authorities, Map variablesFilter,
                     String sortBy, // processNode field or process variable value if sortBy="var.<variableName>" 
                     String ascending,
                     Integer maxResult, // to restrict the maximum size of the list
                     Integer offset) // for paging
    {
        if (!authorities) return null

        def params = [:]
        
        def sortByProperty = "pn.nodeID";  // default HQL property for sorting list
        def variableName = null;    // Name of variable if sorting is by variable value
        if (sortBy) {
	        if (sortBy.startsWith("vars.") && sortBy.size() > "vars.".size()) {
	          variableName = StringUtils.substringAfter(sortBy, "vars.")
	          sortByProperty = "sort_${variableName}.variableValue"
          } else {
	          sortByProperty = "pn.${sortBy}"
	        }
        }
        
        if (maxResult) params.max = maxResult
        if (offset) params.offset = offset

        String processCondition = buildProcessCondition(params)
        String typeCondition = buildTypeCondition(params)
        String statusCondition = buildStatusCondition(params)
        String authoritiesCondition = buildAuthoritiesFilterCondition(authorities, params)
        // NOTE: can be null
        String variableFilterCondition = buildVariablesFilterCondition(variablesFilter, params)
        // NOTE: can be null
        String sortCondition = buildSortVariableCondition(variableName, params)
        // NOTE: can be null
        String orderCondition = buildOrderCondition(sortByProperty, ascending)

        StringBuffer query = new StringBuffer()

        query.append("select pn from com.jcatalog.grailsflow.model.process.ProcessNode  pn inner join pn.process as p ")
        if (variableName) {
          query.append(", com.jcatalog.grailsflow.model.process.ProcessVariable as sort_${variableName}")
        }
        query.append(" where ${processCondition} and ${typeCondition} and ${statusCondition} and ${authoritiesCondition} ")
        if (variableFilterCondition) {
          query.append(" and ${variableFilterCondition} ")
        }
        if (sortCondition) {
          query.append(" and ${sortCondition} ")
        }
        if (orderCondition) {
          query.append(" ${orderCondition} ")
        }

        if (! variableName) {
	        return ProcessNode.executeQuery(query.toString(), params)
        } else {
          // Take into account processes that does not have sort variable
          StringBuffer queryNoVars = new StringBuffer()
          queryNoVars.append("""select pn from com.jcatalog.grailsflow.model.process.ProcessNode  pn inner join pn.process as p
                                where ${processCondition} and ${typeCondition} and ${statusCondition} and ${authoritiesCondition} """)
	        if (variableFilterCondition) {
	          queryNoVars.append(" and ${variableFilterCondition} ")
	        }
          // NOTE: can be null
          String hasNoSortCondition = buildHasNoSortVariableCondition(variableName, params)
	        if (hasNoSortCondition) {
	          queryNoVars.append(" and ${hasNoSortCondition} ")
          }

          def queries = null
          if (ascending == 'asc') {
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
            if (maxResult) {
              params.max = maxResult - result.size() 
            }
            if (params.max && params.max <= 0){
              return result;
            }
            result += ProcessNode.executeQuery(it, params)
	        }
          return result;
        }

    }

}