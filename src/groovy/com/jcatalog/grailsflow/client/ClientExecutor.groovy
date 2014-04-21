package com.jcatalog.grailsflow.client

import org.apache.commons.httpclient.methods.RequestEntity

/**
 * ClientExecutor class represents common interface for executing
 * callback functions.
 * The application should have its own realisation implemented,
 * according to application security type, e.g. enhance method
 * post with special header information(login/password).
 *
 * - param siteBase - site base information for forming URL request path;
 * - param url - URL for sending request;
 * - param requestEntity - request information that should be sent
 * to callback function.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
interface ClientExecutor {
    def executeCall(String siteBase, String url, RequestEntity requestEntity)
}