import com.jcatalog.grailsflow.actions.AsynchronousAction
import org.apache.commons.logging.LogFactory

class SleepAction extends AsynchronousAction {
    public Integer ms
    protected static def log = LogFactory.getLog(SleepAction.class)

    Object execute(){
        log.info("...SLEEPING...")
        Thread.sleep(ms)
        return "okay"
    }
    
    Class getScheduledJobClass() {
        return com.jcatalog.grailsflow.demo.jobs.PrintMessageJob
    }
}