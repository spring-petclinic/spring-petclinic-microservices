pipeline {
    agent any

    tools {
        maven 'maven3.9.9'
    }

    options {
        skipDefaultCheckout()
    }

    environment {
        BUILD_VETS = "false"
        BUILD_VISITS = "false"
        BUILD_CUSTOMERS = "false"
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    checkout scm
                }
            }
        }
        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()
                    echo "Changed files:\n${changedFiles}"

                    env.BUILD_VETS = changedFiles.split("\n").find { it.startsWith("spring-petclinic-vets-service/") } ? "true" : "false"
                    env.BUILD_VISITS = changedFiles.split("\n").find { it.startsWith("spring-petclinic-visits-service/") } ? "true" : "false"
                    env.BUILD_CUSTOMERS = changedFiles.split("\n").find { it.startsWith("spring-petclinic-customers-service/") } ? "true" : "false"


                    echo "BUILD_VETS: ${env.BUILD_VETS}"
                    echo "BUILD_VISITS: ${env.BUILD_VISITS}"
                    echo "BUILD_CUSTOMERS: ${env.BUILD_CUSTOMERS}"
                }
            }
        }

        stage('Build & Test Services') {
            matrix {
                axes {
                    axis {
                        name 'SERVICE'
                        values 'spring-petclinic-vets-service', 
                               'spring-petclinic-visits-service', 
                               'spring-petclinic-customers-service'
                    }
                }
                when {
                    expression {
                        return (SERVICE == 'spring-petclinic-vets-service' && env.BUILD_VETS == "true") ||
                               (SERVICE == 'spring-petclinic-visits-service' && env.BUILD_VISITS == "true") ||
                               (SERVICE == 'spring-petclinic-customers-service' && env.BUILD_CUSTOMERS == "true")
                    }
                }

                stages {
                    stage('Build') {
                        steps {
                            dir("${SERVICE}") {
                                sh "mvn clean package -DskipTests"
                            }
                        }
                    }
                    stage('Test & Coverage') {
                        steps {
                            dir("${SERVICE}") {
                                sh "mvn test"
                                junit '**/target/surefire-reports/*.xml'
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline completed!"
        }
    }
}
