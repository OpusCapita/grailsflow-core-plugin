package com.jcatalog.grailsflow

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import groovy.transform.Synchronized
import org.springframework.orm.hibernate3.SessionFactoryUtils
import org.hibernate.FlushMode
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.orm.hibernate3.SessionHolder
import com.jcatalog.grailsflow.model.process.BasicProcess
import com.jcatalog.grailsflow.model.graphics.ProcessNodePosition
import com.jcatalog.grailsflow.model.process.FlowStatus
import com.jcatalog.grailsflow.status.ProcessStatusEnum
import com.jcatalog.grailsflow.process.ProcessSearchParameters
import org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin
import java.util.concurrent.Executors
import java.util.concurrent.Callable

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
 *  ProcessAdministrationService class is used to organize admin operations with processes,
 *  e.g. 'delete' operation
 *
 * @author July Antonicheva
 */
class ProcessAdministrationService {
    boolean transactional = false // manage transactions manually

    def grailsApplication
    def sessionFactory
    def processWorklistService
    def processManagerService

    def maxLoadedProcesses

    @Transactional(propagation = Propagation.REQUIRED)
    public Long deleteFilteredProcesses(ProcessSearchParameters searchParameters) {
        return Executors.newSingleThreadExecutor().submit( {
            Long processesQuantity = 0
            searchParameters.maxResult = maxLoadedProcesses
            long removingTime = new Date().time
            log.info("Starting removing processes")

            while (true) {
                def session = SessionFactoryUtils.getNewSession(sessionFactory);
                session.setFlushMode(FlushMode.AUTO);
                TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));

                try {
                    List<BasicProcess> processes = processWorklistService.getProcessList(searchParameters)
                    if (!processes) break;

                    processesQuantity += processes.size()
                    processes.each() { BasicProcess process ->
                        // remove positions for graphics
                        ProcessNodePosition.findAllByProcess(process)*.delete()

                        // remove log files about process executions
                        try {
                            File logFile = processManagerService.getProcessLogFile(process)
                            if (logFile && logFile.exists()) {
                                logFile.delete()
                            } else {
                                log.info("There is no existed log file for process ${process.type}[id: ${process.id}]")
                            }
                        } catch(Exception ex) {
                            log.error("Cannot delete log file for process ${process.type}[id: ${process.id}]", ex)
                        }
                    }

                    processes*.delete()
                } catch (Throwable ex) {
                    log.error("Unexpected exception occurred in synchronized block during process removing! ", ex)
                } finally {
                    session.flush()
                    session.clear()
                    TransactionSynchronizationManager.unbindResource(sessionFactory);
                    SessionFactoryUtils.closeSession(session);
                }
            }

            long executionTime = new Date().time - removingTime
            log.info("Removing processes finished: execution time [${executionTime}] ms")
            return processesQuantity
        } as Callable).get()

    }
}



