pipeline {
    agent any

    tools {
        maven 'Maven 3.8.7' // hoặc version bạn cài trong Jenkins
    }

    environment {
        GITHUB_TOKEN = credentials('github-token') // tạo credential trong Jenkins
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
        always {
            echo 'Pipeline finished'
        }
    }
}
