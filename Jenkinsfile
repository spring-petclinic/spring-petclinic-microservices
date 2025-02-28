pipeline {
    agent any

    environment {
        CHANGED_FILES = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    echo "Changed files: ${CHANGED_FILES}"
                    if (!CHANGED_FILES.contains("vets-service/")) {
                        echo "No changes in vets-service. Skipping build & test."
                        currentBuild.result = 'SUCCESS'
                        error("Skipping pipeline as no changes detected in vets-service")
                    }
                }
            }
        }

        stage('Build') {
            steps {
                echo 'Building vets-service...'
                sh 'cd vets-service && mvn clean package'
            }
        }

        stage('Test') {
            steps {
                echo 'Running tests for vets-service...'
                sh 'cd vets-service && mvn test'
            }
            post {
                always {
                    junit 'vets-service/target/surefire-reports/*.xml'
                    cobertura coberturaReportFile: 'vets-service/target/site/cobertura/coverage.xml'
                }
            }
        }
    }
}
