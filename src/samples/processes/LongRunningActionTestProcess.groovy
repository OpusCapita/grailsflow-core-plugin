import com.jcatalog.grailsflow.test.SharedTestArea

class LongRunningActionTestProcess{

    def descriptions = {
        LongRunningActionTestProcess(description_en : "LongRunningActionTestProcess definition description..." )
    }

    def views = {
    }

    def LongRunningActionTestProcess = {
        initialize() {
            action {
                Object mutexInitStarted = SharedTestArea.sharedArea.get("initStarted")

                synchronized (mutexInitStarted){
                    mutexInitStarted.notifyAll();
                }

                while(1){

                    Thread.sleep(10)
                }
            }
            on("okay").to([ "finishOk" ])
        }
        finishOk() {
            action {

            }
        }

    }
}