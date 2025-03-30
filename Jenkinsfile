pipeline {
    agent any

    environment {
        REPO_URL = 'https://github.com/htloc0610/spring-petclinic-microservices'
        BRANCH = 'main'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    echo "Cloning repository ${REPO_URL}"
                    checkout scm
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    echo "Running tests..."
                    sh './mvnw test'
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    echo "Building project..."
                    sh './mvnw clean package'
                }
            }
        }

        stage('Post-Build') {
            steps {
                script {
                    echo "Uploading test reports..."
                    junit '**/target/surefire-reports/*.xml'
                    jacoco execPattern: '**/target/jacoco.exec'
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished"
        }
    }
}
