grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)

grails.project.target.level = 1.8
grails.project.source.level = 1.8

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
        mavenRepo "http://repo.grails.org/grails/core"
    }
    
    dependencies {
        compile ("com.sdicons.jsontools:jsontools-core:1.7",
                 "org.apache.httpcomponents.client5:httpclient5:5.2.3",
                 "commons-beanutils:commons-beanutils:1.9.2",
                 "javax.validation:validation-api:1.1.0.Final") {
            exported=false
        }

        runtime ("javax.mail:mail:1.4",
                 "javax.activation:activation:1.1",
                 "org.hibernate:hibernate-validator:5.0.3.Final"){
            exported=false
        }
        test "org.grails:grails-datastore-test-support:1.0.1-grails-2.4"
    }
    
    plugins {
        // plugins for the build system only
        build   ":tomcat:7.0.55"
        build   ":release:3.0.1"

        runtime ":hibernate:3.6.10.18"
        runtime(":resources:1.2.8",
                ":jquery:1.11.1",
                ":jquery-ui:1.10.4")
        runtime ':font-awesome-resources:4.3.0.1'
        compile ":twitter-bootstrap:3.3.1"
        compile ":export:1.6"
        compile ":quartz:1.0.1"
    }
}
