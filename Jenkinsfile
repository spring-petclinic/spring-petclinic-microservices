pipeline {
    agent any
    
    environment {
        SERVICE_CHANGED = '' // Đã được set từ stage Check Changes
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
                sh "./mvnw clean verify -pl ${env.SERVICE_CHANGED} -am"
                
                echo "Checking if Jacoco coverage report was generated..."
                sh "ls -la ${env.SERVICE_CHANGED}/target/site/"
            }
            post {
                always {
                    junit "${env.SERVICE_CHANGED}/target/surefire-reports/*.xml"
                    archiveArtifacts artifacts: "${env.SERVICE_CHANGED}/target/site/jacoco/*", fingerprint: true
                }
            }
        }
        stage('Check Coverage') {
            when {
                expression { return env.SERVICE_CHANGED != '' }
            }
            steps {
                script {
                    def coverageHtml = sh(
                        script: "xmllint --html --xpath 'string(//table[@id=\"coveragetable\"]/tfoot/tr/td[3])' ${env.SERVICE_CHANGED}/target/site/jacoco/index.html 2>/dev/null",
                        returnStdout: true
                    ).trim()
        
                    def coverage = coverageHtml.replace('%', '').toFloat() / 100
                    echo "Test Coverage: ${coverage * 100}%"
        
                    if (coverage < 0.70) {
                        error "Coverage below 70%! Pipeline failed."
                    }
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
