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

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.HttpEntity
import org.apache.hc.core5.http.io.entity.EntityUtils
/**
 * Simple implementation for client executor.
 * It makes authorization through passing 'manager' login and password.
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class GrailsflowHTTPClientExecutor implements ClientExecutor {
    private static final Log log = LogFactory.getLog(getClass())
    
    def executeCall(String siteBase, String url, HttpEntity requestEntity) {
        log.debug("Starting executing callback functions.")

        CloseableHttpClient httpClient = HttpClients.createDefault()

        // authentication part
        // we need to implement special SecurityHTTPClient that will provide
        // possibility to access remote URL.
        def loginUrl = "${siteBase}/login/login?loginName=manager&password=manager"

        HttpPost method = new HttpPost(loginUrl)
        try {
            executeHttpPost(httpClient, method, loginUrl)
        } catch (IOException ex) {
            log.error(" Error during authorization method execution", ex)
            httpClient.close()
        }

        log.debug("Authorization request is finished.")
        
        // callback URL part
        method = new HttpPost(url)
        method.setEntity(requestEntity)

        try {
            executeHttpPost(httpClient, method, url)
        } catch (Throwable t) {
            log.error("Error during callback method execution", t)
        } finally {
            httpClient.close()
        }

        return null
    }

    private void executeHttpPost(CloseableHttpClient httpClient, HttpPost method, String url) {
        CloseableHttpResponse response = httpClient.execute(method)
        HttpEntity entity = response.getEntity()
        String entityAsString = EntityUtils.toString(entity)
        EntityUtils.consume(entity)
        logDebugInformation(url, response, entityAsString)
    }

    private void logDebugInformation(url, CloseableHttpResponse response, String entityAsString) {
        if (log.isDebugEnabled()) {
            log.debug("Response code from ${url}: ${response.getCode()} ${response.getReasonPhrase()}")
            log.debug("Response body: ${entityAsString}")
        }
    }
}