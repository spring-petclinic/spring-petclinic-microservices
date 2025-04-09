pipeline {
    agent any

    tools {
        maven 'Maven 3.8.7'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                echo 'Running tests...'
                sh './mvnw test'
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('Build') {
            steps {
                echo 'Building...'
                sh './mvnw package -DskipTests'
            }
        }
    }

    post {
        success {
            echo 'Build and test succeeded.'
        }
        failure {
            echo 'Build or test failed.'
        }
    }
}
