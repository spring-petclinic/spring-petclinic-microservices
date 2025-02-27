pipeline {
    agent any
    environment {
        SERVICES_CHANGED = ""
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    // Fetch latest changes from main branch
                    sh 'git fetch origin main --depth=1'

                    // Get the common ancestor commit of main and the current branch
                    def baseCommit = sh(script: "git merge-base origin/main HEAD", returnStdout: true).trim()

                    // Get the list of changed files compared to baseCommit
                    def changes = sh(script: "git diff --name-only ${baseCommit} HEAD", returnStdout: true).trim().split("\n")

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

                    // Identify changed services, including test changes
                    def changedServices = services.findAll { service ->
                        changes.any { file -> file.startsWith(service + "/") || file.contains(service) || file.startsWith("test/") }
                    }

                    // Ensure at least one service is detected as changed
                    if (changedServices.isEmpty()) {
                        error("No relevant changes detected, skipping pipeline")
                    }

                    // Convert list to a comma-separated string
                    env.SERVICES_CHANGED = changedServices.join(',')
                    echo "Services changed: ${env.SERVICES_CHANGED}"
                }
            }
        }


        stage('Test & Coverage Check') {
            when {
                expression { env.SERVICES_CHANGED?.trim() }
            }
            steps {
                script {
                    def parallelStages = [:]
                    env.SERVICES_CHANGED.tokenize(',').each { service ->
                        parallelStages["Test & Coverage: ${service}"] = {
                            dir(service) {
                                sh './mvnw test'

                                // Ensure jacoco.xml exists before attempting coverage check
                                if (fileExists("target/site/jacoco/jacoco.xml")) {
                                    def coverage = sh(script: '''
                                        grep -Po '(?<=<counter type="LINE" missed="\\d+" covered=")\\d+(?="/>)' target/site/jacoco/jacoco.xml |
                                        awk '{sum += $1} END {print sum}'
                                    ''', returnStdout: true).trim()

                                    if (coverage.isNumber() && coverage.toInteger() < 70) {
                                        error("Test coverage for ${service} is below 70%")
                                    }
                                } else {
                                    echo "Coverage file not found for ${service}, skipping coverage check"
                                }
                            }
                        }
                    }
                    parallel parallelStages
                }
            }
        }

        stage('Build') {
            when {
                expression { env.SERVICES_CHANGED?.trim() }
            }
            steps {
                script {
                    def parallelBuilds = [:]
                    env.SERVICES_CHANGED.tokenize(',').each { service ->
                        parallelBuilds["Build: ${service}"] = {
                            dir(service) {
                                sh './mvnw package -DskipTests'
                            }
                        }
                    }
                    parallel parallelBuilds
                }
            }
        }

        stage('Docker Build') {
            when {
                expression { env.SERVICES_CHANGED?.trim() }
            }
            steps {
                script {
                    def parallelDockerBuilds = [:]
                    env.SERVICES_CHANGED.tokenize(',').each { service ->
                        parallelDockerBuilds["Docker Build: ${service}"] = {
                            dir(service) {
                                sh "docker build --no-cache -t myrepo/${service}:latest ."
                            }
                        }
                    }
                    parallel parallelDockerBuilds
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished for services: ${env.SERVICES_CHANGED}"
        }
    }
}
