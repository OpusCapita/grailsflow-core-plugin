//
// This script is executed by Grails after plugin was installed to project.
// This script is a Gant script so you can use all special variables provided
// by Gant (such as 'baseDir' which points on project base dir). You can
// use 'ant' to access a global instance of AntBuilder
//
// For example you can create directory under project tree:
//
//    ant.mkdir(dir:"${basedir}/grails-app/jobs")
//

ant.mkdir(dir: "web-app/workarea/documents")
def configFile = new File("${basedir}/grails-app/conf/Config.groovy")
if (configFile.exists()) {
    println 'Add settings for grails.converters.json to Config!'
    ant.echo( file:"${basedir}/grails-app/conf/Config.groovy",
            message:"\n//grailsFlowPlugin add this setting for grails.converters.JSON to escape circular reference\n",
            append:true )
    ant.echo( file:"${basedir}/grails-app/conf/Config.groovy",
            message:"grails.converters.json.circular.reference.behaviour=\"INSERT_NULL\"",
            append:true )
}