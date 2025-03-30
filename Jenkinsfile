pipeline {
    agent any

    tools {
        maven 'maven3.9.9' // Tên Maven trong Global Tool Configuration
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
                dir('spring-petclinic-vets-service') {
                    sh "mvn clean package -DskipTests"
                    sh "mvn test"
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
                    sh "mvn clean package -DskipTests"
                    sh "mvn test"
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
                    sh "mvn clean package -DskipTests"
                    sh "mvn test"
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
