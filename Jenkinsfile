pipeline {
    agent any

    stages {
        stage('Test') {
            steps {
                echo 'Running Tests...'
                sh './mvnw clean test'
            }
            post {
                always {
                    junit '*/target/surefire-reports/.xml'
                }
            }
        }

        stage('Code Coverage') {
            steps {
                echo 'Generating Coverage Report...'
                sh './mvnw org.jacoco:jacoco-maven-plugin:report'
                recordCoverage(tools: [[parser: 'JACOCO']])
            }
        }

        stage('Build') {
            steps {
                echo 'Building Application...'
                sh './mvnw clean install'
            }
            post {
                success {
                    archiveArtifacts artifacts: '*/target/.jar', fingerprint: true
                }
            }
        }
    }
}
