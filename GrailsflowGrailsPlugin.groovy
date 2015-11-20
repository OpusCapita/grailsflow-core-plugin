
import com.jcatalog.grailsflow.model.process.FlowStatus

import com.jcatalog.grailsflow.grails.ListFactoryBean

import com.jcatalog.grailsflow.search.DefaultSearchParameter
import com.jcatalog.grailsflow.search.DateSearchParameter
import com.jcatalog.grailsflow.search.DefaultDisplayParameter

import com.jcatalog.grailsflow.status.NodeStatusEnum
import com.jcatalog.grailsflow.status.ProcessStatusEnum
import com.jcatalog.grailsflow.scheduling.triggers.ConfigurableSimpleTrigger

class GrailsflowGrailsPlugin {
    def version = '1.7.4-SNAPSHOT'
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.3 > *"
    def dependsOn = [quartz: "1.0.1 > *"]
    def pluginExcludes = [
            "src/docs/**"
    ]

    def author = "jCatalog AG"
    def authorEmail = "july.antonicheva@jcatalog.com"
    def title = "Grailsflow"
    def description = "GrailsFlow is an open source Workflow and Process Engine."

    // URL to the plugin's documentation
    def documentation = "http://jcatalog.github.io/grailsflow-core-plugin/"
    def license = "APACHE"
    def organization = [ name: "jCatalog AG", url: "http://grailsflow.org/" ]
    def developers = [
            [ name: "July Antonicheva", email: "july.antonicheva@jcatalog.com" ]
    ]
    def issueManagement = [ system: "JIRA", url: "http://jira.jcatalog.com/browse/GFW" ]
    def scm = [ url: "https://github.com/jCatalog/grailsflow-core-plugin" ]

    def doWithSpring = {
      ConfigurableSimpleTrigger.metaClass.'static'.getGrailsApplication = { -> application }

      def clusterName = (application.config.grailsflow.clusterName instanceof Closure) ?
          application.config.grailsflow.clusterName() : application.config.grailsflow.clusterName
      if (!clusterName) {
          application.config.grailsflow.clusterName = "gfw_${new Date().time}_${new Random().nextInt(1000000)}"
      }

      // format patterns
      datePatterns(java.util.HashMap, ['en':'MM/dd/yyyy', 'de':'dd.MM.yyyy'])
      dateTimePatterns(java.util.HashMap, ['en':'MM/dd/yy HH:mm', 'de':'dd.MM.yy HH:mm'])
      numberPatterns(java.util.HashMap, ['en':'0.00', 'de':'0.00'])
      decimalSeparators(java.util.HashMap, ['en':'.', 'de':','])
      defaultLocale(java.lang.String, "en")

      // default workarea configuration
      workareaPathProvider(com.jcatalog.grailsflow.workarea.GrailsflowPathProvider) {
        resourcesPath = "workarea"
        resourcesUrl = "/workarea"
      }

      // default scripts configuration
      scriptsProvider(com.jcatalog.grailsflow.workarea.GrailsflowWorkareaScriptsProvider) {
        workareaPathProvider = ref('workareaPathProvider')
      }
      appExternalID(java.lang.String, "grailsflow")
      processesPath(java.lang.String, "processes")
      actionsPath(java.lang.String, "actions")
      documentsPath(java.lang.String, "documents")
      callbacksPath(java.lang.String, "callbacks")

      // default UI configuration
      maxResultSize(java.lang.Integer, "20")

      // default configuration for threads quantity (threads that can be running concurrently)
      maxThreadsQuantity(java.lang.Integer, "7")

      // default configuration for quantity of restricted processes that used in SQL
      // 'in' clause
      maxRestrictedProcesses(java.lang.Integer, "2000")

      // default configuration for quantity of loaded processes in removing service
      maxLoadedProcesses(java.lang.Integer, "200")

      // default security configuration
      securityHelper(com.jcatalog.grailsflow.security.GrailsflowSecurityHelper)
      usersProvider(com.jcatalog.grailsflow.security.GrailsflowUsersProvider)
      rolesProvider(com.jcatalog.grailsflow.security.GrailsflowRolesProvider)
      groupsProvider(com.jcatalog.grailsflow.security.GrailsflowGroupsProvider)

      // default asynchronous configuration
      clientExecutor(com.jcatalog.grailsflow.client.GrailsflowHTTPClientExecutor)

      // Grailsflow engine configuration
      actionFactory(com.jcatalog.grailsflow.actions.GrailsflowActionFactory) {
           actionsPath = ref('actionsPath')
           scriptsProvider = ref('scriptsProvider')
      }

      worklistProvider(com.jcatalog.grailsflow.worklist.WorklistProvider) {
        appExternalID = ref('appExternalID')
      }
      processProvider(com.jcatalog.grailsflow.process.ProcessProvider) {
        appExternalID = ref('appExternalID')
      }

      processScriptProvider(com.jcatalog.grailsflow.process.script.GrailsflowProcessScriptProvider) {
        scriptsProvider = ref('scriptsProvider')
        processesPath = ref('processesPath')
      }

      cacheManager(com.jcatalog.grailsflow.cache.SimpleCacheManager)

      processFactory(com.jcatalog.grailsflow.engine.GrailsflowCachingProcessFactory) {
        processScriptProvider = ref('processScriptProvider')
        cacheManager = ref('cacheManager')
      }

      nodeExecutor(com.jcatalog.grailsflow.engine.execution.NodeExecutor) {
        actionFactory = ref('actionFactory')
      }

      // validation
      processDefValidator(com.jcatalog.grailsflow.validation.DefaultProcessDefValidator)
      processClassValidator(com.jcatalog.grailsflow.validation.DefaultProcessClassValidator)

      bean(org.springframework.web.multipart.commons.CommonsMultipartResolver) {
          maxUploadSize(1000000)
      }

      // Allows worklist filtering
      // NOTE: if set to TRUE - it can cause problems with performance
      // in case of large amount of items.
      isWorklistFilterAvailable(java.lang.Boolean, Boolean.TRUE)

      // processListColumns UI
      processTypeSearchProperty(DefaultSearchParameter) {
        name = "type"
        searchTemplate = "/processList/search/type"
      }
      processStatusSearchProperty(DefaultSearchParameter) {
        name = "status"
        searchTemplate = "/processList/search/status"
      }
      startedFromSearchProperty(DateSearchParameter) {
        name = "startedFrom"
        searchTemplate = "/processList/search/date"
      }
      userSearchProperty(DefaultSearchParameter) {
        name = "startUser"
      }
      finishedFromSearchProperty(DateSearchParameter) {
        name = "finishedFrom"
        searchTemplate = "/processList/search/date"
      }
      processListSearchParameters(ListFactoryBean) {
        items = [ ref('processTypeSearchProperty'),
                ref('processStatusSearchProperty'),
                ref('startedFromSearchProperty'), ref('userSearchProperty'),
                ref('finishedFromSearchProperty')
                ]
      }


      processTypeDisplayProperty(DefaultDisplayParameter) {
        name = "type"
        displayTemplate = "/processList/display/type"
      }
      processStatusDisplayProperty(DefaultDisplayParameter) {
        name = "status.statusID"
        displayProperty = "status"
        displayTemplate = "/processList/display/status"
      }
      createdOnDisplayProperty(DefaultDisplayParameter) {
        name = "createdOn"
        displayProperty = "createdOn"
        displayTemplate = "/processList/display/dateTime"
      }
      createdByDisplayProperty(DefaultDisplayParameter) {
        name = "createdBy"
        displayProperty = "createdBy"
      }
      activeNodesDisplayProperty(DefaultDisplayParameter) {
        name = "activeNodes"
        sortable = false
        displayTemplate = "/processList/display/activeNodes"
      }
      finishedOnDisplayProperty(DefaultDisplayParameter) {
        name = "finishedOn"
        displayProperty = "finishedOn"
        displayTemplate = "/processList/display/dateTime"
      }
      processListDisplayParameters(ListFactoryBean) {
        items = [ ref('processTypeDisplayProperty'),
                ref('processStatusDisplayProperty'),
                ref('createdOnDisplayProperty'), ref('createdByDisplayProperty'),
                ref("activeNodesDisplayProperty"),
                ref('finishedOnDisplayProperty')
                ]
      }

      nodeWorklistColumn(DefaultDisplayParameter) {
        name = "nodeID"
        displayTemplate = "/worklist/display/node"
      }
      externalURLWorklistColumn(DefaultDisplayParameter) {
        name = "externalUrl"
        sortable = false
        displayTemplate = "/worklist/display/externalUrl"
      }
      processTypeWorklistColumn(DefaultDisplayParameter) {
        name = "processType"
        displayProperty = "process"
        sortable = false
        displayTemplate = "/worklist/display/processType"
      }
      callerWorklistColumn(DefaultDisplayParameter) {
        name = "caller"
        displayProperty = "caller"
      }
      startedOnWorklistColumn(DefaultDisplayParameter) {
        name = "startedOn"
        displayProperty = "startedOn"
        displayTemplate = "/worklist/display/dateTime"
      }
      dueOnWorklistColumn(DefaultDisplayParameter) {
        name = "dueOn"
        displayProperty = "dueOn"
        displayTemplate = "/worklist/display/dateTime"
      }
      worklistDisplayParameters(ListFactoryBean) {
        items = [ ref('nodeWorklistColumn'),
                ref('externalURLWorklistColumn'),
                ref('processTypeWorklistColumn'),
                ref('callerWorklistColumn'),
                ref('startedOnWorklistColumn'),
                ref('dueOnWorklistColumn')
                ]
      }

      // Extensions
      eventEmailProcessor(com.jcatalog.grailsflow.extension.email.GrailsflowEventEmailProcessor) {
        processManagerService = ref('processManagerService')
      }

    }

    def doWithApplicationContext = { applicationContext ->

        def sessionFactory = applicationContext.sessionFactory

        FlowStatus.withTransaction {
            // Insert process statuses into DB if they're missing
            ProcessStatusEnum.values()?.each() {
                def statusID = it.value()
                if (!FlowStatus.findByStatusID(statusID)) {
                    def status = new FlowStatus(statusID: statusID, description: statusID, isFinal: it.isFinal())
                    status.save()
                }
            }

            // Insert node statuses into DB if they're missing
            NodeStatusEnum.values()?.each() {
                def statusID = it.value()
                if (!FlowStatus.findByStatusID(statusID)) {
                    def status = new FlowStatus(statusID: statusID, description: statusID, isFinal: it.isFinal())
                    status.save()
                }
            }
        }

    }

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when this class plugin class is changed
        // the event contains: event.application and event.applicationContext objects
    }

    def onApplicationChange = { event ->
        // TODO Implement code that is executed when any class in a GrailsApplication changes
        // the event contain: event.source, event.application and event.applicationContext objects
    }
}

