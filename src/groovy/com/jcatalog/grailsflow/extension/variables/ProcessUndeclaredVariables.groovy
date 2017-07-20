package com.jcatalog.grailsflow.extension.variables

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.commons.lang.StringUtils

import com.jcatalog.grailsflow.model.process.ProcessNode
import com.jcatalog.grailsflow.model.process.BasicProcess
import com.jcatalog.grailsflow.model.process.ProcessVariable

/**
 * ProcessUndeclaredVariables bean stores new undeclared ProcessVariable for defined node and process.
 * And allows to get value of the variable and the owner who created it for defined node.
 *
 * @author Tanya Suhoverhaya
 */
class ProcessUndeclaredVariables {
    private static final Log log = LogFactory.getLog(ProcessUndeclaredVariables.getClass())

    def processManagerService

    private static BasicProcess getBasicProcess(Long processID) {

        if (!processID) {
            log.error "Param 'ProcessID' is not passed"
            return
        }

        BasicProcess process = BasicProcess.get(processID)
        if (!process) {
            log.error "Process with processID ${processID} was not found"
            return null
        }

        return process
    }

    private static ProcessNode getProcessNode(BasicProcess process, String nodeID) {
        ProcessNode node = ProcessNode.findWhere("nodeID": nodeID, "process": process)
        if (!node) {
            log.error "Process does not have a node with nodeID: ${nodeID}"
            return null
        }
        return node
    }

    /**
     * Get creator (caller) of undeclared variable
     *
     * @param variableName - name of undeclared variable
     *
     * @return creator (caller) of undeclared variable
     */
    public static String getUndeclaredProcessVariableCaller(String variableName) {

        String variableCaller = ""
        if (variableName) {
            ProcessNode node = ProcessNode.get(StringUtils.substringAfterLast(variableName, "_"))
            if (node) {
                variableCaller = node.caller
            }
        }
        return variableCaller
    }

    /**
     * Get value of specified undeclared variable for named process node
     *
     * @param processID - is a unique key of process
     * @param variableName - name of variable
     * @param nodeID - specific name of process node
     *
     * @return value of undeclared variable for specified process node
     */
    public static String getUndeclaredProcessVariableValue(Long processID, String variableName, String nodeID) {
        String variableValue = ""
        if (nodeID) {
            Map variableMap = getUndeclaredProcessVariableValues(processID, variableName, nodeID)
            if (!variableMap.isEmpty()) {
                variableValue = variableMap.values()[0]?.value
            }
        }
        return variableValue
    }

    /**
     * Get all undeclared variables with specified name
     *
     * @param processID - is a unique key of process
     * @param variableName - name of variable
     * @param nodeID - specific name of process node. Can be empty
     *
     * @return Map of undeclared variables values
     */
    public static Map<String, Object> getUndeclaredProcessVariableValues(Long processID, String variableName, String nodeID = null) {

        Map<String, Object> variablesMap = [:]

        if (!variableName) {
            log.error "Name of undeclared process variable is not defined"
            return variablesMap
        }

        BasicProcess process = getBasicProcess(processID)
        if (!process) {
            return variablesMap
        }

        ProcessNode node = nodeID ? getProcessNode(process, nodeID) : null

        String searchTerm = "${variableName}_${node ? node.id : ''}"
        def variables = process?.variables.findAll{ node ? it.name == searchTerm : it.name.startsWith(searchTerm) }

        variables?.each { variable ->
            def name = variable.name
            def value = ProcessVariable.getConvertedValue(variable.variableValue, ProcessVariable.defineType(variable.typeName))
            def caller = getUndeclaredProcessVariableCaller(name)

            variablesMap.put(name, ["value": value, "caller": caller])
        }

        return variablesMap
    }

    /**
     * Stored undeclared variable as '<variableName>_<processNode.id>' into DB
     *
     * @param processID - is a unique key of process
     * @param nodeID - name of process node
     * @param variableName - name of new variable
     * @param variableValue - value of new variable as string
     * @param variableType - type of new variable, e.g. 'String', 'Double', 'Date' and so on as a simple class name. By default it's a 'String' type
     *
     * @return true - when new variable stored successfully, false - otherwise
     */
    public static Boolean saveUndeclaredProcessVariable(Long processID, String nodeID, String variableName, String variableValue, String variableType = "String") {

        BasicProcess process = getBasicProcess(processID)
        if (!process) {
            return false
        }
        ProcessNode node = getProcessNode(process, nodeID)
        if (!node) {
            return false
        }

        try {
            ProcessVariable processVariable = new ProcessVariable(
                    process: process,
                    name: "${variableName}_${node.id}",
                    variableValue: ProcessVariable.getConvertedValue(variableValue, variableType),
                    typeName: variableType,
                    type: ProcessVariable.defineType(variableType)).save(validate: false)
        } catch (Exception ex) {
            log.error "An error occured while saving undeclared variable ${variableValue} with type ${variableType} and value - ${variableValue}: ${ex.toString()}"
            return false
        }
        return true
    }
}