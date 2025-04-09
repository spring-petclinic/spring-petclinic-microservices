pipeline {
    agent any

    tools {
        maven 'Maven 3.8.7'
    }

    environment {
        GITHUB_TOKEN = credentials('github-token')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                script {
                    githubNotify context: 'Test', status: 'PENDING', description: 'Running tests'
                    sh './mvnw test'
                    githubNotify context: 'Test', status: 'SUCCESS', description: 'Tests passed'
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    githubNotify context: 'Build', status: 'PENDING', description: 'Building app'
                    sh './mvnw package -DskipTests'
                    githubNotify context: 'Build', status: 'SUCCESS', description: 'Build successful'
                }
            }
        }
    }
}
