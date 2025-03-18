pipeline {
    agent any
    environment {
        CHANGED_FILES = ''
    }
    stages {
        stage('Checkout Code') {
            steps {
                script {
                    def branchToCheckout = env.BRANCH_NAME ?: 'main'
                    echo "Checkout branch: ${branchToCheckout}"
                    git branch: branchToCheckout, 
                        url: 'https://github.com/ndmanh3003/spring-petclinic-microservices.git'
                }
            }
        }

        stage('Detect changes') {
            steps {
                script {
                    CHANGED_FILES = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()
                    echo "Changed files:\n${CHANGED_FILES}"
                }
            }
        }

        stage('Test & Build Changed Services') {
            steps {
                script {
                    def services = [
                        'spring-petclinic-customers-service',
                        'spring-petclinic-genai-service',
                        'spring-petclinic-vets-service',
                        'spring-petclinic-visits-service'
                    ]

                    for (service in services) {
                        if (CHANGED_FILES.contains(service)) {
                            echo "Building and testing ${service}..."
                            dir(service) {
                                sh '../mvnw clean test'
                                sh '../mvnw clean install -DskipTests'
                            }
                        } else {
                            echo "Skipping ${service}, no changes detected."
                        }
                    }
                }
            }
        }

        stage('Check Changed Files') {
            steps {
                script {
                    def changedFiles = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()
                    echo "Files changed in this commit: ${changedFiles}"
                }
            }
        }
    }

    post {
        success {
            script {
                def commitId = env.GIT_COMMIT
                echo "Sending 'success' status to GitHub for commit: ${commitId}"
                def response = httpRequest
                                url: "https://api.github.com/repos/ndmanh3003/spring-petclinic-microservices/statuses/${commitId}",
                                httpMode: 'POST',
                                acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON',
                                requestBody: """{
                                    \"state\": \"success\",
                                    \"description\": \"Build passed\",
                                    \"context\": \"ci/jenkins-pipeline\",
                                    \"target_url\": \"${env.BUILD_URL}\"
                                }""",
                                authentication: 'github-token-ndmanh'
                
                echo "GitHub Response: ${response.status}"
            }
        }

        failure {
            script {
                def commitId = env.GIT_COMMIT
                echo "Sending 'failure' status to GitHub for commit: ${commitId}"
                def response = httpRequest(
                    url: "https://api.github.com/repos/ndmanh3003/spring-petclinic-microservices/statuses/${commitId}",
                    httpMode: 'POST',
                    contentType: 'APPLICATION_JSON',
                    requestBody: """{
                        \"state\": \"failure\",
                        \"description\": \"Build failed\",
                        \"context\": \"ci/jenkins-pipeline\",
                        \"target_url\": \"${env.BUILD_URL}\"
                    }""",
                    authentication: 'github-token'
                )
                echo "GitHub Response: ${response.status}"
            }
        }

        always {
            echo "Pipeline finished."
        }
    }
}
