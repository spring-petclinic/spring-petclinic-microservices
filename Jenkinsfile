pipeline {
    agent any

    environment {
        // Ensure Windows system commands can be found
        PATH = "C:\\Windows\\System32;${env.PATH}"
    }

    tools {
        maven "M3"
    }

    stages {
        stage('Test') {
            steps {
                checkout scm
                // Run tests and generate JaCoCo XML coverage report
                bat 'mvn clean test jacoco:report'
            }
            post {
                always {
                    // Publish JUnit test results
                    junit '**/target/surefire-reports/TEST-*.xml'

                    // Publish coverage using the Coverage plugin
                    recordCoverage tools: [
                        jacocoAdapter('**/target/site/jacoco/jacoco.xml')
                    ]
                }
            }
        }
        stage('Build') {
            steps {
                bat 'mvn package'
            }
        }
    }

    post {
        success {
            echo 'Build succeeded!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}
