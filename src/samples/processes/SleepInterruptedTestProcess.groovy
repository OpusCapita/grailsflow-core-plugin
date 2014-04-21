import com.jcatalog.grailsflow.utils.ConstantUtils

class SleepInterruptedTestProcess {

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
        SleepInterruptedTest(description_en : "description..." )
    }
    
    def views = {
    }
    
    def SleepInterruptedTestProcess = {
        initialize() {
            action {
                Log(logMessage: "Sleep test process has been started.")
                Thread internalThread
                try {
                    internalThread = Thread.start {
                        try {
                            while(1) {
                                Thread.sleep(200)
                                println("Internal thread is running!")
                            }
                        } catch (InterruptedException ie) {
                            println("Internal thread was interrupted!")
                        }
                    }
                    while(1) {
                        Thread.sleep(100)
                        println("Thread is running!")
                    }

                } catch (Exception ie) {
                    internalThread.interrupt()
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
