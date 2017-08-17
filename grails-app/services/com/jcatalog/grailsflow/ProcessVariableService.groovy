package com.jcatalog.grailsflow

import com.jcatalog.grailsflow.model.process.BasicProcess
import com.jcatalog.grailsflow.model.process.ProcessNode
import com.jcatalog.grailsflow.model.process.ProcessVariable

class ProcessVariableService {

    static transactional = false

    /**
     * Stored new process variable
     *
     * @param process - BasicProcess for which new variable should be stored to
     * @param variableName - name of new variable
     * @param variableValue - value of new variable
     *
     * @return new ProcessVariable
     */
    public ProcessVariable saveProcessVariable(BasicProcess process, String variableName, def variableValue) {

        Objects.requireNonNull(process, "Process can't be null")
        Objects.requireNonNull(variableName, "ProcessVariable name can't be null")
        Objects.requireNonNull(variableValue, "ProcessVariable value can't be null")

        ProcessVariable processVariable = new ProcessVariable(
                process: process,
                name: variableName,
                type: ProcessVariable.defineType(variableValue.class.simpleName)
        )

        processVariable.value = variableValue
        processVariable.save(validate: false)

        return processVariable
    }

    /**
     * Stored new process variable as <variableName_node.id> for specified node
     *
     * @param node - ProcessNode of the process
     * @param variableName - name of new variable
     * @param variableValue - value of new variable
     *
     * @return new ProcessVariable
     */
    public ProcessVariable saveProcessNodeVariable(ProcessNode node, String variableName, def variableValue) {

        Objects.requireNonNull(node, "ProcessNode can't be null")
        Objects.requireNonNull(variableName, "ProcessVariable name can't be null")
        Objects.requireNonNull(variableValue, "ProcessVariable value can't be null")

        variableName = "${variableName}_${node.id}"

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

        Objects.requireNonNull(node, "ProcessNode can't be null")
        Objects.requireNonNull(variableName, "ProcessVariable name can't be null")

        List<ProcessVariable> processVariables = ProcessVariable.withCriteria {
            eq('process', node.process)
            eq('name', "${variableName}_${node.id}")
        }
        return processVariables
    }
}