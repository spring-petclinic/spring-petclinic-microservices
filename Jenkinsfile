pipeline {
    agent any
    environment {
        CHANGED_SERVICE = ''
    }
    stages {
        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim().split("\n")
                    def services = ['spring-petclinic-customers-service', 'spring-petclinic-genain-service', 'spring-petclinic-vets-service', 'spring-petclinic-visits-service']
                    echo "File changed: $changedFiles"
                    def detectedService = services.find { service -> 
                        changedFiles.any { it.startsWith(service) }
                    }
                    if (detectedService) {
                        CHANGED_SERVICE = detectedService
                        echo "Detected changes in $CHANGED_SERVICE"
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
                expression { return CHANGED_SERVICE != '' }
            }
            steps {
                dir("${CHANGED_SERVICE}") {
                    sh './gradlew test'
                    junit 'build/test-results/test/*.xml'
                    sh './gradlew jacocoTestReport'
                }
            }
        }

        stage('Build') {
            when {
                expression { return CHANGED_SERVICE != '' }
            }
            steps {
                dir("${CHANGED_SERVICE}") {
                    sh './gradlew build'
                }
            }
        }
    }
}
