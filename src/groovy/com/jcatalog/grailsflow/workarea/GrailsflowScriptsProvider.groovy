package com.jcatalog.grailsflow.workarea

import com.jcatalog.grailsflow.workarea.ScriptsProvider

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

import org.springframework.core.io.Resource
import org.springframework.web.context.support.ServletContextResource
import org.springframework.web.util.WebUtils

/**
 * Default implementation for ScriptsProvider
 *
 * @author Stephan Albers
 * @author July Karpey
 * @author Maria Voitovich
 */
class GrailsflowScriptsProvider implements ScriptsProvider, ApplicationContextAware {

    ApplicationContext appContext
    String resourcesPath

    public File getResourceFile(String relativeResource) {

        File resourceFile
        File resourceDir = new File(resourcesPath)

        if (!relativeResource || relativeResource == '.'
                || relativeResource.trim() == '') {
            resourceFile = resourceDir.isAbsolute() ? resourceDir : getFile(appContext.getResource(resourcesPath))
        } else {
            resourceFile = resourceDir.isAbsolute() ? new File(resourceDir, relativeResource) : getFile(appContext.getResource(checkUrl(resourcesPath) + checkUrl(relativeResource)))
        }

        return resourceFile

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

    private File getFile(Resource resource) {
        (resource instanceof ServletContextResource) ?
                new File(WebUtils.getRealPath(resource.servletContext, resource.path)) : resource.file
    }
}
