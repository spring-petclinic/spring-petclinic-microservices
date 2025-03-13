pipeline {
    agent any
    
    environment {
        LSERVICE_CHANGED = '' // Đã được set từ stage Check Changes
    }
    
    stages {
        stage('Check Changes') {
            steps {
                script {
                    def changedFiles = sh(script: "git diff --name-only origin/main", returnStdout: true).trim().split("\n")
                    def services = ['spring-petclinic-customers-service', 'spring-petclinic-vets-service', 'spring-petclinic-visits-service']
                    
                    echo "Changed files: ${changedFiles}"
                    
                    if (changedFiles.size() == 0 || changedFiles[0] == '') {
                        echo "No changes detected. Skipping pipeline."
                        currentBuild.result = 'ABORTED'
                        return
                    }
                    
                    def detectedServices = []
                    for (service in services) {
                        if (changedFiles.any { it.startsWith(service + '/') }) {
                            detectedServices << service
                        }
                    }
                    
                    if (detectedServices.isEmpty()) {
                        echo "No relevant service changes detected. Skipping pipeline."
                        currentBuild.result = 'ABORTED'
                        return
                    }
                    echo "Detected Services: ${detectedServices}"
                    env.SERVICE_CHANGED = detectedServices.join(",")
                    echo "Changes detected in services: ${env.SERVICE_CHANGED}"
                }
            }
        }
        
        stage('Test & Coverage') {
            when {
                expression { return env.SERVICE_CHANGED != '' }
            }
            steps {
                echo "Running unit tests for service: ${env.SERVICE_CHANGED}"
                sh "./mvnw clean test -pl ${env.SERVICE_CHANGED} -am"
                
                echo "Generating Jacoco coverage report..."
                sh "./mvnw jacoco:report -pl ${env.SERVICE_CHANGED} -am"
                
                echo "Extracting test coverage..."
                script {
                    def coverageReport = sh(
                        script: "grep -A 1 '<td>Total</td>' ${env.SERVICE_CHANGED}/target/site/jacoco/index.html | tail -1",
                        returnStdout: true
                    ).trim()
                    echo "Test Coverage Report: ${coverageReport}"
                }
            }
            post {
                always {
                    junit "${env.SERVICE_CHANGED}/target/surefire-reports/*.xml"
                    archiveArtifacts artifacts: "${env.SERVICE_CHANGED}/target/site/jacoco/*", fingerprint: true
                }
            }
        }
        
        stage('Build') {
            when {
                expression { return env.SERVICE_CHANGED != '' }
            }
            steps {
                echo "Building service: ${env.SERVICE_CHANGED}"
                sh "./mvnw package -pl ${env.SERVICE_CHANGED} -am -DskipTests"
            }
        }
    }
    
    post {
        success {
            echo "Pipeline completed successfully for service: ${env.SERVICE_CHANGED}"
        }
        failure {
            echo "Pipeline failed for service: ${env.SERVICE_CHANGED}"
        }
    }
}
