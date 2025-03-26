pipeline {
    agent any

    tools {
        maven '3.9.9'
    }

    stages {
        stage('Test') {
            steps {
                echo 'Testing ...'
                sh 'mvn -version'
            }
        }

        stage('Building') {
            steps {
                echo 'Building ...'
                sh '''
                    cd spring-petclinic-visits-service
                    mvn clean install
                '''
            }
        }
    }
}