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

                    sh 'git fetch origin main'
                    
                    def changedFiles = sh(script: 'git diff --name-only origin/main...HEAD', returnStdout: true).trim().split("\n")
                    def changedServices = []
                    for (service in services) {
                        if (changedFiles.any { it.startsWith(service) }) {
                            changedServices.add(service)
                        }
                    }
                    echo "Code changes in services: ${changedServices.join(', ')}"
                    env.CHANGED_SERVICES = changedServices.join(', ')                             
                }
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def services = [
                        "spring-petclinic-customers-service",
                        "spring-petclinic-vets-service",
                        "spring-petclinic-visits-service",
                        "spring-petclinic-genai-service"
                    ]
        
                    // Fetch latest changes from origin/main
                    sh 'git fetch origin main'
        
                    // Identify the correct base commit for diff
                    def baseCommit = sh(script: 'git merge-base origin/main HEAD', returnStdout: true).trim()
                    
                    // Get changed files from the merge base commit
                    def changedFiles = sh(script: "git diff --name-only ${baseCommit} HEAD", returnStdout: true).trim().split("\n")
        
                    def changedServices = []
                    for (service in services) {
                        if (changedFiles.any { it.startsWith(service) }) {
                            changedServices.add(service)
                        }
                    }
        
                    echo "Code changes detected in services: ${changedServices.join(', ')}"
                    env.CHANGED_SERVICES = changedServices.join(', ')
                }
            }
        }


        stage('Build Services') {
            when {
                expression { env.CHANGED_SERVICES != '' }
            }
            steps {
                script {
                    def changedServices = env.CHANGED_SERVICES.split(',')
                    changedServices.each{ service -> 
                        echo "Building service: ${service}"
                        dir("${service}") {
                            sh "mvn clean package -DskipTests"
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
