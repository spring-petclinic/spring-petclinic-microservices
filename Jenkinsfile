pipeline {
    agent any
    environment {
        CHANGED_FILES = ''
        CHANGED_SERVICES = '' // Lưu danh sách các service bị thay đổi
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
                    env.CHANGED_FILES = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()
                    echo "Changed files:\n${env.CHANGED_FILES}"
                }
            }
        }

        stage('Test Changed Services') {
            steps {
                script {
                    def services = [
                        'spring-petclinic-customers-service',
                        'spring-petclinic-genai-service',
                        'spring-petclinic-vets-service',
                        'spring-petclinic-visits-service'
                    ]
                    def changedServices = []

                    if (env.CHANGED_FILES && env.CHANGED_FILES.trim()) {
                        def changedFilesList = env.CHANGED_FILES.split('\n')
                        for (service in services) {
                            if (changedFilesList.find { it.startsWith(service) }) {
                                echo "Running tests for ${service}..."
                                changedServices.add(service)
                                dir(service) {
                                    // Chạy unit test
                                    sh './mvnw clean test'
                                }
                            } else {
                                echo "Skipping test for ${service}, no changes detected."
                            }
                        }
                    } else {
                        echo "No changed files detected. Skipping test phase."
                    }
                    env.CHANGED_SERVICES = changedServices.join(' ')
                }
            }
            post {
                always {
                    echo "Publishing test results and code coverage..."
                    junit allowEmptyResults: true, testResults: '*/target/surefire-reports/.xml'
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java',
                        skipIfNoReports: true
                    )
                    archiveArtifacts artifacts: '*/surefire-reports/.xml', fingerprint: true
                }
            }
        }

        stage('Build Changed Services') {
            steps {
                script {
                    if (env.CHANGED_SERVICES && env.CHANGED_SERVICES.trim()) {
                        def services = env.CHANGED_SERVICES.split(' ')
                        for (service in services) {
                            echo "Building ${service}..."
                            dir(service) {
                                sh './mvnw clean install -DskipTests'
                            }
                        }
                    } else {
                        echo "No services to build."
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                def commitId = env.GIT_COMMIT
                echo "Sending 'success' status to GitHub for commit: ${commitId}"
                def response = httpRequest(
                    url: "https://api.github.com/repos/ndmanh3003/spring-petclinic-microservices/statuses/${commitId}",
                    httpMode: 'POST',
                    contentType: 'APPLICATION_JSON',
                    requestBody: """{
                        \"state\": \"success\",
                        \"description\": \"Build passed\",
                        \"context\": \"ci/jenkins-pipeline\",
                        \"target_url\": \"${env.BUILD_URL}\"
                    }""",
                    authentication: 'github-token'
                )
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
