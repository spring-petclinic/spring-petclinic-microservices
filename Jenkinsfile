pipeline {
    agent any
    environment {
        SERVICES_CHANGED = ""
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    // Get the list of changed files compared to main branch
                    def changes = sh(script: "git diff --name-only origin/main", returnStdout: true).trim().split("\n")

                    // Define microservices directories
                    def services = [
                        "spring-petclinic-customers-service",
                        "spring-petclinic-vets-service",
                        "spring-petclinic-visits-service",
                        "spring-petclinic-api-gateway",
                        "spring-petclinic-config-server",
                        "spring-petclinic-admin-server",
                        "spring-petclinic-genai-service"
                    ]

                    // Identify changed services
                    def changedServices = services.findAll { service ->
                        changes.any { it.startsWith(service + "/") }
                    }

                    // If no relevant changes, abort pipeline
                    if (changedServices.isEmpty()) {
                        error("No relevant changes detected, skipping pipeline")
                    }

                    // Convert list to a comma-separated string for compatibility
                    SERVICES_CHANGED = changedServices.join(',')
                    echo "Services changed: ${SERVICES_CHANGED}"
                }
            }
        }

        stage('Test & Coverage Check') {
            steps {
                script {
                    def parallelStages = [:]
                    SERVICES_CHANGED.tokenize(',').each { service ->
                        parallelStages["Test & Coverage: ${service}"] = {
                            stage("Test & Coverage: ${service}") { // Ensure each service has a proper stage
                                steps {
                                    dir(service) {
                                        sh './mvnw test'

                                        // Validate test coverage
                                        script {
                                            def coverage = sh(script: '''
                                                grep -Po '(?<=<counter type="LINE" missed="\\d+" covered=")\\d+(?="/>)' target/site/jacoco/jacoco.xml |
                                                awk '{sum += $1} END {print sum}'
                                            ''', returnStdout: true).trim()

                                            if (coverage.toInteger() < 70) {
                                                error("Test coverage for ${service} is below 70%")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!parallelStages.isEmpty()) {
                        parallel parallelStages
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    def parallelBuilds = [:]
                    SERVICES_CHANGED.tokenize(',').each { service ->
                        parallelBuilds["Build: ${service}"] = {
                            stage("Build: ${service}") {
                                steps {
                                    dir(service) {
                                        sh './mvnw package -DskipTests'
                                    }
                                }
                            }
                        }
                    }
                    if (!parallelBuilds.isEmpty()) {
                        parallel parallelBuilds
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    def parallelDockerBuilds = [:]
                    SERVICES_CHANGED.tokenize(',').each { service ->
                        parallelDockerBuilds["Docker Build: ${service}"] = {
                            stage("Docker Build: ${service}") { // Ensure each service has a proper stage
                                steps {
                                    dir(service) {
                                        sh "docker build --no-cache -t myrepo/${service}:latest ." // Added --no-cache for clean builds
                                    }
                                }
                            }
                        }
                    }
                    if (!parallelDockerBuilds.isEmpty()) {
                        parallel parallelDockerBuilds
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished for services: ${SERVICES_CHANGED}"
        }
    }
}
