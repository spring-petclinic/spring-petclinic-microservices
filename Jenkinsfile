pipeline {
    agent any
    tools {
        jdk 'JDK21'
        // maven 'Maven3'
    }

    stages {
        stage('Check Java & Maven') {
            steps {
                sh 'java -version'
                sh 'mvn -version'
            }
        }
        stage('Checkout Code') {
            steps {
                script {
                    echo "Checking out code from branch: ${env.BRANCH_NAME}"
                    checkout scm
                }
            }
        }

        // stage('Setup Environment') {
        //     steps {
        //         sh 'sudo dnf install -y java-17-openjdk maven'
        //         sh 'sudo dnf install maven -y'
        //     }
        // }

        stage('Run Unit Tests') {
            steps {
                script {
                    echo "Running tests for all"
                    sh "./mvnw test"
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    jacoco execPattern: '**/target/jacoco.exec'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "./mvnw clean install"
            }
        }
    }

    post {
        success { echo "Run pipeline for ${env.BRANCH_NAME} successfully" }
        failure { echo "Run pipeline for ${env.BRANCH_NAME} failed!" }
    }
}
