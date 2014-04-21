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
 
package com.jcatalog.grailsflow.client

import com.jcatalog.grailsflow.client.ClientExecutor 
import org.apache.commons.httpclient.methods.RequestEntity
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpException
import org.apache.commons.httpclient.methods.PostMethod

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Simple implementation for client executor.
 * It makes authorization through passing 'manager' login and password.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class GrailsflowHTTPClientExecutor implements ClientExecutor {
    private static final Log log = LogFactory.getLog(getClass())
    
    def executeCall(String siteBase, String url, RequestEntity requestEntity) {
        log.debug("Starting executing callback functions.")
        
        HttpClient httpClient = new HttpClient()

        // authentication part
        // we need to implement special SecurityHTTPClient that will provide
        // possibility to access remote URL.
        def loginUrl = "${siteBase}/login/login?loginName=manager&password=manager"

        PostMethod method = new PostMethod(loginUrl)
        try {
            def statusCode = httpClient.executeMethod(method)
        } catch (Throwable t) {
            log.error(" Error during method execution", t)
        }

        def stream = method.getResponseBodyAsStream()
        if (stream) stream.close()
        method.releaseConnection()

        log.debug("Authorization request is finished.")
        
        // callback URL part
        method = new PostMethod(url)
        method.setRequestEntity(requestEntity)

        try {
            def statusCode = httpClient.executeMethod(method)
            log.debug("Status of callback function execution is: $statusCode")
        } catch (Throwable t) {
            log.error("Error during method execution", t)
        }

        stream = method.getResponseBodyAsStream()
        if (stream) stream.close()
        method.releaseConnection()

        return null
    }
}