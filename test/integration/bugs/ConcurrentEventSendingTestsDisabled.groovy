package bugs

import org.springframework.orm.hibernate3.SessionFactoryUtils
import org.springframework.orm.hibernate3.SessionHolder
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.hibernate.FlushMode
import com.jcatalog.grailsflow.model.process.BasicProcess
import com.jcatalog.grailsflow.status.ProcessStatusEnum
import com.jcatalog.grailsflow.model.process.*
import java.util.concurrent.Semaphore
import com.jcatalog.grailsflow.engine.execution.ExecutionResultEnum
import com.jcatalog.grailsflow.test.SharedTestArea

import java.util.concurrent.TimeUnit
import java.util.concurrent.CountDownLatch

/**
 * Tests bug described in GFW-209
 */

// currently disabled
class ConcurrentEventSendingTestsDisabled extends processes.AbstractProcessTestCase {

    void testConcurrentKill() {
        def id = processManagerService.startProcess("SleepTest", "admin", null)
        assertNotNull id

        // error code
        Thread.start {
            def session = SessionFactoryUtils.getNewSession(sessionFactory);
            session.setFlushMode(FlushMode.AUTO);

            TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
            assert processManagerService.killProcess(id, "admin") == Boolean.TRUE

            session.flush()
            TransactionSynchronizationManager.unbindResource(sessionFactory);
            SessionFactoryUtils.closeSession(session);
        }
        sleep(200)
        Thread.start {
            def session = SessionFactoryUtils.getNewSession(sessionFactory);
            session.setFlushMode(FlushMode.AUTO);

            TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
            int code = processManagerService.sendEvent(id, "initialize", null, "admin")
            assert code == ExecutionResultEnum.PROCESS_KILLED.value()

            session.flush()
            TransactionSynchronizationManager.unbindResource(sessionFactory);
            SessionFactoryUtils.closeSession(session);
        }
        sleep(500)

        assert ExecutionResultEnum.EXECUTED_SUCCESSFULLY.value() != processManagerService.sendEvent(id, "initialize", null, "admin")
        assert BasicProcess.get(id).status.statusID == ProcessStatusEnum.KILLED.value()

    }

    void testConcurrentEventSending() {
        def id = processManagerService.startProcess("SleepTest", "admin", null)
        assertNotNull id

        (1..5).each { thread ->
            Thread.start {
                def session = SessionFactoryUtils.getNewSession(sessionFactory);
                session.setFlushMode(FlushMode.AUTO);

                TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
                int code = processManagerService.sendEvent(id, "initialize", null, "admin")
                if (thread == 1) {
                    assert code == ExecutionResultEnum.EXECUTED_SUCCESSFULLY.value()
                } else {
                    assert code != ExecutionResultEnum.EXECUTED_SUCCESSFULLY.value()
                }

                session.flush()
                TransactionSynchronizationManager.unbindResource(sessionFactory);
                SessionFactoryUtils.closeSession(session);
            }
            sleep(30)
        }

        sleep(200)
        assert ExecutionResultEnum.EXECUTED_SUCCESSFULLY.value() != processManagerService.sendEvent(id, "initialize", null, "admin")

    }

    void testThreadsDetailsPool() {
        def id1 = processManagerService.startProcess("SleepTest", "admin", null)
        def id2 = processManagerService.startProcess("SleepTest", "admin", null)
        def id3 = processManagerService.startProcess("SleepTest", "admin", null)

        int code1 = processManagerService.sendEvent(id1, "initialize", null, "admin")
        assert code1 == 0
        int code2 = processManagerService.sendEvent(id2, "initialize", null, "admin")
        assert code2 == 0


        assert threadRuntimeInfoService.isExecuting(id1) == Boolean.FALSE
        assert threadRuntimeInfoService.isExecuting(id2) == Boolean.FALSE

        Thread.start {
            def session = SessionFactoryUtils.getNewSession(sessionFactory);
            session.setFlushMode(FlushMode.AUTO);

            TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
            int code = processManagerService.sendEvent(id3, "initialize", null, "admin")
            assert code == 9

            session.flush()
            TransactionSynchronizationManager.unbindResource(sessionFactory);
            SessionFactoryUtils.closeSession(session);
        }
        sleep(100)
        assert threadRuntimeInfoService.isExecuting(id3) == Boolean.TRUE

        BasicProcess.withTransaction { status ->
            status.setRollbackOnly();
        }
        assert processManagerService.killProcess(id3, "admin") == Boolean.TRUE
      //  assert threadRuntimeInfoService.isExecuting(id3) == Boolean.FALSE
    }

    public testManagedAction(){
        def id = processManagerService.startProcess("ManagedActionTest", "admin", null)
        assert threadRuntimeInfoService.isExecuting(id) == Boolean.FALSE
        assert threadRuntimeInfoService.isExecutingOrRecentlyFinished(id) == Boolean.TRUE
        Object mutexInitStarted = new Object(), mutexInitFinish = new Object(), mutexInitFinished = new Object();

        SharedTestArea.sharedArea.put("initStarted", mutexInitStarted);
        SharedTestArea.sharedArea.put("initFinish", mutexInitFinish);
        SharedTestArea.sharedArea.put("initFinished", mutexInitFinished);

        Thread.start {
            def session = SessionFactoryUtils.getNewSession(sessionFactory);
            session.setFlushMode(FlushMode.AUTO);

            TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
            int code = processManagerService.sendEvent(id, "initialize", null, "admin")

            session.flush()
            TransactionSynchronizationManager.unbindResource(sessionFactory);
            SessionFactoryUtils.closeSession(session);
        }
        synchronized (mutexInitStarted){
            mutexInitStarted.wait(1000);
        }
        assert threadRuntimeInfoService.isExecuting(id) == Boolean.TRUE
        assert threadRuntimeInfoService.isExecutingOrRecentlyFinished(id) == Boolean.TRUE
        synchronized (mutexInitFinish){
            mutexInitFinish.notifyAll();
        }

        synchronized (mutexInitFinished){
            mutexInitFinished.wait(1000);
        }
        sleep(100);
        assert threadRuntimeInfoService.isExecuting(id) == Boolean.FALSE
        assert threadRuntimeInfoService.isExecutingOrRecentlyFinished(id) == Boolean.TRUE
        threadRuntimeInfoService.clear()
        assert threadRuntimeInfoService.isExecutingOrRecentlyFinished(id) == Boolean.FALSE
    }
    
    private doInNewThread(Closure c){
        Thread.start {

            def session = SessionFactoryUtils.getNewSession(sessionFactory)
            session.setFlushMode(FlushMode.AUTO);

            TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session))
            c.call()

            session.flush()
            TransactionSynchronizationManager.unbindResource(sessionFactory)
            SessionFactoryUtils.closeSession(session)

        }
    }
    
    public testManagedActionForForkProcess(){
        def id = processManagerService.startProcess("ManagedActionForkJoinTest", "admin", null)
        def nodesCount = 7;
        assert threadRuntimeInfoService.isExecuting(id) == Boolean.FALSE
        assert threadRuntimeInfoService.isExecutingOrRecentlyFinished(id) == Boolean.TRUE
        
        Semaphore startedSemaphore = new Semaphore(1), startSemaphore = new Semaphore(1)
        SharedTestArea.sharedArea.put("startedSemaphore", startedSemaphore)
        SharedTestArea.sharedArea.put("startSemaphore", startSemaphore)
        
        //next acquire call will be blocked until semaphore will be released in initialize of ManagedActionForkJoinTest  
        startedSemaphore.acquire()
        startSemaphore.acquire()

        doInNewThread({
          assert ExecutionResultEnum.EXECUTED_SUCCESSFULLY.value() == processManagerService.sendEvent(id, "initialize", null, "admin");
        });
        //check #1 : we are in initialize
        //blocks until it is released in initialize of ManagedActionForkJoinTest
        assert startedSemaphore.tryAcquire(1000L, TimeUnit.MILLISECONDS)
        assert threadRuntimeInfoService.isExecuting(id) == Boolean.TRUE
        
        Semaphore errorConditionSemaphore = SharedTestArea.sharedArea.get("errorConditionSemaphore");

        startSemaphore.release()
        assert threadRuntimeInfoService.isExecuting(id) == Boolean.TRUE
        
        //check #2 : execute simultaniously all nodes
        sleep(100); //sleep is required to allow "initialize" to complete and save (persist) "n1" node
        
        //the 1st node has priority (to lock and continue in the test)
        doInNewThread({
          assert ExecutionResultEnum.INTERRUPTED_BY_KILLING.value() == processManagerService.sendEvent(id, "n1", null, "admin");
        });

        CountDownLatch doneSignal = new CountDownLatch(6);

        //execute all other nodes
        (2.. nodesCount).each{ n ->
            Thread.start {
                try {
                    def session = SessionFactoryUtils.getNewSession(sessionFactory)
                    session.setFlushMode(FlushMode.AUTO);

                    TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session))

                    assert ExecutionResultEnum.INTERRUPTED_BY_KILLING.value() == processManagerService.sendEvent(id, "n${n}", null, "admin");

                    session.flush()
                    TransactionSynchronizationManager.unbindResource(sessionFactory)
                    SessionFactoryUtils.closeSession(session)

                } catch (InterruptedException ex) {
                } finally{
                    doneSignal.countDown()
                }
            }
        }
        doneSignal.await(3000, TimeUnit.MILLISECONDS)

        sleep(1000)  //sleep is required to allow all nodeThreads to be launched and being blocked
        assert threadRuntimeInfoService.isExecuting(id) == Boolean.TRUE
        
        //now executing of all running threads is blocked by startSemaphore. That means that
        //the process and nodes are still executing

        //check #3 : killing node
        assert processManagerService.killProcess(id, "admin") == Boolean.TRUE
        if (threadRuntimeInfoService.isExecuting(id)) {
            assert threadRuntimeInfoService.processData.get(id).interrupted == Boolean.TRUE
        }
        assert threadRuntimeInfoService.isExecutingOrRecentlyFinished(id) == Boolean.TRUE
        //no error condition was met
        assert errorConditionSemaphore.tryAcquire(1000L, TimeUnit.MILLISECONDS)  == Boolean.TRUE

        //check #4 : the event has been sent to already killed process
        def lastNode = ++nodesCount;
        doInNewThread({
          assert ExecutionResultEnum.PROCESS_KILLED.value() == processManagerService.sendEvent(id, "n${lastNode}", null, "admin");
        });

        //check #last : check killed status of all nodes
         (1..nodesCount).each{ n ->
            def node = ProcessNode.findByProcessAndNodeID(BasicProcess.get(id), "n${n}")
            assert node != null
            // TODO: status for node can be KILLING
            // assert node.status.statusID == NodeStatusEnum.KILLED.value()
         }
    }

    public testKillingLongRunningExecution(){
        def id = processManagerService.startProcess("LongRunningActionTest", "admin", null)
        assertNotNull id

        Object mutexInitStarted = new Object();
        SharedTestArea.sharedArea.put("initStarted", mutexInitStarted);
        Thread.start {
            def session = SessionFactoryUtils.getNewSession(sessionFactory);
            session.setFlushMode(FlushMode.AUTO);

            TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
            int code = processManagerService.sendEvent(id, "initialize", null, "admin")

            session.flush()
            TransactionSynchronizationManager.unbindResource(sessionFactory);
            SessionFactoryUtils.closeSession(session);
        }
        synchronized (mutexInitStarted){
            mutexInitStarted.wait(1000);
        }
        assert threadRuntimeInfoService.isExecuting(id) == Boolean.TRUE
        assert processManagerService.killProcess(id, "admin") == Boolean.TRUE
        int counter = 0
        while(threadRuntimeInfoService.isProcessKilledByExecutionThread(id)){
            Thread.sleep(1000)
            counter++
            assert counter < 10
        }
        assert threadRuntimeInfoService.isExecuting(id) == Boolean.FALSE
    }
}