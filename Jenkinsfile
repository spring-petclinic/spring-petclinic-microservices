pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                script {
                    def changedFiles = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim().split('\n')
                    if (changedFiles.any { it.startsWith('vets-service/') }) {
                        echo 'Running tests for vets-service...'
                        sh './mvnw test'
                    } else {
                        echo 'No relevant changes, skipping tests.'
                    }
                }
            }
        }
        stage('Build') {
            steps {
                echo 'Building application...'
                sh './mvnw package'
            }
        }
    }
}

