import com.jcatalog.grailsflow.utils.ConstantUtils

class KillingTestProcess {

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
        KillingTest(description_en : "description..." )
    }
    
    def views = {
    }
    
    def KillingTestProcess = {
        startWait() {
            on('LongExecution').to([ 'LongExecution' ])
            on('ExecutionThrowError').to([ 'ExecutionThrowError' ])
        }

        LongExecution() {
            action {
                try {
                    while(1) {
                        println("....................Action thread is running!")
                        Thread.sleep(500)
                    }
                } catch (Exception e) {
                    println("...........ERROR:..............Action is finished")
                    Thread.sleep(10000)
                }
                println("..................Action is finished")
                return "okay"
            }
            on("okay").to([ "finishOk" ])
        }

        ExecutionThrowError() {
            action {
                try {
                    while(1) {
                        println("....................Action thread is running!")
                        Thread.sleep(500)
                    }
                } catch (Exception e) {
                    println("...........ERROR:..............Action is finished")
                    throw new Exception(e.message)
                }
                println("..................Action is finished")
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
