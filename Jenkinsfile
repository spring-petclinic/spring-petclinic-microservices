pipeline {
    agent any

    tools {
        maven 'Maven 3.8.7'
    }

    environment {
        GITHUB_TOKEN = credentials('github-token') 
    }

    stages {
        stage('Test') {
            steps {
                script {
                    githubNotify context: 'Test', status: 'PENDING'
                    sh './mvnw test'
                    githubNotify context: 'Test', status: 'SUCCESS'
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    githubNotify context: 'Build', status: 'PENDING'
                    sh './mvnw package -DskipTests'
                    githubNotify context: 'Build', status: 'SUCCESS'
                }
            }
        }
    }
}
