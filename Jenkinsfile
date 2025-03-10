pipeline {
    agent any
    environment {
        MAVEN_OPTS = "-Xmx1024m" // Prevents Jenkins from running out of memory
    }
    tools {
        maven 'Maven 3' // Use Jenkins' built-in Maven
    }
    
    stages {
        stage('Clean Workspace') {
            steps {
                cleanWs() // Remove old builds to prevent conflicts
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = sh(script: 'git diff --name-only $(git rev-parse HEAD~1 || echo HEAD)', returnStdout: true).trim().split("\n")
                    env.CHANGED_SERVICES = []

                    def services = [
                        "spring-petclinic-customers-service",
                        "spring-petclinic-vets-service",
                        "spring-petclinic-visits-service",
                        "spring-petclinic-genai-service"
                    ]

                    for (service in services) {
                        if (changedFiles.any { it.startsWith(service) }) {
                            env.CHANGED_SERVICES += service
                        }
                    }

                    if (env.CHANGED_SERVICES.isEmpty()) {
                        echo "No relevant changes detected. Skipping tests and build."
                        currentBuild.result = 'SUCCESS'
                        return
                    } else {
                        echo "Detected changes in: ${env.CHANGED_SERVICES.join(', ')}"
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
                    when { expression { return env.CHANGED_SERVICES.contains("spring-petclinic-customers-service") } }
                    steps {
                        dir('spring-petclinic-customers-service') {
                            timeout(time: 10, unit: 'MINUTES') { // Avoid test hangups
                                sh 'mvn clean test'
                            }
                            junit 'target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Vets Service') {
                    when { expression { return env.CHANGED_SERVICES.contains("spring-petclinic-vets-service") } }
                    steps {
                        dir('spring-petclinic-vets-service') {
                            timeout(time: 10, unit: 'MINUTES') {
                                sh 'mvn clean test'
                            }
                            junit 'target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Visits Service') {
                    when { expression { return env.CHANGED_SERVICES.contains("spring-petclinic-visits-service") } }
                    steps {
                        dir('spring-petclinic-visits-service') {
                            timeout(time: 10, unit: 'MINUTES') {
                                sh 'mvn clean test'
                            }
                            junit 'target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('GenAI Service') {
                    when { expression { return env.CHANGED_SERVICES.contains("spring-petclinic-genai-service") } }
                    steps {
                        dir('spring-petclinic-genai-service') {
                            timeout(time: 10, unit: 'MINUTES') {
                                sh 'mvn clean test'
                            }
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
                    when { expression { return env.CHANGED_SERVICES.contains("spring-petclinic-customers-service") } }
                    steps {
                        dir('spring-petclinic-customers-service') {
                            sh 'mvn clean package -DskipTests'
                            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                        }
                    }
                }
                stage('Vets Service') {
                    when { expression { return env.CHANGED_SERVICES.contains("spring-petclinic-vets-service") } }
                    steps {
                        dir('spring-petclinic-vets-service') {
                            sh 'mvn clean package -DskipTests'
                            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                        }
                    }
                }
                stage('Visits Service') {
                    when { expression { return env.CHANGED_SERVICES.contains("spring-petclinic-visits-service") } }
                    steps {
                        dir('spring-petclinic-visits-service') {
                            sh 'mvn clean package -DskipTests'
                            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                        }
                    }
                }
                stage('GenAI Service') {
                    when { expression { return env.CHANGED_SERVICES.contains("spring-petclinic-genai-service") } }
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
