pipeline {
    agent any

    environment {
        MAVEN_HOME = tool name: 'Maven', type: 'maven'
    }

    options {
        skipDefaultCheckout()
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

                    // Kiểm tra service nào có thay đổi
                    env.BUILD_VETS = changedFiles.contains("vets-service/")
                    env.BUILD_VISITS = changedFiles.contains("visits-service/")
                    env.BUILD_CUSTOMERS = changedFiles.contains("customers-service/")
                }
            }
        }

        stage('Build & Test Vets Service') {
            when {
                expression { env.BUILD_VETS == "true" }
            }
            steps {
                dir('vets-service') {
                    sh "${MAVEN_HOME}/bin/mvn clean package -DskipTests"
                    sh "${MAVEN_HOME}/bin/mvn test"
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build & Test Visits Service') {
            when {
                expression { env.BUILD_VISITS == "true" }
            }
            steps {
                dir('visits-service') {
                    sh "${MAVEN_HOME}/bin/mvn clean package -DskipTests"
                    sh "${MAVEN_HOME}/bin/mvn test"
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build & Test Customers Service') {
            when {
                expression { env.BUILD_CUSTOMERS == "true" }
            }
            steps {
                dir('customers-service') {
                    sh "${MAVEN_HOME}/bin/mvn clean package -DskipTests"
                    sh "${MAVEN_HOME}/bin/mvn test"
                    junit '**/target/surefire-reports/*.xml'
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
