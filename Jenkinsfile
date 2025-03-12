pipeline {
    agent any
    environment {
        MAVEN_HOME = tool 'Maven'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Test') {
            steps {
                sh './mvnw test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        stage('Build') {
            steps {
                sh './mvnw package'
            }
        }
    }
}
