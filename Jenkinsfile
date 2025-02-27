pipeline {
    agent any
    environment {
        SERVICES_CHANGED = ""
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    // Check if the repository is shallow before running unshallow
                    echo "🔍 Checking if the repository is shallow..."
                    def isShallow = sh(script: "git rev-parse --is-shallow-repository", returnStdout: true).trim()
                    echo "⏳ Is repository shallow? ${isShallow}"

                    if (isShallow == "true") {
                        echo "📂 Repository is shallow. Fetching full history..."
                        sh 'git fetch origin main --prune --unshallow'
                    } else {
                        echo "✅ Repository is already complete. Skipping --unshallow."
                        sh 'git fetch origin main --prune'
                    }

                    // Get the actual common ancestor commit
                    def baseCommit = sh(script: "git merge-base origin/main HEAD", returnStdout: true).trim()
                    echo "🔍 Base commit: ${baseCommit}"

                    if (!baseCommit) {
                        error("❌ Base commit not found! Check if 'git merge-base origin/main HEAD' returns a valid commit.")
                    }

                    // Get the list of changed files compared to baseCommit
                    def changes = sh(script: "git diff --name-only ${baseCommit} HEAD", returnStdout: true).trim().split("\n")

                    // Log detected changes for debugging
                    echo "📜 Raw changed files:\n${changes}"

                    if (!changes) {
                        error("❌ No changed files detected! Check if 'git diff --name-only ${baseCommit} HEAD' returns valid output.")
                    }

                    // Normalize paths to handle absolute vs relative path mismatches
                    def normalizedChanges = changes.collect { file ->
                        file.replaceFirst("^.*?/spring-petclinic-microservices/", "")
                    }

                    echo "✅ Normalized changed files: ${normalizedChanges.join(', ')}"

                    def services = [
                        "spring-petclinic-customers-service",
                        "spring-petclinic-vets-service",
                        "spring-petclinic-visits-service",
                        "spring-petclinic-api-gateway",
                        "spring-petclinic-config-server",
                        "spring-petclinic-admin-server",
                        "spring-petclinic-genai-service"
                    ]

                    services.each { service ->
                        def matchedFiles = normalizedChanges.findAll { file ->
                            return file.startsWith("${service}/") || file.contains("${service}/") || file.matches(".*${service}.*")
                        }

                        if (!matchedFiles.isEmpty()) {
                            echo "✅ Service '${service}' detected changes in files: ${matchedFiles.join(', ')}"
                        }
                    }


                    // Identify which services changed
                    def changedServices = services.findAll { service ->
                        normalizedChanges.any { file ->
                            return file.startsWith("${service}/") || file.contains("${service}/") || file.matches(".*${service}.*")
                        }
                    }

                    echo "📢 Final changed services list: ${changedServices.join(', ')}"

                    if (changedServices == null || changedServices.isEmpty()) {
                        error("❌ No relevant services detected. Check if the paths in 'normalizedChanges' match the expected service names.")
                    } else {
                        echo "✅ Assigning changed services to environment variable..."
                        def servicesList = changedServices.join(',')
                        echo "🔧 Setting env.SERVICES_CHANGED = '${servicesList}'"
                        env.SERVICES_CHANGED = servicesList
                    }

                    echo "🚀 Services changed: ${env.SERVICES_CHANGED}"
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
