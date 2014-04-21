import com.jcatalog.grailsflow.test.SharedTestArea
import java.util.concurrent.Semaphore


// startedSemaphore and startSemaphore are used for externally control the process nodes execution
class ManagedActionForkJoinTestProcess {

    def descriptions = {
        ManagedActionForkJoinTest(description_en : "description..." )
    }
    
    def ManagedActionForkJoinTestProcess = {
        initializeFork(isStart: true) { 
            action {
              
                Semaphore errorConditionSemaphore = new Semaphore(1)
                SharedTestArea.sharedArea.put("errorConditionSemaphore", errorConditionSemaphore)
                
                
                //immediatedly accuire semaphore to be bloced in all
                //execution nodes to simulate "running" state
                Semaphore nodeRunningSemaphore = new Semaphore(1)
                SharedTestArea.sharedArea.put("nodeRunningSemaphore", nodeRunningSemaphore)
                nodeRunningSemaphore.acquire()
                
                SharedTestArea.sharedArea.get("startedSemaphore")?.release()
                SharedTestArea.sharedArea.get("startSemaphore")?.acquire()
                
                return "okay"
            }
            def followingNodes = (1..10).collect{ "n${it}" }
            on("okay").to(followingNodes)
        }
        
        n1() {
            action {
                SharedTestArea.sharedArea.get("startedSemaphore")?.release()
                SharedTestArea.sharedArea.get("nodeRunningSemaphore").acquire()
                return "okay"
            }
            on("okay").to(["finish"])
        }
        /*
        I don't know why dynamically nodebuilder does not work propertly
        (2..10).each(){
          def node = "${it}n"
          "${node}"(){
            action {
                Semaphore startSemaphore = SharedTestArea.sharedArea.get("startSemaphore")
                startSemaphore.acquire()
                return "okay"
            }
            on("okay").to(["finish"])
          }
        }
        */
        n2(){
            action {
                SharedTestArea.sharedArea.get("nodeRunningSemaphore").acquire()
                return "okay"
            }
            on("okay").to(["finish"])
        }
        n3(){
            action {
                SharedTestArea.sharedArea.get("nodeRunningSemaphore").acquire()
                return "okay"
            }
            on("okay").to(["finish"])
        }
        n4(){
            action {
                SharedTestArea.sharedArea.get("nodeRunningSemaphore").acquire()
                return "okay"
            }
            on("okay").to(["finish"])
        }
        n5(){
            action {
                SharedTestArea.sharedArea.get("nodeRunningSemaphore").acquire()
                return "okay"
            }
            on("okay").to(["finish"])
        }
        n6(){
            action {
                SharedTestArea.sharedArea.get("nodeRunningSemaphore").acquire()
                return "okay"
            }
            on("okay").to(["finish"])
        }
        n7(){
            action {
                SharedTestArea.sharedArea.get("nodeRunningSemaphore").acquire()
                return "okay"
            }
            on("okay").to(["finish"])
        }
        n8(){
            action {
                SharedTestArea.sharedArea.get("nodeRunningSemaphore").acquire()
                return "okay"
            }
            on("okay").to(["finish"])
        }
        n9(){
            action {
                SharedTestArea.sharedArea.get("nodeRunningSemaphore").acquire()
                return "okay"
            }
            on("okay").to(["finish"])
        }
        n10(){
            action {
                SharedTestArea.sharedArea.get("nodeRunningSemaphore").acquire()
                SharedTestArea.sharedArea.get("errorConditionSemaphore").acquire()
                return "okay"
            }
            on("okay").to(["finish"])
        }
        
        finishOrJoin(isFinal: true) {
            action {
              SharedTestArea.sharedArea.get("errorConditionSemaphore").acquire()
            }
        }
        
    }
}
