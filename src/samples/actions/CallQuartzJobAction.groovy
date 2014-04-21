package asynchronous 

import com.jcatalog.grailsflow.actions.AsynchronousAction

class CallQuartzJobAction extends AsynchronousAction {
    public String name
    public Integer age

    Object execute(){
        name = "Ann Jons"
        age = age ? age + 4 : 40
        return "okay"
    }

    Class getScheduledJobClass() {
        return com.jcatalog.grailsflow.demo.jobs.PrintMessageJob
    }
}