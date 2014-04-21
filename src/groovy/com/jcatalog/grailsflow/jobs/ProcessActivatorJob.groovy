package com.jcatalog.grailsflow.jobs

import org.springframework.scheduling.quartz.QuartzJobBean
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * ProcessActivatorJob class represents job that starts process
 * with the given initial parameters.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class ProcessActivatorJob extends QuartzJobBean {
    private static final Log log = LogFactory.getLog(getClass())

    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        def dataMap = ctx.jobDataMap

        if (!dataMap.processManagerService) {
            log.warn("ProcessManagerService is not configured.")
            return
        } else {

            if (!dataMap.processManagerService.checkProcessIdentifier(dataMap.processType, dataMap.variables)) {
                log.error("${dataMap.processType} workflow cannot be started: 412 Precondition Failed.")
                return 
            }
            
            def result = dataMap.processManagerService.startProcess(dataMap.processType, dataMap.requester, dataMap.variables) 
            return
        }
    }
}