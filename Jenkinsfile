// it looks like sometimes changes from library become available in currentBuild.changeSets instead if original repo
library identifier: 'utils@master',
        retriever: modernSCM(
                [
                        $class: 'GitSCMSource',
                        remote: 'https://github.com/OpusCapita/minsk-core-jenkins-util-library.git'
                ]
        )

pipeline {
    agent any

    tools {
        jdk 'jdk8'
    }

    environment {
        def grailsHome = tool name: 'grails-2.4.4'
        PATH = "${grailsHome}/bin:${env.PATH}"
    }

    stages {
        stage('Initialize') {
            steps {
                echo "PATH = ${PATH}"
            }
        }

        stage('Refresh dependencies (grails)') {
            steps {
                echo "[STAGE] 'Refresh dependencies (grails)'"
                sh "grails refresh-dependencies --non-interactive"
            }
        }

        stage('Running tests (grails)') {
            steps {
                echo "[STAGE] 'Running tests'"
                sh "grails test-app --stacktrace --offline"
            }
            post {
                always {
                    publishHTML(
                            [
                                    allowMissing         : true,
                                    alwaysLinkToLastBuild: false,
                                    keepAll              : true,
                                    reportDir            : 'target/test-reports/html',
                                    reportFiles          : 'index.html',
                                    reportName           : 'Unit Tests Report',
                                    reportTitles         : 'Unit Tests Report'
                            ]
                    )
                    publishHTML(
                            [
                                    allowMissing         : true,
                                    alwaysLinkToLastBuild: false,
                                    keepAll              : true,
                                    reportDir            : 'target/test-reports/cobertura',
                                    reportFiles          : 'index.html',
                                    reportName           : 'Test Code Coverage',
                                    reportTitles         : 'Test Code Coverage'
                            ]
                    )
                }
            }
        }

        stage('Deploy to repository') {
            steps {
                echo "[STAGE] 'Deploy to repository'"
                sh "grails maven-deploy -verbose -Dgrails.env=prod --offline"
            }
        }

        stage('Build and deploy docs') {
            steps {
                echo "[STAGE] 'Build and deploy docs'"
                sh "rm -rf plugin.xml"
                sh "grails doc --offline"
                sh "grails DocDeploy --offline"
            }
        }
    }

    post {
        always {
            script {
                try {
                    echo "Notification to `Slack`"
                    slack.sendNotification()
                    echo "Notification to `JIRA`"
                    jira.addComments()
                } catch (e) {
                    echo "ERROR: ${e.message}"
                    throw e
                }
            }
        }
    }
}