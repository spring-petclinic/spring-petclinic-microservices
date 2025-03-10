pipeline {
    agent any
    stages {
        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim().split("\n")
                    def services = ['spring-petclinic-customers-service', 'spring-petclinic-genain-service', 'spring-petclinic-vets-service', 'spring-petclinic-visits-service']
                    def changedService = services.find { service -> 
                        changedFiles.any { it.startsWith(service) }
                    }
                    
                    if (changedService) {
                        echo "Detected changes in ${changedService}"
                        env.CHANGED_SERVICE = changedService
                    } else {
                        echo "No relevant changes detected. Skipping pipeline."
                        currentBuild.result = 'ABORTED'
                        error("No service changed. Exiting pipeline.")
                    }
                }
            }
        }

        stage('Test') {
            when {
                expression { return env.CHANGED_SERVICE != null && env.CHANGED_SERVICE != '' }
            }
            steps {
                dir("${env.CHANGED_SERVICE}") {
                    sh './gradlew test jacocoTestReport' // Run tests and generate coverage report

                    // Upload test case results
                    junit 'build/test-results/test/*.xml'

                    // Upload test coverage report using Coverage plugin
                    script {
                        publishCoverage adapters: [
                            jacocoAdapter('build/reports/jacoco/test/jacocoTestReport.xml')
                        ]
                    }
                }
            }
        }

        stage('Build') {
            when {
                expression { return env.CHANGED_SERVICE != null && env.CHANGED_SERVICE != '' }
            }
            steps {
                dir("${env.CHANGED_SERVICE}") {
                    sh './gradlew build'
                }
            }
        }
    }
}
