pipeline {
    agent any
    stages {
        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = sh(script: 'git diff --name-only origin/main', returnStdout: true).trim().split("\n")
                    echo "Changed file: ${changedFile}"
                    def services = ['spring-petclinic-customers-service', 'spring-petclinic-genai-service', 'spring-petclinic-vets-service', 'spring-petclinic-visits-service']
                    def detectedServices = services.findAll { service -> 
                        changedFiles.any { it.startsWith(service) }
                    }

                    if (detectedServices) {
                        env.CHANGED_SERVICES = detectedServices.join(',')
                        echo "Detected changes in: ${env.CHANGED_SERVICES}"
                    } else {
                        echo "No relevant changes detected. Skipping pipeline."
                        currentBuild.result = 'ABORTED'
                        error("No service changed. Exiting pipeline.")
                    }
                }
            }
        }

        stage('Test & Coverage') {
            when {
                expression { return env.CHANGED_SERVICES != null && env.CHANGED_SERVICES != '' }
            }
            steps {
                script {
                    def servicesToTest = env.CHANGED_SERVICES.split(',')
                    servicesToTest.each { service ->
                        dir(service) {
                            sh 'chmod +x ./gradlew'  // Ensure Gradle wrapper is executable
                            sh './gradlew test jacocoTestReport'

                            // Upload test results
                            junit 'build/test-results/test/*.xml'

                            // Upload test coverage report
                            publishCoverage adapters: [
                                jacocoAdapter('build/reports/jacoco/test/jacocoTestReport.xml')
                            ]
                        }
                    }
                }
            }
        }

        stage('Build') {
            when {
                expression { return env.CHANGED_SERVICES != null && env.CHANGED_SERVICES != '' }
            }
            steps {
                script {
                    def servicesToBuild = env.CHANGED_SERVICES.split(',')
                    servicesToBuild.each { service ->
                        dir(service) {
                            sh 'chmod +x ./gradlew'  // Ensure Gradle wrapper is executable
                            sh './gradlew build'
                        }
                    }
                }
            }
        }
    }
}
