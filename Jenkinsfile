pipeline {
    agent any

    tools {
        maven '3.9.9'
    }

    stages {
        stage('Test') {
            steps {
                sh 'mvn -version'
            }
        }
    }
}