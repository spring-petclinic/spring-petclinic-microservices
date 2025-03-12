pipeline {
    agent any

    environment {
        GIT_REPO = 'https://github.com/nghiaz160904/spring-petclinic-microservices.git'
    }

    stages {
        stage('Checkout Code') {
                steps {
                    withCredentials([string(credentialsId: 'GITHUB_CREDENTIALS', variable: 'GIT_TOKEN')]) {
                        git branch: 'main', url: "https://${GIT_TOKEN}@github.com/nghiaz160904/spring-petclinic-microservices.git"
                    }
                }
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = []
                    try {
                        changedFiles = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim().split("\n")
                    } catch (Exception e) {
                        echo "No previous commit found, running full build."
                    }

                    env.SHOULD_BUILD_CUSTOMERS = changedFiles.any { it.startsWith("customers-service/") } ? "true" : "false"
                    env.SHOULD_BUILD_VETS = changedFiles.any { it.startsWith("vets-service/") } ? "true" : "false"
                    env.SHOULD_BUILD_VISIT = changedFiles.any { it.startsWith("visit-service/") } ? "true" : "false"

                    echo "Changes detected:"
                    echo "SHOULD_BUILD_CUSTOMERS = ${env.SHOULD_BUILD_CUSTOMERS}"
                    echo "SHOULD_BUILD_VETS = ${env.SHOULD_BUILD_VETS}"
                    echo "SHOULD_BUILD_VISIT = ${env.SHOULD_BUILD_VISIT}"
                }
            }
        }

        stage('Test Services') {
            parallel {
                stage('Test Customers Service') {
                    when { expression { env.SHOULD_BUILD_CUSTOMERS == "true" } }
                    steps {
                        dir('customers-service') {
                            sh '../mvnw test'
                        }
                    }
                }

                stage('Test Vets Service') {
                    when { expression { env.SHOULD_BUILD_VETS == "true" } }
                    steps {
                        dir('vets-service') {
                            sh '../mvnw test'
                        }
                    }
                }

                stage('Test Visit Service') {
                    when { expression { env.SHOULD_BUILD_VISIT == "true" } }
                    steps {
                        dir('visit-service') {
                            sh '../mvnw test'
                        }
                    }
                }
            }
        }

        stage('Build Services') {
            parallel {
                stage('Build Customers Service') {
                    when { expression { env.SHOULD_BUILD_CUSTOMERS == "true" } }
                    steps {
                        dir('customers-service') {
                            sh '../mvnw clean package -DskipTests'
                        }
                    }
                }

                stage('Build Vets Service') {
                    when { expression { env.SHOULD_BUILD_VETS == "true" } }
                    steps {
                        dir('vets-service') {
                            sh '../mvnw clean package -DskipTests'
                        }
                    }
                }

                stage('Build Visit Service') {
                    when { expression { env.SHOULD_BUILD_VISIT == "true" } }
                    steps {
                        dir('visit-service') {
                            sh '../mvnw clean package -DskipTests'
                        }
                    }
                }
            }
        }
    }
}
