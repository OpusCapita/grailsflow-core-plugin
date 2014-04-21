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
package com.jcatalog.grailsflow.engine.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * ProcessNotifier is used to thread-safely deliver information (e.g. interruption flag) to currently
 * executing process,
 *
 * @author Ivan Baidakou
 */
class ProcessNotifier{
    boolean interrupted
    boolean killedByExecutionThread
    Thread invocationThread
    Thread executionThread
    
    //indicates the number of threads, which will execute or are executing current process
    int interestedThreads = 0

    //lock for process modification
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    //lock for invocationThread and for interests   
    ReentrantReadWriteLock invocationThreadLock = new ReentrantReadWriteLock();

    synchronized boolean getInterrupted() {
        return interrupted
    }

    synchronized void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted
    }

    synchronized boolean isKilledByExecutionThread() {
        return killedByExecutionThread
    }

    synchronized void setKilledByExecutionThread(boolean killedByExecutionThread) {
        this.killedByExecutionThread = killedByExecutionThread
    }

    synchronized Thread getInvocationThread() {
        return invocationThread
    }
    
    synchronized void registerInteres(){
      interestedThreads++;
    }

    synchronized void unregisterInteres(){
      interestedThreads--;
    }
    
    synchronized boolean hasInteres(){
      return interestedThreads != 0;
    }
}