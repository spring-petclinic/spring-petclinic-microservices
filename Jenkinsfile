pipeline {
    agent any
    environment {
        MAVEN_HOME = tool 'Maven'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Test') {
            steps {
                // Run tests with JaCoCo coverage
                sh './mvnw test verify -Pcoverage'
            }
            post {
                always {
                    // Publish test results
                    junit '**/target/surefire-reports/*.xml'
                    
                    // Publish JaCoCo coverage report
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java',
                        exclusionPattern: '**/test/**'
                    )
                }
            }
        }
        stage('Build') {
            steps {
                sh './mvnw package -DskipTests'
            }
            post {
                success {
                    // Archive the built artifacts
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                }
            }
        }
    }
    // Add post-pipeline actions
    post {
        always {
            // Clean workspace after build
            cleanWs()
        }
    }
}