import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import com.jcatalog.grailsflow.engine.concurrent.ProcessNotifier
import java.util.concurrent.locks.Lock
import sun.rmi.runtime.NewThreadAction

/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


/**
 *
 * Thread runtime info service is responsible for providing runtime
 * about currently running processes; it also permits to execute 
 * arbitrary code (closures) on some process in the context of
 * current thread (hanldes synchronization issues)
 *
 * @author Ivan Baidakou
 */
class ThreadRuntimeInfoService{

    boolean transactional = false // manage transactions manually

    ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    //key - processId, value - ProcessNotifier, guarded by rwLock
    Map processData = new HashMap();

    //consists of keys of processIds. guarded by rwLock
    Set recentlyFinishedProcesses = new HashSet()

    /**
     * Executes provided closure and records processKey as running process.
     * After executing the closuer the processKey is recorded in #recentlyFinishedProcesses
     * which should be cleaned via #clear method. 
     *
     * The closure execution is synchronized via ProcessNotifier for particular porcess
     * i.e. only one closure is executed on the process. 
     *
     * @param processKey
     * @param closure
     * @return the result of closure execution
     */
    public invokeInCurrentThread(Long processKey, Closure closure){
        Object r
        ProcessNotifier notifier
        try{
            rwLock.writeLock().lock() //lock execution info
            notifier = processData.get(processKey)
            if(notifier == null){
              notifier = new ProcessNotifier()
              processData.put(processKey,notifier)
            }
            notifier.invocationThreadLock.writeLock().lock()
            notifier.registerInteres()
            notifier.invocationThreadLock.writeLock().unlock()
            rwLock.writeLock().unlock() //unlock execution info
            
            try{
              notifier.lock.writeLock().lock() //lock process
              notifier.invocationThreadLock.writeLock().lock()
              notifier.invocationThread = Thread.currentThread()
              notifier.invocationThreadLock.writeLock().unlock()
              r = closure(notifier)
            }finally{
              notifier.invocationThreadLock.writeLock().lock()
              notifier.invocationThread = null
              notifier.unregisterInteres()
              notifier.invocationThreadLock.writeLock().unlock()
              notifier.lock.writeLock().unlock() //unlock process
            }

            // If current thread was killed it must generate exception
            // to avoid https://jira.terracotta.org/jira/browse/QTZ-471
            // interrupted() is used instead of isInterrupted() because interrupted status must be cleared
            if (Thread.currentThread().interrupted()) {
                throw new InterruptedException("Invocation of a closure in current thread was interrupted. The result of execution of the closure is ${r}")
            }
            
        }finally{
            //remove execution info only if there is no more threads are interested 
            //for doing some action with the process
            rwLock.writeLock().lock()
            notifier.invocationThreadLock.readLock().lock()
            if(!notifier.hasInteres()){
              processData.remove(processKey)
              recentlyFinishedProcesses.add(processKey)
            }
            notifier.invocationThreadLock.readLock().unlock()
            rwLock.writeLock().unlock()
        }
        return r
    }

    /**
     *  Sets the interrupted flag on processNotifier and interrupts the thread
     *  also registers interest in notifier, that means, that the killing thread
     *  will unregister it. The registered interes has the following meaning:  
     *  before the killing thread updates all nodes (simultaniously) it will used to
     *  keep interrupted flag for all threads in queue for update current process 
     * 
     * @param processKey
     * @return return true on success, and false if there is no such a process running
     */
    public boolean signalInterrupt(Long processKey){
        boolean r = false
        rwLock.readLock().lock()
        ProcessNotifier notifier = processData.get(processKey)
        if(notifier){
            notifier.invocationThreadLock.writeLock().lock();
            notifier.interrupted = true
            notifier.invocationThread?.interrupt()
            if (notifier.invocationThread?.isInterrupted()) {
                r = true;
            }

            notifier.registerInteres()
            notifier.invocationThreadLock.writeLock().unlock();
        }
        rwLock.readLock().unlock()
        return r;
    }

    /**
     *  Interrupts the action thread
     *
     * @param processKey
     * @return return true on success, and false if there is no such a process running
     */
    public boolean signalActionInterrupt(Long processKey){
        boolean r = false
        ProcessNotifier notifier = processData.get(processKey)

        if(notifier){
            if (notifier.executionThread) {
                notifier.executionThread.interrupt()
                r = true
            }

        }

        return r;
    }

    /**
     * Check weather the specified process is being executed
     * @param processKey
     * @return true, if current process is being executed
     */
    public boolean isExecuting(Long processKey){
        boolean r = false
        rwLock.readLock().lock()
        r = processData.keySet().contains(processKey)
        rwLock.readLock().unlock()
        return r;
    }

    /**
     * Check weather the action from node of specified process is being executed and the point of execution is before
     * the check whether notifier.interrupted == true
     * @param processKey
     * @return true, if current process node action is being executed and the point of execution is before
     * the check whether notifier.interrupted == true
     */
    public boolean isProcessKilledByExecutionThread(Long processKey){
        boolean r = false
        rwLock.readLock().lock()
        ProcessNotifier notifier = processData.get(processKey)
        if (notifier) {
            notifier.invocationThreadLock.readLock().lock()
            r = notifier.executionThread?.isAlive() && notifier.killedByExecutionThread
            notifier.invocationThreadLock.readLock().unlock()
        }
        rwLock.readLock().unlock()
        return r;
    }


    /**
     * Check weather the specified process is being executed or it is in the list of recently finished processes
     * @param processKey
     * @return true, if current process is being executed or just has finished
     */
    public boolean isExecutingOrRecentlyFinished(Long processKey){
        boolean r = false
        rwLock.readLock().lock()
        r = processData.keySet().contains(processKey) || recentlyFinishedProcesses.contains(processKey)
        rwLock.readLock().unlock()
        return r;
    }

    /**
     * removes information about recently finished processes
     */
    public void clear(){
        rwLock.writeLock().lock()
        recentlyFinishedProcesses.clear()
        rwLock.writeLock().unlock()
    }

}
