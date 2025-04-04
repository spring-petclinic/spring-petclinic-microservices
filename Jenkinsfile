pipeline {
    agent {
      label 'slave1'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Cloning repository...'
            }
        }

        stage('Install dependencies') {
            steps {
                echo 'Installing npm packages...'
            }
        }

        stage('Run Tests') {
            steps {
                echo 'Running tests...'
            }
        }

        stage('Build') {
            steps {
                echo 'Building the application...'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully ✅'
        }
        failure {
            echo 'Pipeline failed ❌'
        }
    }
}
