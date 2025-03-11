pipeline {
    agent any
    tools {
        maven 'Maven 3'
    }

    stages {
        stage('Checkout Code') {
            steps {
                script {
                    checkout scm // Fetches the latest Git changes
                }
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def lastCommit = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim()

                    if (lastCommit == "none") {
                        echo "No previous commit found. Running full build."
                        env.CHANGED_SERVICES = ["spring-petclinic-customers-service", "spring-petclinic-vets-service", "spring-petclinic-visits-service", "spring-petclinic-genai-service"]
                    } else {
                        def changedFiles = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim().split("\n")
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
                            timeout(time: 10, unit: 'MINUTES') {
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
