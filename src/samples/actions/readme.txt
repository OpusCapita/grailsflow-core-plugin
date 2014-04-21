'actions' folder of workarea contains Actions that can be called from the processes.


Action class name should end with 'Action' suffix.
Actions should extend com.jcatalog.grailsflow.actions.Action
Action class can belong to package, but action class name must be unique  
Public properties of action class are parameters that are available for action call.
Action logic should be implemented in execute() method
Calling of the action from the process will create new object of action class,
set passed values to action properties and call action's execute() method.

Action example:

class LogAction extends com.jcatalog.grailsflow.actions.Action{
    public String logMessage

    def execute(){
        println logMessage
        return null
    }
}

Calling of the action in process code:

Log(logMessage: 'Hello world!')


Following variables are available for actions:

* actionContext -- object of com.jcatalog.grailsflow.actions.ActionContext class
                   that contains service data:
    * processID -- ID of the running process that calls action
    * nodeID -- ID (name) of the node that calls action
    * user -- name of the user

    * assignees -- map of assignees for running process
    * nextAssignees -- variable for managing assignees for next node(s)

    * variables -- map of current values of process variables 
 