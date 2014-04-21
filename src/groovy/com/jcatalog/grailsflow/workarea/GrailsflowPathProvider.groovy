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

package com.jcatalog.grailsflow.workarea

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.core.io.Resource

/**
 * Grailsflow impementation for WorkareaPathProvider
 *
 * @author Stephan Albers
 * @author July Karpey
 */
class GrailsflowPathProvider implements WorkareaPathProvider,
                                        ApplicationContextAware {
    String resourcesPath
    String resourcesUrl
    ApplicationContext appContext

    File getResourceFile(String relativeResource) {
        if (!relativeResource || relativeResource == '.'
            || relativeResource.trim() == '') {
             return appContext.getResource(resourcesPath).getFile()
        }
        try {
            Resource resource = appContext
                .getResource(checkUrl(resourcesPath)+checkUrl(relativeResource))
            return resource?.getFile()
        } catch (FileNotFoundException e) {
            log.debug("There is no resource for path ${checkUrl(resourcesPath)+checkUrl(relativeResource)}. $e")
            return null
        }

    }

    String getResourcePath(String relativeResource) {
        getResourceFile(relativeResource).getAbsolutePath()
    }

    String getResourceUrl(String relativeResource) {
        if (!relativeResource || relativeResource == '.'
            || relativeResource.trim() == '') {
            return resourcesPath
        }

        RequestContextHolder.currentRequestAttributes()
            .getContextPath()+checkUrl(resourcesUrl) + checkUrl(relativeResource)
    }

    private String checkUrl(String path) {
         if (!path.startsWith("/")) {
             path = "/" + path
         }
         path
    }

    def void setApplicationContext(ApplicationContext ctx) throws BeansException {
        appContext = ctx
    }
}