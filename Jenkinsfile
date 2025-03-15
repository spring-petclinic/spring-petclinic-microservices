pipeline {
    agent any
    
    environment {
        OTHER = '' // Danh sách các service thay đổi
    }
    
    stages {
        stage('Check Changes') {
            steps {
                script {
                    def changedFiles = []
                    env.NO_SERVICES_TO_BUILD = 'false'
                    if (env.CHANGE_TARGET) {
                        // Nếu đây là PR build
                        echo "Pull request detected. Skipping CI execution. Only checking status."
                        return
                    } else {
                        // Nếu đây là branch build
                        changedFiles = sh(script: "git diff --name-only HEAD^", returnStdout: true).trim().split('\n')
                    }
                    
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
                        env.NO_SERVICES_TO_BUILD = 'true'
                    } else {
                        echo "Detected Services: ${detectedServices}"
                        env.SERVICE_CHANGED = detectedServices.join(",")
                        echo "Changes detected in services: ${env.SERVICE_CHANGED}"
                    }
                }
            }
        }
        
        stage('Test & Coverage') {
            when {
                expression { env.NO_SERVICES_TO_BUILD == 'false' && !env.CHANGE_ID}
            }
            steps {
                script {
                    def services = env.SERVICE_CHANGED.split(',')
                    for (service in services) {
                        echo "Running unit tests for service: ${service}"
                        sh "./mvnw clean verify -pl ${service} -am"

                        echo "Checking if Jacoco coverage report was generated for ${service}..."
                        sh "ls -la ${service}/target/site/"
                    }
                }
            }
            post {
                always {
                    script {
                        def services = env.SERVICE_CHANGED.split(',')
                        for (service in services) {
                            junit "${service}/target/surefire-reports/*.xml"
                            archiveArtifacts artifacts: "${service}/target/site/jacoco/*", fingerprint: true
                        }
                    }
                }
            }
        }

        stage('Check Coverage') {
            when {
                expression { env.NO_SERVICES_TO_BUILD == 'false' && !env.CHANGE_ID}
            }
            steps {
                script {
                    def services = env.SERVICE_CHANGED.split(',')
                    def failedCoverageServices = []

                    for (service in services) {
                        def coverageHtml = sh(
                            script: "xmllint --html --xpath 'string(//table[@id=\"coveragetable\"]/tfoot/tr/td[3])' ${service}/target/site/jacoco/index.html 2>/dev/null",
                            returnStdout: true
                        ).trim()
            
                        def coverage = coverageHtml.replace('%', '').toFloat() / 100
                        echo "Test Coverage for ${service}: ${coverage * 100}%"
            
                        if (coverage < 0.70) {
                            failedCoverageServices << service
                        }
                    }

                    if (failedCoverageServices.size() > 0) {
                        error "Coverage below 70% for services: ${failedCoverageServices.join(', ')}! Pipeline failed."
                    }
                }
            }
        }

        stage('Build') {
            when {
                expression { env.NO_SERVICES_TO_BUILD == 'false' && !env.CHANGE_ID}
            }
            steps {
                script {
                    def services = env.SERVICE_CHANGED.split(',')
                    for (service in services) {
                        echo "Building service: ${service}"
                        sh "./mvnw package -pl ${service} -am -DskipTests"
                    }
                }
            }
        }
    }
    
    post {
        success {
            script {
                withCredentials([string(credentialsId: 'GITHUB_TOKEN', variable: 'GITHUB_TOKEN')]) {
                    sh """
                    curl -X POST -H "Authorization: token ${GITHUB_TOKEN}" \
                        -d '{"state": "success", "context": "Jenkins CI", "description": "CI passed!"}' \
                        https://api.github.com/repos/nghiaz1692004/DevOps_Project1.git/statuses/\${GIT_COMMIT}
                    """
                }
            }
        }
        failure {
            script {
                withCredentials([string(credentialsId: 'GITHUB_TOKEN', variable: 'GITHUB_TOKEN')]) {
                    sh """
                    curl -X POST -H "Authorization: token ${GITHUB_TOKEN}" \
                        -d '{"state": "failure", "context": "Jenkins CI", "description": "CI failed!"}' \
                        https://api.github.com/repos/nghiaz1692004/DevOps_Project1.git/statuses/\${GIT_COMMIT}
                    """
                }
            }
        }
    }
}
