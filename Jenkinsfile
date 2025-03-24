pipeline {
    agent any

    stages {
        stage('PWD') {
            steps {
                pwd
            }
        }

        stage('Test') {
            steps {
                echo 'Testing ...'
            }
        }

        stage('Build') {
            steps {
                echo 'Building ...'
            }
        }
    }
}