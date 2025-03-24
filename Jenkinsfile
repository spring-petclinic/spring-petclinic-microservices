pipeline {
    agent any

    stages {
        stage('Test') {
            steps {
                echo 'Testing ...'
            }
        }

        stage('Build') {
            steps {
                echo 'Building ...'
                sh 'mvn clean install -Dmaven.test.skip=true'
            }
        }
    }
}