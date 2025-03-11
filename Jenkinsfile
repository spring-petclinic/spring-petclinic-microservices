pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                echo 'Building...'
                sh 'mvn clean package' // Build project
            }
        }
        stage('Test') {
            steps {
                echo 'Running tests...'
                sh 'mvn test' // Chạy test
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml' // Upload test results
                    cobertura coberturaReportFile: 'target/site/jacoco/jacoco.xml' // Upload code coverage
                }
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying...'
            }
        }
    }
}
