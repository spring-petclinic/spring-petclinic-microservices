pipeline {
    agent any

    environment {
        GITHUB_TOKEN = credentials('github-token')
        CHANGED_FILES = ''
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    def affectedServices = [] // Danh sách các service bị thay đổi
                    CHANGED_FILES = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim()
                    echo "Changed files: ${CHANGED_FILES}"

                    def changedFiles = CHANGED_FILES.split("\n")
                    for (file in changedFiles) {
                        if (file.startsWith("spring-petclinic-") && file.split("/").size() > 1) {
                            def service = file.split("/")[0]
                            if (!affectedServices.contains(service)) {
                                affectedServices << service
                            }
                        }
                    }

                    if (affectedServices.isEmpty()) {
                        echo "No relevant service changes detected. Skipping pipeline."
                        currentBuild.result = 'SUCCESS'
                        return
                    }

                    echo "Affected services: ${affectedServices}"
                    env.AFFECTED_SERVICES = affectedServices.join(',') // Lưu danh sách thành chuỗi cho các stage sau
                }
            }
        }

        stage('Test and Coverage') {
            when {
                expression { return env.AFFECTED_SERVICES != null && env.AFFECTED_SERVICES != "" }
            }
            steps {
                script {
                    def affectedServices = env.AFFECTED_SERVICES.split(',')
                    for (service in affectedServices) {
                        echo "Testing service: ${service} on ${env.NODE_NAME}"
                        dir(service) {
                            // Chạy test với JaCoCo
                            sh '../mvnw clean test'
                            // Tạo báo cáo JaCoCo
                            sh '../mvnw jacoco:report'
                        }
                    }
                }
            }
            post {
                always {
                    // Báo cáo kết quả test chỉ của các service đã chạy test
                    junit '**/target/surefire-reports/*.xml'

                    script {
                        def affectedServices = env.AFFECTED_SERVICES.split(',')
                        for (service in affectedServices) {
                            echo "Generating JaCoCo report for: ${service}"
                            jacoco(
                                execPattern: "${service}/target/jacoco.exec", // Chỉ lấy exec của service test
                                classPattern: "${service}/target/classes",
                                sourcePattern: "${service}/src/main/java",
                                exclusionPattern: "${service}/src/test/**",
                                minimumLineCoverage: '70', // Yêu cầu tối thiểu 70% coverage
                                changeBuildStatus: true // Thất bại nếu không đạt ngưỡng
                            )
                        }
                    }
                }
            }
        }

        stage('Build') {
            when {
                expression { return env.AFFECTED_SERVICES != null && env.AFFECTED_SERVICES != "" }
            }
            steps {
                script {
                    def affectedServices = env.AFFECTED_SERVICES.split(',')
                    for (service in affectedServices) {
                        echo "Building service: ${service} on ${env.NODE_NAME}"
                        dir(service) {
                            sh '../mvnw clean package -DskipTests'
                        }
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
