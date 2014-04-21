import com.jcatalog.grailsflow.utils.ConstantUtils

class SleepInterruptedActionTestProcess {

    public Integer var1
    public Integer var2
    public Integer var3
    public Integer var4
    public Integer var5
    public String str1
    public String str2
    public String str3
    public String str4
    public String str5
    
    def descriptions = {
        SleepInterruptedActionTest(description_en : "description..." )
    }
    
    def views = {
    }
    
    def SleepInterruptedActionTestProcess = {
        initialize() {
            action {
                Log(logMessage: "Sleep test process has been started.")
                try {
                    EndlessCircle()
                } catch (Exception ie) {
                    println("Action thread was interrupted!")
                    throw ie
                }
                return "okay"
            }
            on("okay").to([ "finishOk" ])
        }
     
               
        finishOk() {
            action {
                Log(logMessage: "Sleep test process has been finished.")
            }
        }
    }
}
