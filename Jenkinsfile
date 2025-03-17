pipeline {
    agent any

    stages {
        stage('Checkout Code') {
            steps {
                script {
                    def branchToCheckout = env.BRANCH_NAME ?: 'master'
                    echo "Checkout branch: ${branchToCheckout}"
                    git branch: branchToCheckout, url: 'https://github.com/tranductung07012004/devOps_1_spring-petclinic-microservices.git'
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    echo "Performing regular build..."
                    // Build thông thường bỏ qua tests để tránh chạy lại test
                    sh './mvnw clean install -DskipTests'
                }
            }
        }

        stage('Test') {
            steps {
                echo "Running tests..."
                // Chạy test với Maven wrapper
                sh './mvnw clean test'
            }
            post {
                always {
                    echo "Publishing test results..."
                    // Định dạng đường dẫn tới file kết quả test, có thể điều chỉnh theo cấu trúc dự án
                    junit '**/target/surefire-reports/*.xml'
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
                    url: "https://api.github.com/repos/tranductung07012004/devOps_1_spring-petclinic-microservices/statuses/${commitId}",
                    httpMode: 'POST',
                    contentType: 'APPLICATION_JSON',
                    requestBody: """{
                        "state": "success",
                        "description": "Build passed",
                        "context": "ci/jenkins-pipeline",
                        "target_url": "${env.BUILD_URL}"
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
                    url: "https://api.github.com/repos/tranductung07012004/devOps_1_spring-petclinic-microservices/statuses/${commitId}",
                    httpMode: 'POST',
                    contentType: 'APPLICATION_JSON',
                    requestBody: """{
                        "state": "failure",
                        "description": "Build failed",
                        "context": "ci/jenkins-pipeline",
                        "target_url": "${env.BUILD_URL}"
                    }""",
                    authentication: 'github-token'
                )
                echo "GitHub Response: ${response.status}"
            }
        }

        always {
            echo "Pipeline finished. Commit SHA: ${params.commit_sha}"
        }
    }
} 