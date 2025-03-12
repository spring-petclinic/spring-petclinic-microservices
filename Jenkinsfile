pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/<your-repo>.git'
            }
        }
        stage('Test') {
            steps {
                sh './mvnw test'
                junit '**/target/surefire-reports/*.xml'
            }
        }
        stage('Build') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
        }
    }
}
