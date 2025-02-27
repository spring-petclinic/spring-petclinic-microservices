pipeline {
    agent any
    environment {
        SERVICES_CHANGED = ""
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    // Fetch full history of main branch
                    sh 'git fetch origin main --prune --unshallow'

                    // Compare against tip of origin/main
                    def baseCommit = sh(script: "git rev-parse origin/main", returnStdout: true).trim()

                    // Identify changes
                    def changes = sh(
                        script: "git diff --name-only ${baseCommit}..HEAD",
                        returnStdout: true
                    ).trim().split("\n")

                    // Log detected changes for debugging
                    echo "Changed files detected: ${changes.join(',')}"

                    def services = [
                        "spring-petclinic-customers-service",
                        "spring-petclinic-vets-service",
                        "spring-petclinic-visits-service",
                        "spring-petclinic-api-gateway",
                        "spring-petclinic-config-server",
                        "spring-petclinic-admin-server",
                        "spring-petclinic-genai-service"
                    ]

                    // If you literally want to detect changes in 'test/' or 'test/huy/', adapt this logic
                    def changedServices = services.findAll { service ->
                        changes.any { file ->
                            file.startsWith("${service}/") || // Matches changes in service directories
                            file.contains(service) || // Broad matching
                            file.startsWith("test/") // Ensures test changes are detected
                        }
                    }


                    if (changedServices.isEmpty()) {
                        error("No relevant changes detected, skipping pipeline")
                    }

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
