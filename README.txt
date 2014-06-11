Grailsflow plugin: Installation Guide and Configuration.
--------------------------------------------------------------

-------------
Installation.
-------------

Grailsflow plugin depends on:

* Grails plugins:
  1. Hibernate plugin (tested with version 1.3.1)
  2. Quartz plugin (tested with version 0.4.1)

Grails plugins should be installed using "grails install-plugin" goal BEFORE grailsflow plugin installation. 
For example:
# grails install-plugin quartz

* Java libraries

1. jsontools-core (tested with version 1.7)
   (for building graphics)

2. commons-httpclient (tested with version 3.0.1)
   (for asynchronous Actions)

3. javamail (tested with version 1.4)
   (for receiving Events by mail)

3. activation (tested with version 1.1)
   (for receiving Events by mail)


Use 'grails install-plugin path/to/grailsflow-plugin.zip' goal to install grailslow-core plugin
in your application.


--------------
Configuration.
--------------

Following beans are already configured in
GrailsflowCoreGrailsPlugin.groovy with default values. You can
change default configuration by specifying new beans values
in grails-app/conf/spring/resources.xml file or in other place
where spring beans for your application are defined.


* Workarea configuration:

Processes, actions and some other grailsflow objects are stored as files
in so-called workarea.  
  
  1. "workareaPathProvider" bean
     Bean of the type that implements com.jcatalog.grailsflow.workarea.PathProvider interface.
     By default "workareaPathProvider" is bean of "com.jcatalog.grailsflow.workarea.GrailsflowPathProvider"
     class that is configured to use local "workarea/" directory as root for workarea.
     In most cases it would be enough to use "com.jcatalog.grailsflow.workarea.GrailsflowPathProvider" 
     class for "workareaPathProvider" bean and simply overwrite it's configuration 
     (workarea root directory and workarea root URL) as shown below:

     workareaPathProvider(com.jcatalog.grailsflow.workarea.GrailsflowPathProvider) { 
        resourcesPath = "myWorkareaFolded"
        resourcesUrl = "/myWorkarea"
      }
  
        2. "processesPath" bean
           String value to define directory relative to workarea where Process definitions are stored.
           Grailsflow plugin will search for processes by <workarea root>/<processesPath>/ path. 
           Default value is "processes", so processes are stored in <workarea root>/processes/ 

        3. "actionsPath" bean
           String value to define directory relative to workarea where Action definitions are stored.
           Grailsflow plugin will search for actions by <workarea root>/<actionsPath>/ path. 
           Default value is "actions", so actions are stored in <workarea root>/actions/ 

        4. "callbacksPath" bean
           String value to define directory relative to workarea where Callback scripts definitions are stored.
           Grailsflow plugin will search for documents by <workarea root>/<callbacksPath>/ path. 
           Default value is "callbacks", so callback scripts are stored in <workarea root>/callbacks/ 

        5. "documentsPath" bean
           String value to define directory relative to workarea where Documents definitions are stored.
           Grailsflow plugin will search for documents by <workarea root>/<documentsPath>/ path. 
           Default value is "documents", so documents are stored in <workarea root>/documents/ 

    6. 'grailsflow.threads.maxQuantity' configuration
       Closure which returns integer value which is configuration for threads quantity (threads that can be running
       concurrently). If it is defined, "maxThreadsQuantity" bean is not taken into account.
       To define it, open Config.groovy in your application and add something like the following:
       grailsflow.threads.maxQuantity = { ->
            def applicationContext = Holders.applicationContext
            return applicationContext.getBean("maxNodesExecutedInParallel").globalValue
        }

    7. "maxThreadsQuantity" bean (It is used only if 'grailsflow.threads.maxQuantity' is not defined into Config.groovy)
       Integer value -> default configuration for threads quantity (threads that can be running concurrently)


* UI configuration

        1. "maxResultSize" bean
            Integer value used for defining size of page for paginated lists (Worklist, Process list, etc)
            Default value is 20

        2. "additionalWorklistColumns" bean
            List of string values that are names of ProcessVariables that should be displayed
            as additional columns on Worklist UI.
            Default value is null

  3.  "datePatterns" bean
      Map of String language -> String Date pattern. Used to format dates displayed on the UI. 
      Default value is ['en':'MM/dd/yyyy', 'de':'dd.MM.yyyy']

  4.  "dateTimePatterns" bean
      Map of String language -> String Date pattern. Used to format time displayed on the UI. 
      Default value is ['en':'MM/dd/yy HH:mm', 'de':'dd.MM.yy HH:mm']

  5.  "numberPatterns"
      Map of String language -> String Number pattern. Used to format numbers displayed on the UI. 
      Default value is ['en':'0.00', 'de':'0.00']
      
  6.  "decimalSeparators"
      Map of String language -> String separator symbol. Used to format numbers displayed on the UI. 
      Default value is ['en':'.', 'de':',']

* Security configuration

Grailsflow plugin supports restriction of Processes and Nodes execution by Users, Roles and Groups.
By default grailsflow plugin uses user with name "grailsflow", role "GRAILSFLOW" and group "Grailsflow" as logged user.
List of available users contains just "grailsflow" username, list of available roles contain just "GRAILSFLOW" role.
To support security that corresponds to your application you have to implement following interfaces for defining
following beans:
  
  1. "securityHelper" bean 
      must implement "com.jcatalog.grailsflow.security.SecurityHelper" interface
      By default bean is of "com.jcatalog.grailsflow.security.GrailsflowSecurityHelper" type        

  2. "usersProvider" bean 
      must implement "com.jcatalog.grailsflow.security.UsersProvider" interface
      By default bean is of "com.jcatalog.grailsflow.security.GrailsflowUsersProvider" type         

  3. "rolesProvider" bean 
      must implement "com.jcatalog.grailsflow.security.RolesProvider" interface
      By default bean is of "com.jcatalog.grailsflow.security.GrailsflowRolesProvider" type

  4. "groupsProvider" bean
      must implement "com.jcatalog.grailsflow.security.GroupsProvider" interface
      By default bean is of "com.jcatalog.grailsflow.security.GrailsflowGroupsProvider" type

* Validation configuration

  1. "processDefValidator" bean that must implement com.jcatalog.grailsflow.validation.Validator interface.
     It validates Process definition when saving it to Process file. Process file is saved regardless of the validation result. 
     Warning and error messages are displayed on the UI.
     By default bean is of the com.jcatalog.grailsflow.validation.DefaultProcessDefValidator class.

  2. "processClassValidator" bean that must implement com.jcatalog.grailsflow.validation.Validator interface.
     It validates Process before starting its execution and prevents startup of invalid process. 
     Warning and error messages are displayed on the UI.
     By default bean is of the com.jcatalog.grailsflow.validation.DefaultProcessClassValidator class.

* Other configuration

        1. "appExternalID" bean
           String stored in DB as BasicProcess.appGroupID. This value was introduced to distinguish
           processes started by different applications in case if they're stored in the same DB.
           Can be specified as empty value!
           Default value is "grailsflow"

  2. files uploading
     To define maximum size for uploading files you should define bean of 
     "org.springframework.web.multipart.commons.CommonsMultipartResolver" type and configure it's 
     "maxUploadSize" property
     Default value for uploading file size is 1000000
     

* Asynchronous actions (will be reimplemented in future versions)

        1. "clientExecutor" bean
                 must implement "com.jcatalog.grailsflow.client.ClientExecutor" interface
                 By default bean is of "com.jcatalog.grailsflow.client.GrilsflowHTTPClientExecutor"

