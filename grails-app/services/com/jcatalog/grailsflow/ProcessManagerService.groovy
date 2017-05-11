package com.jcatalog.grailsflow
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

import org.springframework.beans.factory.InitializingBean

import org.apache.commons.lang.StringUtils

import com.jcatalog.grailsflow.engine.ProcessFactory
import com.jcatalog.grailsflow.engine.execution.NodeExecutor

import com.jcatalog.grailsflow.actions.ActionContext

import com.jcatalog.grailsflow.utils.ConstantUtils

import com.jcatalog.grailsflow.model.process.BasicProcess
import com.jcatalog.grailsflow.model.process.FlowStatus
import com.jcatalog.grailsflow.model.process.ProcessAssignee
import com.jcatalog.grailsflow.model.process.ProcessNode
import com.jcatalog.grailsflow.model.process.ProcessNodeException
import com.jcatalog.grailsflow.model.process.ProcessVariable

import com.jcatalog.grailsflow.model.graphics.ProcessNodePosition
import com.jcatalog.grailsflow.extension.SendEventParameters

import com.jcatalog.grailsflow.engine.concurrent.ProcessLock

import grails.util.Environment
import java.util.concurrent.Executors
import java.util.concurrent.Callable
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.orm.hibernate3.SessionHolder
import org.hibernate.FlushMode
import org.springframework.orm.hibernate3.SessionFactoryUtils
import org.hibernate.Session
import org.hibernate.Hibernate

import com.jcatalog.grailsflow.logging.LogUtils
import com.jcatalog.grailsflow.utils.AuthoritiesUtils
import com.jcatalog.grailsflow.process.PostKillProcessHandler
import org.springframework.transaction.TransactionStatus
import org.quartz.ObjectAlreadyExistsException
import org.quartz.SimpleTrigger
import static org.quartz.TriggerKey.*;
import java.util.concurrent.ThreadFactory
import com.jcatalog.grailsflow.status.ProcessStatusEnum
import com.jcatalog.grailsflow.status.NodeStatusEnum
import com.jcatalog.grailsflow.engine.execution.ExecutionResultEnum
import com.jcatalog.grailsflow.model.definition.ProcessNodeDef
import com.jcatalog.grailsflow.engine.concurrent.ProcessNotifier
import com.jcatalog.grailsflow.model.definition.ProcessDef
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.JobDataMap
import com.jcatalog.grailsflow.cluster.GrailsflowLock

/**
 * Process Manager service is an engine that deals with processes:
 * starts it, executes it, checks it state, kills it.
 *
 * All operations over running process that can change that process
 * (e.g. sendEvent, forwardProcessNode, killProcess) are synchronized by processID
 *
 * available methods:
 * - startProcess
 * - sendEvent
 * - killProcess
 * - forwardProcessNode
 * - checkProcessIdentifier
 *
 * - getSupportedProcessClasses
 * - getProcessClass
 * - getNewProcessInstance
 * - getStartingProcessInstance
 * - getRunningProcessInstance
 *
 * @author Stephan Albers
 * @author July Karpey
 * @author Maria Voitovich
 * @author Ivan Baidakou
 */
class ProcessManagerService implements InitializingBean {
    boolean transactional = false // manage transactions manually

    ProcessFactory processFactory
    NodeExecutor nodeExecutor

    def maxThreadsQuantity
    def quartzScheduler
    def sessionFactory
    def workareaPathProvider
    def processLogDir
    def grailsApplication
    def grailsflowLockService
    def messageSource
    PostKillProcessHandler postKillProcessHandler
    ThreadRuntimeInfoService threadRuntimeInfoService

    def errors = []

    void afterPropertiesSet() throws Exception {
    }

    public File getProcessLogFile(String processType, Long processId) {
        if (processLogDir) {
            File logsResource = workareaPathProvider.getResourceFile(processLogDir)
            if (!logsResource) {
                File logsRoot = new File("${workareaPathProvider.getResourceFile('')?.absolutePath}/${processLogDir}/${processType}")
                if (!logsRoot.exists()) {
                    logsRoot.mkdirs()
                }
            }

            if (workareaPathProvider.getResourceFile(processLogDir)) {
                File f = new File(workareaPathProvider.getResourceFile(processLogDir), "${processType}/${processId}.log")
                return f
            } else {
                return null
            }
        } else {
            return null;
        }
    }

    public File getProcessLogFile(BasicProcess process) {
        return getProcessLogFile(process.type, process.id)
    }

    /**
     * Get class for process of defined type
     */
    public def getProcessClass(String processType){
        return processFactory.getProcessClassForName(processType)
    }


    /**
     * Get new instance of process of defined type
     */
    public def getNewProcessInstance(String processType){
        def processClass = getProcessClass(processType)
        if (!processClass) {
            return null
        }
        return processClass.newInstance()
    }

    /**
     * Get process instance of defined type. Update it's variables values.
     */
    public def getStartingProcessInstance(String processType, Map<String, Object> variables) {
        def process = getNewProcessInstance(processType)
        if (!process){
            return null;
        }
        // update default variables values
        initProcessFields(process, variables)
        return process;
    }

    /**
     * Get process instance for processID. Update it's variables values.
     */
    public def getRunningProcessInstance(Long processID) {
        BasicProcess basicProcess = BasicProcess.get(processID)
        if (!basicProcess) {
            log.error("Process with key #${processID} doesn't exist.")
            return null
        }
        def process = getNewProcessInstance(basicProcess.type)
        if (!process){
            return null;
        }
        // get current ProcessVariables
        Map processVars = getVariablesMap(basicProcess)
        // initiate process fields with variables values
        initProcessFields(process, processVars)
        return process;
    }

    /**
     * Update process fields with variables values
     */
    private void initProcessFields(def process, Map<String, Object> variables){
        if (!process || !variables) return

        process.class.variables?.each {
            if (variables.keySet().contains(it.name)) {
                try {
                    // define value
                    def value
                    if (variables[it.name] != null && ProcessVariable.isValueIdentifier(it.type)
                        && Hibernate.getClass(variables[it.name]).name != it.type) {
                        try {
                            def domainClass = getClass().getClassLoader().loadClass(it.type, false)
                            if (domainClass && domainClass.list(max:1)) {
                                def ident = domainClass.list(max:1).get(0).ident()
                                def type = ProcessVariable.defineType(ident.getClass())
                                def key = ProcessVariable.getConvertedValue(variables[it.name], type)
                                value = domainClass.get(key)
                            }
                        } catch (Exception e) {
                            log.error("Exception occured in getting object of type '${it.type}' by key", e)
                            value = variables[it.name]
                        }
                    } else if (variables[it.name].getClass().simpleName == it.type) {
                        value = variables[it.name]
                    } else {
                        value = ProcessVariable.getConvertedValue(variables[it.name], it.type)
                    }
                    process.class.getField(it.name)
	                       .set(process, value)
                } catch (IllegalArgumentException e) {
                    log.error("Exception occurred while setting value '${variables[it.name]}' of type '${variables[it.name]?.class}' for field '${it.name}' of type '${it.type}' of process ${process.class.name}", e)
                } catch (Exception ex) {
                    log.error("Exception occurred", ex)
                }

	        }
        }
    }

    /**
     * Starting process execution
     *
     * @param processTypeID
     * @param user
     *
     * @return process ID value if process was started successfully
     */
    public synchronized def startProcess(def processTypeID, def user, def variables) {
        // check processTypeID in supportedProcessTypes
        if (!processFactory.getProcessTypes().contains(processTypeID)) {
            log.error("Cannot start process of type '${processTypeID}'. Type is not supported.")
            return null
        }

        def process = getStartingProcessInstance(processTypeID, variables)
        if (!process) {
          log.error("Cannot start process of type '${processTypeID}'. Failed to create process class instance.")
          return null
        }

        Class processClass = process.class

        // create BasicProcess based on process instance
        BasicProcess basicProcess = new BasicProcess()
        basicProcess.type = processTypeID
        basicProcess.createdOn = new Date()
        basicProcess.createdBy = user
        basicProcess.lastModifiedOn = new Date()
        basicProcess.lastModifiedBy = user

        if (!processClass?.startNode) {
            log.error("Cannot start process of type '${processTypeID}'. There is no start node in process definition.")
            return null
        }

        if (processClass.startNode?.type == ConstantUtils.NODE_TYPE_WAIT) {
            basicProcess.status = getStatus(ProcessStatusEnum.SUSPENDED.value())
        } else {
            basicProcess.status = getStatus(ProcessStatusEnum.ACTIVATED.value())
        }

        BasicProcess.withTransaction {status->
            if (!basicProcess.save(flush: true) ) {
                status.setRollbackOnly();
                log.error("Cannot save process: "+basicProcess.errors)
            }
        }

        // set initial process assignees
        def processAssignees = [:]
        processClass.nodesList.each() { nodeDef ->
            processAssignees.put(nodeDef.nodeID, nodeDef.assignees?.collect() { it.assigneeID })
        }

        // get start node def
        ProcessNodeDef startNodeDef = processClass.startNode
        startNodeDef.processDef = new ProcessDef(processID:  processTypeID)
        // initialize start node
        def startNode = initNewNode(startNodeDef, null, user, processClass.nodeInfos[startNodeDef.nodeID])

        threadRuntimeInfoService.invokeInCurrentThread(basicProcess.id,{ ProcessNotifier notifier ->
            try {
                // create process variables
                defineVariables(basicProcess, process)

                updateProcessAssignees(basicProcess, processAssignees)

                basicProcess.addToNodes(startNode)

                BasicProcess.withTransaction {status->
                    if (!basicProcess.save(flush: true) ) {
                        status.setRollbackOnly();
                        log.error("Cannot save process: "+basicProcess.errors)
                    }
                }

                if (!basicProcess.hasErrors()) {
                    // define process graphic representation
                    // TODO: should be removed and created automatically by engine
                    def finalNodeIDs = processClass.finalNodes.collect() { it.nodeID }
                    def startNodeID = startNodeDef.nodeID
                    processClass.nodesList.each() { node ->
                        def nodePosition = new ProcessNodePosition(process: basicProcess, nodeID: node.nodeID,
                                               actionType: node.type, dueDate: node.dueDate)
                        nodePosition.knotType = ""
                        if (node.nodeID in finalNodeIDs) {
                            nodePosition.knotType = "final"
                        }
                        if (node.nodeID == startNodeID) {
                            nodePosition.knotType = "start"
                        }
                        ProcessNodePosition.withTransaction { status->
                            if (!nodePosition.save(flush: true)) {
                                status.setRollbackOnly();
                                log.error("Cannot save process: "+nodePosition.errors.join('\n'))
                            }
                        }
                    }
                }
            } catch (Throwable ex) {
                log.error("Unexpected exception occurred during process starting! ", ex)
            }
        })

        return basicProcess.id
    }

    /**
     * Killing process
     *
     * @param processID
     * @param user
     *
     * @return true if process was killed successfully
     */
    public Boolean killProcess(Long processID, String user) {
        Boolean result = Boolean.FALSE

        threadRuntimeInfoService.signalInterrupt(processID)
        Thread killingThread = Thread.start {
            def session = SessionFactoryUtils.getNewSession(sessionFactory);
            session.setFlushMode(FlushMode.AUTO);

            TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));

            threadRuntimeInfoService.invokeInCurrentThread(processID,{ ProcessNotifier notifier ->
                try {
                    BasicProcess process = BasicProcess.get(processID)
                    log.debug("Killing process #${processID}")
                    result = startKillingProcess(process, user)
                } catch (Throwable ex) {
                    log.error("Unexpected exception occurred in synchronized block! ", ex)
                    result = Boolean.FALSE
                }
            })
            session.flush()
            TransactionSynchronizationManager.unbindResource(sessionFactory);
            SessionFactoryUtils.closeSession(session);
        }
        killingThread.join()
        log.debug("Kill action execution of process #${processID}")


        if (threadRuntimeInfoService.isProcessKilledByExecutionThread(processID)) {
            threadRuntimeInfoService.signalActionInterrupt(processID)
        } else {
            Thread killedThread = Thread.start {
                def session = SessionFactoryUtils.getNewSession(sessionFactory);
                session.setFlushMode(FlushMode.AUTO);

                TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));

                threadRuntimeInfoService.invokeInCurrentThread(processID,{ ProcessNotifier notifier ->
                    try {
                        BasicProcess process = BasicProcess.get(processID)
                        log.debug("Killed process #${processID}")
                        result = killedProcess(process, user)
                    } catch (Throwable ex) {
                        log.error("Unexpected exception occurred in synchronized block! ", ex)
                        result = Boolean.FALSE
                    } finally {
                        notifier.unregisterInteres()
                    }
                })
                session.flush()
                TransactionSynchronizationManager.unbindResource(sessionFactory);
                SessionFactoryUtils.closeSession(session);
            }
            killedThread.join()
        }

        return result
    }

    /**
     * Starting Process killing and save state of Process to Killing
     * @param process
     * @param user
     * @param wasProcessKilled
     * @return
     */
    private Boolean startKillingProcess(BasicProcess process, String user) {
        if (!process) return Boolean.FALSE

        if(process.status.statusID == ProcessStatusEnum.KILLED.value()) {
            log.debug("Cannot set status KILLING: Process [${process.id}] has been already killed.")
            return Boolean.TRUE
        }
        if(process.status.statusID == ProcessStatusEnum.COMPLETED.value()) {
            log.debug("Cannot set status KILLING: Process [${process.id}] has been already completed.")
            return Boolean.FALSE
        }

        Boolean result = true
        LogUtils.redirectLogging(getProcessLogFile(process)) {

            process.nodes?.each() {
                if (!it.status.isFinal) {
                    log.debug("Setting status KILLING for the node ${it.nodeID} of process #${process?.id}(${process?.type})")
                    it.status = getStatus(NodeStatusEnum.KILLING.value())
                }
            }
            log.debug("Trying to kill process #${process?.id}(${process?.type})")
            process.status = getStatus(ProcessStatusEnum.KILLING.value())
            process.lastModifiedBy = user
            process.lastModifiedOn = new Date()
            BasicProcess.withTransaction { status ->
                if (!process.save(flush: true)) {
                    status.setRollbackOnly();
                    result = false
                }
            }
        }
        return result
    }

    /**
     * Set Killed status to Process and nodes
     * @param process
     * @param user
     * @param killedStatus
     * @return
     */
    private Boolean killedProcess(BasicProcess process, String user) {
        if (!process) return Boolean.FALSE

        if(process.status.statusID == ProcessStatusEnum.KILLED.value()) {
            log.debug("Process [${process.id}] has been already killed.")
            return Boolean.TRUE
        }
        if(process.status.statusID == ProcessStatusEnum.COMPLETED.value()) {
            log.debug("Process [${process.id}] has been already completed.")
            return Boolean.FALSE
        }

        FlowStatus processKilledStatus = getStatus(ProcessStatusEnum.KILLED.value())
        FlowStatus nodeKilledStatus = getStatus(NodeStatusEnum.KILLED.value())

        Boolean result = true
        LogUtils.redirectLogging(getProcessLogFile(process)) {
            process.nodes?.each() {
                if (!it.status.isFinal) {
                    log.debug("Setting status KILLED for the node ${it.nodeID} of process #${process?.id}(${process?.type})")
                    it.status = nodeKilledStatus
                }
            }
            log.debug("Setting status KILLED for process #${process?.id}(${process?.type})")

            process.status = processKilledStatus
            process.finishedOn = new Date()
            process.finishedBy = user

            if (postKillProcessHandler) {
                postKillProcessHandler.handle(process, user)
            }

            BasicProcess.withTransaction { status ->
                if (!process.save(flush: true)) {
                    status.setRollbackOnly();
                    result = false
                }
            }
        }
        return result
    }

    /**
     * For invoking node asynchronously we schedule SendEventJob.
     * SendEventJob calls node execution in a separate thread.
     *
     * @param process
     * @param nodeID
     * @param event
     * @param user
     * @param variables
     *
     */
    public synchronized int invokeAsynchronousNode(BasicProcess process, String nodeID,
                                                   String event, String user, def variables) {
        JobDataMap parameters = new JobDataMap()
        parameters.processID = process?.id
        parameters.nodeID = nodeID
        parameters.event = event
        parameters.user = user
        parameters.variables = variables

        String currentClusterName = (grailsApplication.config.grailsflow.clusterName instanceof Closure) ?
            grailsApplication.config.grailsflow.clusterName()?.toString() :
            grailsApplication.config.grailsflow.clusterName?.toString()

        SimpleTrigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("${currentClusterName}:${process?.type}(${process?.id})-${nodeID}".toString(), "PROCESS")
            .usingJobData(parameters)
            .build()

        log.debug("Invoking asynchronous node '${nodeID}' of process #${process?.id}(${process?.type})")
        try {
            SendEventJob.schedule(trigger)
        } catch (ObjectAlreadyExistsException e) {
            log.debug("Node '${nodeID}' of process #${process?.id}(${process?.type}) have already started the execution. The invocation was skipped.")
        }
        return 0
    }

    /**
     * Sending event to process for node
     *
     */
    public int sendEvent(SendEventParameters params) {
      return sendEvent(params.processID, params.nodeID, params.event, params.requester, params.variables)
    }

    /**
     * Sending event to process for node
     *
     */
    public int sendEvent(Long processID, String nodeID, String eventID, String user) {
      return sendEvent(processID, nodeID, eventID, user, null)
    }

    /**
     * Sending event to process for node
     *
     */
    public int sendEvent(BasicProcess basicProcess, ProcessNode node, String eventID, String user) {
        return sendEvent(basicProcess, node, eventID, user, null)
    }

    /**
     * Sending event to process for node
     *
     * @param process
     * @param node
     * @param event
     * @param user
     * @param variables
     *
     */
    public int sendEvent(BasicProcess basicProcess, ProcessNode node, String eventID, String user, Map variables) {
        if (!node) {
            return ExecutionResultEnum.NO_NODE_OR_NODE_COMPLETED.value()
        }
        if (!basicProcess) {
            return ExecutionResultEnum.NO_PROCESS_FOR_PROCESSID.value()
        }

        return sendEventForExecution(basicProcess, node, eventID, user, variables)
    }

    /**
     * Sending event to process for node with nodeID
     *
     * @param process
     * @param nodeID
     * @param event
     * @param user
     * @param variables
     *
     */
    public int sendEvent(Long processID, String nodeID, String eventID, String user, Map variables) {
        if (!processID || !nodeID) {
            return ExecutionResultEnum.NO_PROCESS_FOR_PROCESSID.value()
        }

        def basicProcess = BasicProcess.get(processID)
        if (!basicProcess) {
            return ExecutionResultEnum.NO_PROCESS_FOR_PROCESSID.value()
        }

        def node = basicProcess.nodes.find() { it.nodeID == nodeID }
        if (!node) {
            return ExecutionResultEnum.NO_NODE_OR_NODE_COMPLETED.value()
        }

        return sendEventForExecution(basicProcess, node, eventID, user, variables)
    }


    public int sendEventForExecution (BasicProcess basicProcess, ProcessNode node, String eventID, String user, Map variables) {
        // 'wait nodes' are executed synchronously, other types of nodes - asynchronously
        // !!! for tests we running all nodes in one thread
        if (node.type == ConstantUtils.NODE_TYPE_WAIT || Environment.current == Environment.TEST) {
            log.debug("Invoking synchronous node(${ConstantUtils.NODE_TYPE_WAIT}) '${node.nodeID}' of process #${basicProcess.id}(${basicProcess.type})")
            return invokeNodeExecution(basicProcess.id, node.nodeID, eventID, user, variables)
        } else {
            // check the quantity of scheduled 'PROCESS' triggers
            def runningThreads = quartzScheduler
                    .getCurrentlyExecutingJobs()
                    .findAll() {it.trigger.key.group == "PROCESS"}

            if (!runningThreads || canNewThreadBeStarted(runningThreads.size())) {
                return invokeAsynchronousNode(basicProcess, node.nodeID, eventID, user, variables)
            } else {
                return ExecutionResultEnum.NO_THREAD_FOR_EXECUTION.value()
            }
        }
    }

    /**
     * Invoking node event for execution
     *
     * Result codes of sending event (return INT value):
     * return 0 - event was sent successfully
     * return 1 - process with processID was not found
     * return 2 - process does not have a node with nodeID
     * return 3 - node has no appropriate transition for this event
     * return 4 - there is no node with the requested nodeID and processKey
     *            or the node was completed already
     * return 5 - if impossible to get process instance from builder object
     * return 6 - process/node was killed
     * return 7 - node execution failed with exception
     * return 8 - impossible to create new thread for execution
     * return 9 - execution was interrupted by kill operation
     *
     * @param processID - is a unique key of process
     * @param nodeID
     * @param eventID
     * @param user
     * @param variables
     *
     * @return result code of sending event
     */
    public Integer invokeNodeExecution(Long processID, String nodeID, String eventID, String user, Map variables) {
        def executionResult
        BasicProcess basicProcess
        ProcessNode node
        try {
            executionResult = threadRuntimeInfoService.invokeInCurrentThread(processID, { ProcessNotifier notifier ->
                try {
                    ProcessNodeDef nodeDef
                    Class processClass
                    ActionContext context
                    def process

                    if (!notifier.interrupted) {

                        log.debug("Sending event '${eventID}' for node '${nodeID}' of process #${processID} by user: ${user}")
                        // get and check process
                        if (!processID) {
                            return ExecutionResultEnum.NO_PROCESS_FOR_PROCESSID.value()
                        }

                        basicProcess = BasicProcess.get(processID)
                        if (!basicProcess) {
                            return ExecutionResultEnum.NO_PROCESS_FOR_PROCESSID.value()
                        }

                        File logFile = getProcessLogFile(basicProcess)
                        // redirect logs to the process-specific file
                        log.debug("Status of process #${basicProcess.id}(${basicProcess.type}) is ${basicProcess.status.statusID}")
                        if (basicProcess.status.statusID == ProcessStatusEnum.KILLED.value()) {
                            return ExecutionResultEnum.PROCESS_KILLED.value()
                        }

                        process = getRunningProcessInstance(processID)

                        // check if process instance was created successfully,
                        // if the process script is not valid any more
                        // we should kill the process
                        if (!process) {
                            killedProcess(basicProcess, "exception")
                            return ExecutionResultEnum.NO_PROCESS_DEFINITION.value()
                        }
                        processClass = process.class

                        // check if process has a node with nodeID
                        nodeDef = process.class.nodes[nodeID]
                        if (!nodeID || nodeDef == null) {
                            return ExecutionResultEnum.NO_NODEID_IN_PROCESS.value()
                        }

                        // check eventID
                        if (!isSuitableEvent(eventID, nodeID, processClass)) {
                            return ExecutionResultEnum.NO_TRANSITION_FOR_EVENT.value()
                        }

                        node = basicProcess.nodes.find() {
                            it.nodeID == nodeID && it.status in
                                    [ getStatus(NodeStatusEnum.ACTIVATED.value()),
                                            getStatus(NodeStatusEnum.AWAIT_CALLBACK.value()),
                                            getStatus(NodeStatusEnum.RUNNING.value())
                                    ]
                        }
                        if (!node) {
                            return ExecutionResultEnum.NO_NODE_OR_NODE_COMPLETED.value()
                        }

                        log.debug("Status of the node '${node.nodeID}' of process  #${basicProcess.id}(${basicProcess.type}) is ${node.status.statusID}")
                        if (node.status.statusID == NodeStatusEnum.KILLED.value()) {
                            return ExecutionResultEnum.PROCESS_KILLED.value()
                        }

                        // update process variables with passed new values
                        if (variables) {
                            processClass.variables.each() { varDef ->
                                def varName = varDef.name
                                if (variables.keySet().contains(varName) && process.isWritable(varName, nodeID)) {
                                    process[varName] = variables[varName]
                                    log.debug("Setting new value ${process[varName]} for variable ${varName} in process #${basicProcess.id}(${basicProcess.type})")
                                }
                            }
                        }

                        // create execution context for executing node body
                        context = new ActionContext(basicProcess, nodeID, user, process.processContext, getProcessAssignees(basicProcess), eventID)
                    } else {
                        log.error("Sending event for process [${processID}] was interrupted unexpectedly!")
                        return ExecutionResultEnum.INTERRUPTED_BY_KILLING.value()
                    }

                    if (!notifier.interrupted) {
                        // if the node in active state ['ACTIVATED', 'RUNNING'] than we need to execute step body
                        if ((node.status.statusID == NodeStatusEnum.ACTIVATED.value()
                                || node.status.statusID == NodeStatusEnum.RUNNING.value())
                           && processClass.nodeActions[node.nodeID]) {

                            if (node.type == ConstantUtils.NODE_TYPE_WAIT) {
                                log.warn("""[DEPRECATED] The 'action{}' closure is deprecated in manual node definition!
                                    Check your ${node.nodeID} node.
                                    Such code may lead to system dead locks.""")
                            }

                            // update node status to 'RUNNING' state
                            node.event = eventID
                            node.status = getStatus(NodeStatusEnum.RUNNING.value())
                            node.startedExecutionOn = new Date()
                            if(!saveProcess(basicProcess, processID)){
                                return ExecutionResultEnum.FAILED_WITH_EXCEPTION.value()
                            }

                            def result = null
                            try {
                                result = executeNode(node, processClass, context, notifier)
                                node.refresh()
                                basicProcess.refresh()
                            } catch (Exception e) {
                                node.refresh()
                                basicProcess.refresh()
                                if (e instanceof InterruptedException) {
                                    log.debug("Node execution was interrupted!")
                                    return ExecutionResultEnum.INTERRUPTED_BY_KILLING.value()
                                } else {
                                    log.error("Node execution throwed exception", e)
                                }

                                ProcessNodeException exception = new ProcessNodeException(e)
                                exception.node = node
                                node.exception = exception

                                if (nodeDef.transitions*.event?.contains("exception")) {
                                    eventID = "exception"
                                } else { // if there's no 'exception' transition then kill process
                                    log.error("Sending event for process [${processID}] was interrupted unexpectedly!")
                                    killedProcess(basicProcess, "exception")
                                    e.printStackTrace();
                                     return ExecutionResultEnum.INTERRUPTED_BY_KILLING.value()
                                }
                            }

                            log.debug("The result of execution node '${nodeID}' of process #${basicProcess.id}(${basicProcess.type}) is '${result}'")
                            // check result value
                            if (eventID == null) {
                                eventID = result
                            }

                            // check result: if the result is "AWAIT_CALLBACK"
                            // it means that we wait for the result of an asynchronous call
                            // and not be forwarded to the next step before a callback from
                            // the Webservice/asynchronous operation is received.
                            if (result == "AWAIT_CALLBACK") {
                                node.status = getStatus(NodeStatusEnum.AWAIT_CALLBACK.value())
                                if(!saveProcess(basicProcess, processID)){
                                    return ExecutionResultEnum.FAILED_WITH_EXCEPTION.value()
                                }

                                return ExecutionResultEnum.EXECUTED_SUCCESSFULLY.value()
                            }
                        }
                    } else {
                        log.error("Sending event for process [${processID}] was interrupted unexpectedly!")
                        return ExecutionResultEnum.INTERRUPTED_BY_KILLING.value()
                    }

                    if (!notifier.interrupted) {
                        // update variables after node execution
                        updateProcessVariablesFromContext(basicProcess, nodeID, process, context?.variables)
                        // get nextNodes
                        def nextStates = process.gotoNode(eventID, nodeID)

                        // get assignees for nextNodes if any specified during action execution
                        def nextAssignees = getNextAssignees(nextStates, context?.nextAssignees)

                        // update assignees Map
                        updateProcessAssignees(basicProcess, context.assignees + nextAssignees)

                        // complete executed node
                        completeNode(node, eventID, user)

                        // execute transition for process node with nodeID. NOTE: assignees should be updates before activating new nodes
                        log.debug("Next nodes after transition from node '${nodeID}' of process #${basicProcess.id}(${basicProcess.type}) on event '${eventID}' are '${nextStates}'")
                        nextStates.each {
                            if (completeTransition(node, it, basicProcess, process, user, eventID, context)) {
                                log.debug("Node ${it} for process #${basicProcess.id} is activated")
                            } else {
                                log.debug("Node ${it} for process #${basicProcess.id} is not activated")
                            }
                        }

                        // check if process is complete
                        if (isProcessNodesCompleted(basicProcess)) {
                            completeProcess(basicProcess, user)
                        } else {
                            calculateProcessStatus(basicProcess)
                        }

                        if(!saveProcess(basicProcess, processID)){
                            return ExecutionResultEnum.FAILED_WITH_EXCEPTION.value()
                        }

                        return ExecutionResultEnum.EXECUTED_SUCCESSFULLY.value()
                    } else {
                        log.error("Sending event for process [${processID}] was interrupted unexpectedly!")
                        return ExecutionResultEnum.INTERRUPTED_BY_KILLING.value()
                    }
                } catch (Throwable ex) {
                    log.error("Unexpected exception occurred in synchronized block of node invocation!", ex)
                    return ExecutionResultEnum.FAILED_WITH_EXCEPTION.value()
                } // try-catch
            });
        } finally {
            // If current thread was killed it must clear interrupted status
            // to avoid https://jira.terracotta.org/jira/browse/QTZ-471
            // interrupted() is used instead of isInterrupted() because interrupted status must be cleared
            if (Thread.currentThread().interrupted()) {
                log.warn("The process [${processID}] was killed after node [${nodeID}] was compleated with code ${executionResult}")
                executionResult = ExecutionResultEnum.INTERRUPTED_BY_KILLING.value()
            }
        }
        return executionResult

    } // invokeNodeExecution

    /**
     * Validate and Save the state of Process.
     * Process will be killed If Validation has failed.
     * @param basicProcess
     * @param processId
     * @return
     */
    private boolean saveProcess(def basicProcess, def processId){
        def result = true
        if (!basicProcess.validate()) {
                LogUtils.redirectLogging(getProcessLogFile(basicProcess)) {
                    log.error("Impossible to save Process. Validation is failed: " + basicProcess.errors)
                }
                // discard process changes
                sessionFactory.currentSession.clear()
                basicProcess = BasicProcess.get(processId)
                killedProcess(basicProcess, "exception")
                result = false

        } else {
            BasicProcess.withTransaction {TransactionStatus status ->
                if (!basicProcess.save(flush: true, validation: false)) {
                    status.setRollbackOnly();
                }
            }
        }
        return result
    }

    /**
     * Delegating worklist item to another users/roles/groups
     */
    public boolean forwardProcessNode(ProcessNode node, Collection<String> forwardedTo, String forwardedBy) {
      def processID = node.process.id
      def nodeID = node.id
      def lock = ProcessLock.getProcessLock(processID)
      synchronized(lock) {
        try{
            def forwardedOn = new Date()
            def forwardedNode = ProcessNode.get(nodeID)

            // create copy for old version of updated node
            def newNode = new ProcessNode()
            newNode.properties = forwardedNode.properties["branchID", "nodeID", "type", "description", "status"]

            forwardedNode.addToNextNodes(newNode)
            newNode.addToPreviousNodes(forwardedNode)

            newNode.caller = forwardedBy
            newNode.startedOn = forwardedOn

            forwardedNode.caller = forwardedBy
            forwardedNode.finishedOn = forwardedOn
            forwardedNode.status = getStatus(NodeStatusEnum.FORWARDED.value())
            forwardedNode.event = "FORWARDED" // TODO: think of better event name

            // update assignees
            def assignees = [:]
            assignees.put(newNode.nodeID, forwardedTo?.unique()?.findAll() { StringUtils.isNotEmpty(it) })
            updateProcessAssignees(forwardedNode.process, assignees)

            forwardedNode.process.addToNodes(newNode)

            BasicProcess.withTransaction { status ->
              if (! forwardedNode.process.save(flush: true)) {
                status.setRollbackOnly();
              }
            }
            return Boolean.TRUE
         } catch (Throwable ex){
            log.error("Unexpected exception occurred in synchronized block! Please, contact to administrator. ", ex)
            return Boolean.FALSE
         }
      }
    }

    /*
     * Validates eventID for Node
     */
    private Boolean isSuitableEvent(eventID, nodeID, processClass) {
        if (nodeID == null || eventID == null) {
          return Boolean.TRUE
        } else {
          return new Boolean(processClass.nodes[nodeID]?.transitions?.collect() { it.event }?.contains(eventID))
        }
    }

    /*
     * Store process fields to ProcessVariables
     */
    private void updateProcessVariablesFromContext(basicProcess, nodeID, process, variables) {
        log.debug("Updating variables of process #${basicProcess.id}(${basicProcess.type}) after execution of node '${nodeID}'")
        if (!variables) {
          log.debug("Context variables are empty. Process variables won't be updated")
          return
        }
        basicProcess.variables?.each { var ->
             if (process.isWritable(var.name, nodeID) && variables.keySet().contains(var.name)) {
               def value = variables[var.name]
               log.debug("Update ProcessVariable '${var.name}' with value '${value}' in process #${basicProcess.id}(${basicProcess.type})")
               var.value = value
             }
         }
    }

    /*
     * TODO: think of the conditions
     * Process is suspended if any of it's active nodes is of type "Wait"
     */
    private void calculateProcessStatus(def basicProcess) {
        def nodeActivatedStatus = getStatus(NodeStatusEnum.ACTIVATED.value())
        def activeNodes = basicProcess.nodes?.findAll() {
            it.status == nodeActivatedStatus && it.type == ConstantUtils.NODE_TYPE_WAIT
        }
        if (activeNodes) {
            basicProcess.status = getStatus(ProcessStatusEnum.SUSPENDED.value())
        } else {
            basicProcess.status = getStatus(ProcessStatusEnum.ACTIVATED.value())
        }
    }

    private ProcessNode completeTransition(def fromNode, def toNodeID, def basicProcess, def processInstance,
                                           def user, def eventID, def context) {
        def processClass = processInstance.class
        def destNodeDef = processClass.nodes[toNodeID]
        def description = processInstance.evaluateExpression(processClass.nodeInfos[toNodeID])
        def destNode = null

        if (destNodeDef.type == ConstantUtils.NODE_TYPE_ANDJOIN) {
            def pendingStatus = getStatus(NodeStatusEnum.PENDING.value())
            destNode = basicProcess.nodes.find() {node -> node.nodeID == toNodeID && node.status == pendingStatus}
        }
        if (destNode) {
            fromNode.addToNextNodes(destNode)
            destNode.addToPreviousNodes(fromNode)
        } else {
            destNodeDef.processDef = new ProcessDef(processID:  basicProcess.type)
            destNode = initNewNode(destNodeDef, fromNode, user, description)
            basicProcess.addToNodes(destNode)
        }

        fromNode.event = eventID
        basicProcess.lastModifiedOn = new Date()
        basicProcess.lastModifiedBy = user

        def activatedStatus = getStatus(NodeStatusEnum.ACTIVATED.value())
        if (destNodeDef.type == ConstantUtils.NODE_TYPE_ANDJOIN) {
            if (destNodeDef?.incomingTransitions?.size() == destNode.previousNodes?.size()) {
                log.debug("Activating AndJoin node '${toNodeID}' for process #${basicProcess.id}(${basicProcess.type}). All incoming transitions are complete.")
                destNode.status = activatedStatus
            } else {
                log.debug("Skip activating AndJoin node ${toNodeID} for process #${basicProcess.id}(${basicProcess.type}). Some incoming transitions are complete, node is in pending state.")
            }
        } else if (destNodeDef.type == ConstantUtils.NODE_TYPE_ORJOIN) {
            log.debug("Activating node '${toNodeID}' for process '[key: ${basicProcess.id}, type: ${basicProcess.type}]'")
            def completedStatus = getStatus(NodeStatusEnum.COMPLETED.value())
            def stoppedStatus = getStatus(NodeStatusEnum.STOPPED.value())
            def branchesNodes =  basicProcess.nodes?.findAll() { node ->
                node.status != completedStatus && node.branchID.size() == destNode.branchID.size()+ 1
            }
            branchesNodes?.each() { node ->
                log.debug("Stop execution of node '${node.nodeID}' for process #${basicProcess.id}(${basicProcess.type}). Another branch has reached OrJoin node.")
                node.status = stoppedStatus
            }
        } else {
            log.debug("Activating node '${toNodeID}' for process #${basicProcess.id}(${basicProcess.type})")
        }
        return destNode
    }

    private boolean isProcessNodesCompleted(def process) {
        def notCompleteNodes = process.nodes.findAll() { !it.status.isFinal }
        return notCompleteNodes.size() == 0
    }

    /*
     * Mark process as completed.
     */
    private void completeProcess(def process, def user) {
        process.finishedOn = new Date()
        process.finishedBy = user
        process.status = getStatus(ProcessStatusEnum.COMPLETED.value())
    }

    /*
     * Mark node as completed
     */
    private void completeNode(def node, def event, def user) {
        node.finishedOn = new Date()
        node.caller = user
        node.event = event
        node.status = getStatus(NodeStatusEnum.COMPLETED.value())
    }

    /**
     * Create ProcessVariables based on process instance
     *
     * @param basicProcess - running instance of process class
     * @param process  - process class
     *
     */
    private void defineVariables(def basicProcess, def process) {
        if (!process || !basicProcess) return

        process.class.variables?.each {
            def variable = new ProcessVariable("process": basicProcess, "name": it.name)
            variable.value = process.class.getField(it.name).get(process)
            variable.type = ProcessVariable.defineType(it.type)
            variable.typeName = it.type
            variable.subTypeName = it.subType
            variable.isProcessIdentifier = it.isProcessIdentifier
            basicProcess.addToVariables(variable)
        }
    }

    /**
     * Initialize process node
     *
     * @param process
     * @param status
     * @param nodeID
     * @param user
     * @param description
     */
    private ProcessNode initNewNode(def nodeDef, def fromNode, def user, def description) {
        if (!nodeDef) { return null }

        // create new entry in process nodes
        def processNode = new ProcessNode(nodeDef)
        processNode.caller = user
        processNode.description = description
        processNode.startedOn = fromNode?.finishedOn ?: new Date()

        if (nodeDef.dueDate) {
            String processType = nodeDef.processDef?.processID
            def customizedDueDate = grailsApplication.config.grailsflow.customizedDueDate

            def customizedProcessDueDate = grailsApplication.config.flatten()
                .get("grailsflow.${processType}.customizedDueDate".toString())
            if (customizedProcessDueDate) {
                customizedDueDate = customizedProcessDueDate
            }
            if (customizedDueDate && customizedDueDate instanceof Closure) {
                processNode.dueOn = customizedDueDate(new Date(processNode.startedOn.time+nodeDef.dueDate))
            } else {
                processNode.dueOn = new Date(processNode.startedOn.time+nodeDef.dueDate)
            }
        }

        if (fromNode) {
            fromNode.addToNextNodes(processNode)
            processNode.addToPreviousNodes(fromNode)
        }

        if (fromNode) {
            if (fromNode.type == ConstantUtils.NODE_TYPE_FORK) {
                processNode.branchID = "${fromNode.branchID}${fromNode.nextNodes.size()}"
            } else if (processNode.type in [ConstantUtils.NODE_TYPE_ANDJOIN, ConstantUtils.NODE_TYPE_ORJOIN]
                && fromNode.branchID?.size() > 1) {
                processNode.branchID = "${fromNode.branchID[0..-2]}"
            } else {
                processNode.branchID = fromNode.branchID
            }
        }

        if (nodeDef.type == ConstantUtils.NODE_TYPE_ANDJOIN) {
            processNode.status = getStatus(NodeStatusEnum.PENDING.value())
        } else {
            processNode.status = getStatus(NodeStatusEnum.ACTIVATED.value())
        }
        return processNode
    }

    private Map getVariablesMap(def process) {
        def vars = [:]
        if (!process) return vars

        process?.variables?.each {
            vars[it.name] = it.value
        }
        return vars
    }

    private Map getProcessAssignees(def process) {
       def result = [:]
       process?.assignees?.each() { processAssignee ->
         def nodeID = processAssignee.nodeID
         def assigneeID = processAssignee.assigneeID
         def list = result[nodeID]
         if (!list) {
           list = []
         }
         list << assigneeID
         result.put(nodeID, list)
       }
       return result;
    }

    private Map updateProcessAssignees(def process, Map assignees) {
       // update current assignees
       if (assignees) {
         assignees.each() { nodeID, nodeAssignees ->
            def set = new HashSet()
            // remove old assignees for node
            def oldNodeAssignees = process.assignees?.findAll() { it.nodeID == nodeID.toString() }
            oldNodeAssignees?.each() { assignee ->
              // remove assignee if it's not part of new assignees
              if (!nodeAssignees || !nodeAssignees.contains(assignee.assigneeID) ) {
                process.assignees.remove(assignee)
              } else {
                set << assignee.assigneeID
              }
            }
            // save new assignees for node, skip duplications
            nodeAssignees?.each() {
              if (!set.contains(it)) {
	              def processAssignee = new ProcessAssignee("process": process, "nodeID": nodeID, "assigneeID": it)
	              process.addToAssignees(processAssignee)
	              set << it
              }
            }
         }
       }
       // return updated ProcessAssignees
       return getProcessAssignees(process)
    }

    private Map getNextAssignees(def nodes, def assignees){
      if (!nodes || nodes.size() == 0 || !assignees ) return [:]
      def nextAssignees = [:]
      if (!assignees instanceof List) {
         def list = []
         assignees.split(",").each() {
				  list << it.trim()
         }
         assignees = list
      }
      nodes.each() {
         nextAssignees.put(it, assignees)
      }
      return nextAssignees
    }

    public Collection getSupportedProcessClasses() {
      def types = processFactory.getProcessTypes()
      def classes = []
      types?.each() { type ->
        def process = getProcessClass(type)
        if (process) {
          classes.add(process)
        } else {
          log.error("Cannot get processClass for process type ${type}.")
        }
      }
      return classes
    }

    public Map getSupportedProcessScripts() {
      def types = processFactory.getProcessTypes()
      def scripts = [:]
      types?.each() { type ->
        def process = getProcessClass(type)
        if (process) {
          scripts[type] = process
        } else {
          scripts[type] = null
        }
      }
      return scripts
    }

    private executeNode(ProcessNode node, def processClass, ActionContext context, ProcessNotifier notifier) {
        log.debug("Executing node '${node.nodeID}' of process #${node.process?.id}(${node.process?.type})")
        Closure actionsCode = processClass.nodeActions[node.nodeID]
        // execute actions in separate session (creation a separate thread)
        return Executors.newSingleThreadExecutor( new ThreadFactory() {
            public Thread newThread(Runnable r) {
                SecurityManager s = System.getSecurityManager();
                def group = (s != null)? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
                def namePrefix = "#${node.process?.id}(${node.process?.type})-${node.nodeID}"
                Thread t = new Thread(group, r, namePrefix, 0);
                if (t.isDaemon())
                    t.setDaemon(false);
                if (t.getPriority() != Thread.NORM_PRIORITY)
                    t.setPriority(Thread.NORM_PRIORITY);

                notifier.invocationThreadLock.writeLock().lock()
                notifier.executionThread = t
                notifier.killedByExecutionThread = true
                notifier.invocationThreadLock.writeLock().unlock()
                return t;
            }
        }).submit( {
            def result
            try {
                Session session = SessionFactoryUtils.getNewSession(sessionFactory)
                session.setFlushMode(FlushMode.AUTO);
                //binding a new session on thread
                TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));

                result = LogUtils.redirectLogging(getProcessLogFile(node.process)) {
                    def executionResult
                    try {
                        log.info("Executing node '${node.nodeID}' of process #${node.process?.id}(${node.process?.type})")

                        executionResult = nodeExecutor.execute(actionsCode, context)

                    } catch(Exception e) {
                        log.error("Execution of node '${node.nodeID}' for process #${node.process?.id}(${node.process?.type}) is failed", e)
                        throw e
                    } finally {
                        //unbinding a new session from thread
                        TransactionSynchronizationManager.unbindResource(sessionFactory)
                        try {
                            if(!FlushMode.MANUAL.equals(session.getFlushMode())) {
                                session.flush();
                            }
                        } catch (Exception ex) {
                            log.error("Session flushing after node execution '${node.nodeID}' for process #${node.process?.id}(${node.process?.type}) ) is failed", ex)
                        } finally {
                            //close session in any way
                            SessionFactoryUtils.closeSession(session);
                        }
                    }
                    return executionResult
                }
            } finally {
                notifier.invocationThreadLock.writeLock().lock()
                if (notifier.interrupted) {
                    notifier.invocationThreadLock.writeLock().unlock()

                    Thread killedThread = Thread.start {
                        def session = SessionFactoryUtils.getNewSession(sessionFactory);
                        session.setFlushMode(FlushMode.AUTO);

                        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));

                        threadRuntimeInfoService.invokeInCurrentThread(node.process.id,{ ProcessNotifier _notifier ->
                            try {
                                BasicProcess process = BasicProcess.get(node.process.id)
                                log.debug("Killed process from active node execution #${node.process.id}")
                                killedProcess(process, node.caller)
                            } catch (Throwable ex) {
                                log.error("Unexpected exception occurred in synchronized block! ", ex)
                            } finally {
                                _notifier.unregisterInteres()
                            }
                        })
                        session.flush()
                        TransactionSynchronizationManager.unbindResource(sessionFactory);
                        SessionFactoryUtils.closeSession(session);
                    }
                    killedThread.join()

                } else {
                    notifier.killedByExecutionThread = false
                    notifier.invocationThreadLock.writeLock().unlock()
                }
            }
            return result
        } as Callable ).get()

    }


    /**
     * Checking process identifier
     *
     * Result results:
     *
     * return true if the process identifier is okay
     *
     * @param processType
     * @param variables
     *
     * @return result code
     */
    public Boolean checkProcessIdentifier(def processType, def variables) {
        def processClass = getProcessClass(processType)

        if (!processClass) {
            errors << "Cannot get ProcessClass for type ${processType}"
            log.error("Cannot get ProcessClass for type ${processType}")
            return null
        }

        // before saving process we need to check the process identifiers
        def processIdentifiers = processClass.processIdentifier
        if (!processIdentifiers.isEmpty()) {
            def processInstance = processClass?.newInstance()
            if (!processInstance) return Boolean.FALSE

            StringBuffer query = new StringBuffer()
            query.append(""" select count(distinct b) from com.jcatalog.grailsflow.model.process.BasicProcess b where
                            (b.status in (:statuses)) and (b.type = (:processType))
                        """)

            def params = [:]
            StringBuffer restrictions = new StringBuffer()
            def index = 0
            processIdentifiers.each() { identVar ->
                index ++
                if (params != [:]) restrictions.append(" and pvar${index-1}.process in (")
                restrictions.append("""select pvar${index}.process from com.jcatalog.grailsflow.model.process.ProcessVariable as pvar${index}
                                       where pvar${index}.name = (:${identVar}_name)
                                       and pvar${index}.variableValue """)
                params["${identVar}_name"] = identVar

                def value
                if (variables && variables[identVar]) {
                    value = variables[identVar]
                } else {
                   def variable = new ProcessVariable()
                   variable.value = processClass.getField(identVar).get(processInstance)
                   value = variable.variableValue
                }

                if (value) {
                  restrictions.append("= (:${identVar}_value)")
                  params["${identVar}_value"] = value
                } else  restrictions.append(" is null")

                restrictions.append(" and pvar${index}.isProcessIdentifier = (:isKey)")
            }

            for (int i=1; i<=index-1; i++) {
                restrictions.append(")")
            }

            query.append(" and (b in (${restrictions}))")

            params.processType = processType
            params.statuses = [getStatus(ProcessStatusEnum.ACTIVATED.value()),
                               getStatus(ProcessStatusEnum.SUSPENDED.value())]
            params.isKey = Boolean.TRUE

            def keyCopies = BasicProcess.executeQuery(query.toString(), params).get(0)
            if (keyCopies != 0) {
                return Boolean.FALSE
            }

            return Boolean.TRUE
        }
        return Boolean.TRUE
    }

    /**
     * Serves for searching and deletion of unnecessary node assignees.
     * It means that if node has multiple assignees one of them will be responsible for this node. Such user will be the only assignee for it
     * and other assignees won't have more access to it.
     *
     * @param processID Process key (Long type)
     * @param nodeID Node ID (String type). Example: "TestApproval"
     * @param user Assignee that will be the only responsible for provided node.
     * @param locale Locale for messages and errors representation.
     * @param excludedRoles Roles that will not be taken into account on assignee search(roles that mustn't be deleted from assignees). Optional.
     * @param excludedGroups Groups that will not be taken into account on assignee search(groups that mustn't be deleted from assignees). Optional.
     * @param excludedUsers Users that will not be taken into account on assignee search(users that mustn't be deleted from assignees). Optional.
     * @return Map that contains <i>message</i> field and <i>errors</i> & <i>warnings</i> collection.
     */
    def reserveNode(String processID, String nodeID, String user, Locale locale, List excludedRoles = null, List excludedGroups = null, List excludedUsers = null) {

        def result = [:]

        BasicProcess process = BasicProcess.get(processID)

        if (!process) {
            result.error = messageSource.getMessage('plugin.grailsflow.message.errorProcessId', [processID] as Object[], locale)
            return result
        }

        Collection<ProcessAssignee> assigneesToRemove = ProcessAssignee.createCriteria().list {
            eq('process', process)
            eq('nodeID', nodeID)

            not {
                or {
                    eq('assigneeID', AuthoritiesUtils.getUserAuthority(user))
                    eq('assigneeID', AuthoritiesUtils.getRoleAuthority("ADMIN"))

                    if (excludedRoles) {
                        'in'('assigneeID', AuthoritiesUtils.getRoleAuthorities(excludedRoles))
                    }

                    if (excludedGroups) {
                        'in'('assigneeID', AuthoritiesUtils.getGroupAuthorities(excludedGroups))
                    }

                    if (excludedUsers) {
                        'in'('assigneeID', AuthoritiesUtils.getUserAuthorities(excludedUsers))
                    }
                }
            }
        }

        if (!assigneesToRemove) {
            result.warning = messageSource.getMessage('plugin.grailsflow.message.reservation.noOneFound', [nodeID, processID] as Object[], locale)
            return result
        }

        assigneesToRemove*.delete(flush: true)

        result.message = messageSource.getMessage('plugin.grailsflow.message.reservation.nodeReserved', [processID, nodeID, user] as Object[], locale)

        return result
    }

    def getRunningProcesses(Integer max, Closure restrictions = null) {
        def completedStatus = getStatus(ProcessStatusEnum.COMPLETED.value())
        def killedStatus = getStatus(ProcessStatusEnum.KILLED.value())

        def criteria = BasicProcess.createCriteria()
        return criteria.list {
            ne("status", completedStatus)
            ne("status", killedStatus)
            restrictions?.call(criteria)
            if (max) {
                maxResults(max)
            }
        }
    }

    def getProcessesExceptKilled(Integer max, Closure restrictions = null) {
        def killedStatus = getStatus(ProcessStatusEnum.KILLED.value())
        def criteria = BasicProcess.createCriteria()
        return criteria.list {
            ne("status", killedStatus)
            restrictions?.call(criteria)
            if (max) {
                maxResults(max)
            }
        }
    }

    private FlowStatus getStatus(String statusID) {
        return FlowStatus.findByStatusID(statusID)
    }

    private boolean canNewThreadBeStarted(Integer sizeOfRunningThreads) {
        if (grailsApplication.config.grailsflow.threads.maxQuantity instanceof Closure){
            return sizeOfRunningThreads < grailsApplication.config.grailsflow.threads.maxQuantity()
        } else {
            return sizeOfRunningThreads < maxThreadsQuantity
        }
    }

}

