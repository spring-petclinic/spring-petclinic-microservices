pipeline {
    agent any
    
    tools {
        maven 'Maven 3' // Use Jenkins' built-in Maven
    }
    
    stages {
        stage('Clean Workspace') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def services = [
                            "spring-petclinic-customers-service",
                            "spring-petclinic-vets-service",
                            "spring-petclinic-visits-service",
                            "spring-petclinic-genai-service"]
                    def changedFiles = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim().split("\n")
                    def changedServices = []
                    for (service in services) {
                        if (changedFiles.any { it.startsWith(service) }) {
                            changedServices.add(service)
                        }
                    }
                    echo 'Code changes in services: ${changedServices.join(', ')}'
                    env.CHANGED_SERVICES = changedServices.join(', ')                             
                }
            }
        }

        stage('Test Services') {
            when {
                expression { env.CHANGED_SERVICES != '' }
            }
            step {
                script {
                    def changedServices = env.CHANGED_SERVICES.split(',')
                    changedServices.each{service -> {
                            echo 'Testing service: ${service}'
                        }
                    }
                }
            }
        }

        stage('Build Services') {
            when {
                expression { env.CHANGED_SERVICES != '' }
            }
            step {
                script {
                    def changedServices = env.CHANGED_SERVICES.split(',')
                    changedServices.each{service -> {
                            echo 'Testing service: ${service}'
                        }
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo "Pipeline execution completed!"
        }
    }
}
