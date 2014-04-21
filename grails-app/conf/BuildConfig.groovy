grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)

grails.project.work.dir = "target"
grails.views.javascript.library="jquery"

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    inherits "global"
    log "warn"
    repositories {
        inherits true
        grailsPlugins()
        grailsHome()

        if (grailsSettings.config.jcatalog.mavenLocal) {
            mavenLocal grailsSettings.config.jcatalog.mavenLocal
        }
        if (grailsSettings.config.jcatalog.mavenCentral) {
            mavenRepo grailsSettings.config.jcatalog.mavenCentral
        }

        grailsCentral()
        mavenCentral()
        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
        mavenRepo "http://repository.codehaus.org"
        mavenRepo "http://download.java.net/maven/2/"
        mavenRepo "http://repository.jboss.com/maven2/"
    }
    
    dependencies {
        compile ('com.sdicons.jsontools:jsontools-core:1.7',
                 'commons-httpclient:commons-httpclient:3.0.1') {
            exported=false
        }
        runtime ('javax.mail:mail:1.4',
                 'javax.activation:activation:1.1',
                 'org.apache.ant:ant:1.7.1',
                 'org.apache.ant:ant-launcher:1.7.1',
                 'org.springframework:spring-test:3.1.2.RELEASE'){
            exported=false
        }
    }
    
    plugins {
        // plugins for the build system only
        build ":tomcat:7.0.47"

        // plugins needed at runtime but not for compilation
        runtime ":hibernate:3.6.10.6" // or ":hibernate4:4.1.11.6"
        runtime("org.grails.plugins:resources:1.1.6")
        runtime('org.grails.plugins:quartz:0.4.2',
                'org.grails.plugins:jquery:1.7.1',
                'org.grails.plugins:jquery-ui:1.8.15',
                'org.grails.plugins:jqplot:0.1')
        runtime('org.grails.plugins:export:0.9') {
            transitive = false
        }
    }
}
