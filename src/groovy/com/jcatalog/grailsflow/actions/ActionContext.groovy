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
 
package com.jcatalog.grailsflow.actions

import com.jcatalog.grailsflow.bean.BeanProxySupport

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * An ActionContext represents process data (i.e. variables, assignees, etc) in actions
 *
 * @author Stephan Albers
 * @author July Karpey
 * @author Maria Voitovich
 */

class ActionContext {
    private static final Log log = LogFactory.getLog(getClass())

    private Long processID
    private String nodeID
    private String user
    private String event

    private Map<String, Collection<String>> assignees
    private Collection<String> nextAssignees

    private Map<String, Object> variables
    private Map<String, Collection<BeanProxySupport>> exceptions

    public ActionContext(def process, String nodeID, String user, Map<String, Object> variables, def assignees) {
      this(process, nodeID, user, variables, assignees, null) 
    }


    public ActionContext(def process, String nodeID, String user, Map<String, Object> variables, def assignees, String event) {
        log.debug("Creating execution context for node '${nodeID}' of process #${process?.id}")
        this.processID = process?.id
        this.nodeID = nodeID
        this.user = user
        this.event = event

        this.assignees = assignees?: [:]
        nextAssignees = []

        this.variables = variables

        // put additional variables here
        this.variables.assignees = this.assignees
        this.variables.currentAssignees = assignees[nodeID]?: []

        // create nodes exceptions map
        def exceptions = [:]
        process?.nodes?.each() { node ->
           def nodeExceptions = exceptions[node.nodeID] ?: { def a = []; exceptions[node.nodeID] = a; return a; }.call()
           if (node.exception) {
             nodeExceptions.add(new BeanProxySupport([["type", "message", "stackTrace"]: node.exception]))
           }
        }
        this.exceptions = exceptions
    }

    public Long getProcessID() {
      return processID
    }

    public void setProcessID(Long processID) {
      // do nothing
    }

    public String getNodeID() {
      return nodeID
    }

    public void setNodeID(String nodeID) {
      // do nothing
    }

    public String getUser() {
      return user
    }

    public void setUser(String user) {
      // do nothing
    }

    public String getEvent() {
      return event
    }

    public void setEvent(String event) {
      // do nothing
    }

    public Map<String, Collection<String>> getAssignees() {
      return assignees
    }

    public void setAssignees(Map<String, Collection<String>> assignees) {
      // do nothing
    }

    public Collection<String> getNextAssignees() {
      return nextAssignees
    }

    public void setAssignees(Collection<String> nextAssignees) {
      // do nothing
    }

    public Map<String, Object> getVariables() {
      return variables
    }

    public void setVariables(Map<String, Object> variables) {
      // do nothing
    }

    public Map<String, Collection<BeanProxySupport>> getExceptions() {
      return exceptions
    }

    public void setExceptions(Map<String, Collection<BeanProxySupport>> exceptions) {
      // do nothing
    }

}
