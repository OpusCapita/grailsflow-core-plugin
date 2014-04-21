import com.jcatalog.grailsflow.test.SharedTestArea

class ManagedActionTestProcess{

    def descriptions = {
        ManagedActionTest(description_en : "description..." )
    }

    def views = {
    }

    def ManagedActionTestProcess = {
        initialize(isStart: true) {
            action {
                Object mutexInitStarted = SharedTestArea.sharedArea.get("initStarted"),
                        mutexInitFinish = SharedTestArea.sharedArea.get("initFinish"),
                        mutexInitFinished = SharedTestArea.sharedArea.get("initFinished");

                synchronized (mutexInitStarted){
                    mutexInitStarted.notifyAll();
                }
                synchronized (mutexInitFinish){
                    mutexInitFinish.wait(1000);
                }
                synchronized (mutexInitFinished){
                    mutexInitFinished.notifyAll();
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