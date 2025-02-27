pipeline {
    agent any
    environment {
        SERVICES_CHANGED = []
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    // Lấy danh sách file thay đổi so với main branch
                    def changes = sh(script: "git diff --name-only origin/main", returnStdout: true).trim().split("\n")

                    // Danh sách thư mục service
                    def services = [
                        "spring-petclinic-customers-service",
                        "spring-petclinic-vets-service",
                        "spring-petclinic-visits-service",
                        "spring-petclinic-api-gateway",
                        "spring-petclinic-config-server",
                        "spring-petclinic-admin-server",
                        "spring-petclinic-genai-service"
                    ]

                    // Kiểm tra thư mục nào có thay đổi
                    def changedServices = []
                    for (service in services) {
                        if (changes.any { it.startsWith(service + "/") }) {
                            changedServices.add(service)
                        }
                    }

                    // Nếu không có service nào thay đổi thì dừng pipeline
                    if (changedServices.isEmpty()) {
                        error("No relevant changes detected, skipping pipeline")
                    }

                    SERVICES_CHANGED = changedServices
                    echo "Services changed: ${SERVICES_CHANGED.join(', ')}"
                }
            }
        }

        stage('Test & Coverage Check') {
            parallel {
                script {
                    SERVICES_CHANGED.each { service ->
                        stage("Test & Coverage: ${service}") {
                            steps {
                                dir(service) {
                                    sh './mvnw test'

                                    // Kiểm tra test coverage
                                    script {
                                        def coverage = sh(script: "grep -Po '(?<=<counter type=\"LINE\" missed=\"\\d+\" covered=\")\\d+(?=\"/>)' target/site/jacoco/jacoco.xml | awk '{sum += $1} END {print sum}'", returnStdout: true).trim()
                                        if (coverage.toInteger() < 70) {
                                            error("Test coverage for ${service} is below 70%")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Build') {
            parallel {
                script {
                    SERVICES_CHANGED.each { service ->
                        stage("Build: ${service}") {
                            steps {
                                dir(service) {
                                    sh './mvnw package -DskipTests'
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Docker Build') {
            parallel {
                script {
                    SERVICES_CHANGED.each { service ->
                        stage("Docker Build: ${service}") {
                            steps {
                                dir(service) {
                                    sh "docker build -t myrepo/${service}:latest ."
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished for services: ${SERVICES_CHANGED.join(', ')}"
        }
    }
}
