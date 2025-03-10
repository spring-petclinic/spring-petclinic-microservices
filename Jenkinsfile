pipeline {
    agent any
    environment {
        DOCKER_BUILDKIT = '1' // Enable Docker BuildKit for efficient builds (if needed)
    }
    tools {
        maven 'Maven 3' // Use the Maven installation from Jenkins tools
    }
    stages {
        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim().split("\n")
                    env.CHANGED_SERVICES = []
                    
                    def services = ["spring-petclinic-customers-service", "spring-petclinic-vets-service", "spring-petclinic-visits-service", "spring-petclinic-genai-service"]
                    
                    for (service in services) {
                        if (changedFiles.any { it.startsWith(service) }) {
                            env.CHANGED_SERVICES += service
                        }
                    }
                    
                    if (env.CHANGED_SERVICES.isEmpty()) {
                        echo "No microservice has changed, skipping test and build."
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                }
            }
        }

        stage('Test Services') {
            when {
                expression { return env.CHANGED_SERVICES?.size() > 0 }
            }
            parallel {
                stage('Customers Service') {
                    when {
                        expression { return env.CHANGED_SERVICES.contains("spring-petclinic-customers-service") }
                    }
                    steps {
                        dir('spring-petclinic-customers-service') {
                            sh 'mvn clean test'
                            junit 'target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Vets Service') {
                    when {
                        expression { return env.CHANGED_SERVICES.contains("spring-petclinic-vets-service") }
                    }
                    steps {
                        dir('spring-petclinic-vets-service') {
                            sh 'mvn clean test'
                            junit 'target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Visits Service') {
                    when {
                        expression { return env.CHANGED_SERVICES.contains("spring-petclinic-visits-service") }
                    }
                    steps {
                        dir('spring-petclinic-visits-service') {
                            sh 'mvn clean test'
                            junit 'target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('GenAI Service') {
                    when {
                        expression { return env.CHANGED_SERVICES.contains("spring-petclinic-genai-service") }
                    }
                    steps {
                        dir('spring-petclinic-genai-service') {
                            sh 'mvn clean test'
                            junit 'target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }

        stage('Build Services') {
            when {
                expression { return env.CHANGED_SERVICES?.size() > 0 }
            }
            parallel {
                stage('Customers Service') {
                    when {
                        expression { return env.CHANGED_SERVICES.contains("spring-petclinic-customers-service") }
                    }
                    steps {
                        dir('spring-petclinic-customers-service') {
                            sh 'mvn clean package -DskipTests'
                            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                        }
                    }
                }
                stage('Vets Service') {
                    when {
                        expression { return env.CHANGED_SERVICES.contains("spring-petclinic-vets-service") }
                    }
                    steps {
                        dir('spring-petclinic-vets-service') {
                            sh 'mvn clean package -DskipTests'
                            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                        }
                    }
                }
                stage('Visits Service') {
                    when {
                        expression { return env.CHANGED_SERVICES.contains("spring-petclinic-visits-service") }
                    }
                    steps {
                        dir('spring-petclinic-visits-service') {
                            sh 'mvn clean package -DskipTests'
                            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                        }
                    }
                }
                stage('GenAI Service') {
                    when {
                        expression { return env.CHANGED_SERVICES.contains("spring-petclinic-genai-service") }
                    }
                    steps {
                        dir('spring-petclinic-genai-service') {
                            sh 'mvn clean package -DskipTests'
                            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
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
