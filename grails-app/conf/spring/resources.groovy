import grails.util.GrailsUtil

beans = {
  switch(GrailsUtil.environment) {

      case "test":
        break
  }

  workareaPathProvider(com.jcatalog.grailsflow.workarea.GrailsflowPathProvider) {
    resourcesPath = "../src/samples"
    resourcesUrl = "/workarea"
  }

  

}