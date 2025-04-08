pipeline {
    agent any

    stages {
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
}
