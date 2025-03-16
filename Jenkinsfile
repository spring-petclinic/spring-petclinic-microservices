pipeline {
    agent none  // Không chạy trên Master, chỉ điều phối

    environment {
        GIT_COMMIT = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
        NO_SERVICES_TO_BUILD = 'false'
        SERVICE_CHANGED = ''
    }

    stages {
        stage('Check Changes') {
            agent { label 'master' } // Chạy trên Master
            steps {
                script {
                    echo "Commit SHA: ${GIT_COMMIT}"
                    def changedFiles = []
                    
                    if (env.CHANGE_TARGET) {
                        // Nếu đây là Pull Request (PR) build
                        changedFiles = sh(script: "git diff --name-only origin/${env.CHANGE_TARGET}...", returnStdout: true).trim().split('\n')
                    } else {
                        // Nếu đây là branch build
                        changedFiles = sh(script: "git diff --name-only HEAD^", returnStdout: true).trim().split('\n')
                    }

                    def services = ['spring-petclinic-customers-service', 'spring-petclinic-visits-service', 'spring-petclinic-vets-service']
                    
                    echo "Changed files: ${changedFiles}"

                    if (changedFiles.isEmpty() || changedFiles[0] == '') {
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
                    }
                }
            }
        }

        stage('Test & Coverage - Agent 1') {
            agent { label 'agent1' }  
            when {
                expression { env.NO_SERVICES_TO_BUILD == 'false' && (env.SERVICE_CHANGED.contains('customers-service') || env.SERVICE_CHANGED.contains('visits-service')) }
            }
            steps {
                script {
                    def services = env.SERVICE_CHANGED.split(',').findAll { it in ['spring-petclinic-customers-service', 'spring-petclinic-visits-service'] }
                    for (service in services) {
                        echo "Running unit tests for service: ${service} on Agent 1"
                        sh "./mvnw clean verify -pl ${service} -am"
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

        stage('Test & Coverage - Agent 2') {
            agent { label 'agent2' }  
            when {
                expression { env.NO_SERVICES_TO_BUILD == 'false' && env.SERVICE_CHANGED.contains('vets-service') }
            }
            steps {
                script {
                    echo "Running unit tests for vets-service on Agent 2"
                    sh "./mvnw clean verify -pl spring-petclinic-vets-service -am"
                }
            }
            post {
                always {
                    junit "spring-petclinic-vets-service/target/surefire-reports/*.xml"
                    archiveArtifacts artifacts: "spring-petclinic-vets-service/target/site/jacoco/*", fingerprint: true
                }
            }
        }

        stage('Build - Agent 1') {
            agent { label 'agent1' }
            when {
                expression { env.NO_SERVICES_TO_BUILD == 'false' && (env.SERVICE_CHANGED.contains('customers-service') || env.SERVICE_CHANGED.contains('visits-service')) }
            }
            steps {
                script {
                    def services = env.SERVICE_CHANGED.split(',').findAll { it in ['spring-petclinic-customers-service', 'spring-petclinic-visits-service'] }
                    for (service in services) {
                        echo "Building service: ${service} on Agent 1"
                        sh "./mvnw package -pl ${service} -am -DskipTests"
                    }
                }
            }
        }

        stage('Build - Agent 2') {
            agent { label 'agent2' }
            when {
                expression { env.NO_SERVICES_TO_BUILD == 'false' && env.SERVICE_CHANGED.contains('vets-service') }
            }
            steps {
                script {
                    echo "Building vets-service on Agent 2"
                    sh "./mvnw package -pl spring-petclinic-vets-service -am -DskipTests"
                }
            }
        }
    }
    
    post {
        success {
            script {
                withCredentials([string(credentialsId: 'GITHUB_TOKEN', variable: 'GITHUB_TOKEN')]) {
                    sh '''
                    curl -X POST \
                      -H "Authorization: token ${GITHUB_TOKEN}" \
                      -H "Content-Type: application/json" \
                      -d '{"state": "success", "context": "Jenkins CI", "description": "CI passed!"}' \
                      "https://api.github.com/repos/pTn-3001/DevOps_Project1/statuses/${GIT_COMMIT}"
                    '''
                }
            }
        }
        failure {
            script {
                withCredentials([string(credentialsId: 'GITHUB_TOKEN', variable: 'GITHUB_TOKEN')]) {
                    sh '''
                    curl -X POST \
                      -H "Authorization: token ${GITHUB_TOKEN}" \
                      -H "Content-Type: application/json" \
                      -d '{"state": "failure", "context": "Jenkins CI", "description": "CI failed!"}' \
                      "https://api.github.com/repos/pTn-3001/DevOps_Project1/statuses/${GIT_COMMIT}"
                    '''
                }
            }
        }
    }
}
