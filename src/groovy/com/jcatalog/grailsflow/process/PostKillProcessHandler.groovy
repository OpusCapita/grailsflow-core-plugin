package com.jcatalog.grailsflow.process

import com.jcatalog.grailsflow.model.process.BasicProcess

/**
 * Classes that implement PostKillProcessHandler interface may provide application specific code
 * that will be executed after process killing.
 *
 * @author Sergei Shushkevich
 */
public interface PostKillProcessHandler {

    void handle(BasicProcess process, String user)
}