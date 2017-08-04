package com.jcatalog.grailsflow

import com.jcatalog.grailsflow.model.process.BasicProcess
import com.jcatalog.grailsflow.model.process.ProcessNode
import com.jcatalog.grailsflow.model.process.ProcessVariable
import org.apache.commons.collections.ListUtils

class ProcessVariableService {

    static transactional = false

    /**
     * Stored new process variable
     *
     * @param process - BasicProcess for which new variable should be stored to
     * @param variableName - name of new variable
     * @param variableValue - value of new variable
     *
     * @return ProcessVariable - when variable stored successfully, null - otherwise
     */
    public ProcessVariable saveProcessVariable(BasicProcess process, String variableName, def variableValue) {

        if (!process || !variableName || !variableValue) {
            return null
        }

        ProcessVariable processVariable
        try {
            def variableTypeName = variableValue.class.simpleName
            def variableType = ProcessVariable.defineType(variableTypeName)

            processVariable = new ProcessVariable("process": process)
            processVariable.name = variableName
            processVariable.type = variableType
            processVariable.typeName = variableTypeName

            if (variableType == ProcessVariable.LIST) {
                processVariable.typeName = List.class.simpleName
                processVariable.subTypeName = variableValue[0]?.class?.simpleName
            }

            processVariable.value = variableValue
            processVariable.save(validate: false)

        } catch (Exception ex) {
            log.error "An error occured while saving process node variable ${variableName} with value - ${variableValue}: ${ex.toString()}"
            return null
        }
        return processVariable
    }

    /**
     * Stored new process variable as <variableName_node.id> for specified node
     *
     * @param node - ProcessNode of the process
     * @param variableName - name of new variable
     * @param variableValue - value of new variable
     *
     * @return ProcessVariable - when variable stored successfully, null - otherwise
     */
    public ProcessVariable saveProcessNodeVariable(ProcessNode node, String variableName, def variableValue) {

        if (!node || !variableName || !variableValue) {
            return null
        } else {
            variableName = "${variableName}_${node.id}"
        }

        return saveProcessVariable(node.process, variableName, variableValue)
    }

    /**
     * Looking for ProcessVariable with defined process with name <variableName_node.id>
     *
     * @param node - ProcessNode of the process
     * @param variableName - name of new variable
     *
     * @return List of all process variables with name <variableName_node.id>, null - otherwise
     */
    public List<ProcessVariable> findAllProcessNodeVariables(ProcessNode node, String variableName) {

        if (!node || !variableName) {
            return ListUtils.EMPTY_LIST
        }

        List<ProcessVariable> processVariables = ProcessVariable.withCriteria {
            eq('process', node?.process)
            like('name', "${variableName}_${node.id}%")
        }
        return processVariables ?: ListUtils.EMPTY_LIST
    }
}
