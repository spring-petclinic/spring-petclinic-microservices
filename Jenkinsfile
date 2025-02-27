def SERVICES_CHANGED = ""

pipeline {
    agent { 
        node {
            label 'docker-agent-petclinic'
            }
      }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    echo "🔍 Checking if the repository is shallow..."
                    def isShallow = sh(script: "git rev-parse --is-shallow-repository", returnStdout: true).trim()
                    echo "⏳ Is repository shallow? ${isShallow}"

                    // Ensure the full git history is available for accurate change detection
                    if (isShallow == "true") {
                        echo "📂 Repository is shallow. Fetching full history..."
                        sh 'git fetch origin main --prune --unshallow'
                    } else {
                        echo "✅ Repository is already complete. Skipping --unshallow."
                        sh 'git fetch origin main --prune'
                    }

                    // Determine the base commit to compare against
                    def baseCommit = sh(script: "git merge-base origin/main HEAD", returnStdout: true).trim()
                    echo "🔍 Base commit: ${baseCommit}"

                    // Ensure base commit is valid
                    if (!baseCommit) {
                        error("❌ Base commit not found! Ensure 'git merge-base origin/main HEAD' returns a valid commit.")
                    }

                    // Get the list of changed files relative to the base commit
                    def changes = sh(script: "git diff --name-only ${baseCommit} HEAD", returnStdout: true).trim()

                    echo "📜 Raw changed files:\n${changes}"

                    // Ensure changes are not empty
                    if (!changes) {
                        error("❌ No changed files detected! Ensure 'git diff --name-only ${baseCommit} HEAD' provides valid output.")
                    }

                    // Convert the list into an array
                    def changedFiles = changes.split("\n")

                    // Normalize paths to ensure they match expected service directories
                    def normalizedChanges = changedFiles.collect { file ->
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

                    // Identify which services have changes
                    def changedServices = services.findAll { service ->
                        normalizedChanges.any { file ->
                            file.startsWith("${service}/") || file.contains("${service}/")
                        }
                    }

                    echo "📢 Final changed services list: ${changedServices.join(', ')}"

                    // Ensure we have at least one changed service
                    if (changedServices.isEmpty()) {
                        error("❌ No relevant services detected. Verify file path matching logic.")
                    }

                    // Use properties() to persist the value
                    properties([
                        parameters([
                            string(name: 'SERVICES_CHANGED', defaultValue: changedServices.join(','), description: 'Services that changed in this build')
                        ])
                    ])

                    SERVICES_CHANGED = changedServices.join(',')
                    echo "🚀 Services changed (Global ENV): ${SERVICES_CHANGED}"
                }
            }
        }


        stage('Test & Coverage Check') {
            when {
                expression { SERVICES_CHANGED?.trim() != "" }
            }
            steps {
                script {
                    def parallelStages = [:]
                    def servicesList = SERVICES_CHANGED.tokenize(',')

                    if (servicesList.isEmpty()) {
                        error("❌ No changed services found. Verify 'Detect Changes' stage.")
                    }

                    servicesList.each { service ->
                        parallelStages["Test & Coverage: ${service}"] = {
                            dir(service) {
                                sh '../mvnw test'

                                // Validate if JaCoCo coverage report exists
                                if (fileExists("target/site/jacoco/jacoco.xml")) {
                                    def coverage = sh(script: '''
                                        grep -Po '(?<=<counter type="LINE" missed="\\d+" covered=")\\d+(?="/>)' target/site/jacoco/jacoco.xml |
                                        awk '{sum += $1} END {print sum}'
                                    ''', returnStdout: true).trim()

                                    if (coverage.isNumber() && coverage.toInteger() < 70) {
                                        error("Test coverage for ${service} is below 70% threshold.")
                                    }
                                } else {
                                    echo "⚠️ Coverage file not found for ${service}, skipping coverage validation."
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
                expression { SERVICES_CHANGED?.trim() != "" }
            }
            steps {
                script {
                    def parallelBuilds = [:]
                    def servicesList = SERVICES_CHANGED.tokenize(',')

                    if (servicesList.isEmpty()) {
                        error("❌ No changed services found. Verify 'Detect Changes' stage.")
                    }

                    servicesList.each { service ->
                        parallelBuilds["Build: ${service}"] = {
                            dir(service) {
                                sh '../mvnw package -DskipTests'
                            }
                        }
                    }
                    parallel parallelBuilds
                }
            }
        }

//         stage('Docker Build') {
//             when {
//                 expression { SERVICES_CHANGED?.trim() != "" }
//             }
//             steps {
//                 script {
//                     def parallelDockerBuilds = [:]
//                     def servicesList = SERVICES_CHANGED.tokenize(',')
//
//                     if (servicesList.isEmpty()) {
//                         error("❌ No changed services found. Verify 'Detect Changes' stage.")
//                     }
//
//                     servicesList.each { service ->
//                         parallelDockerBuilds["Docker Build: ${service}"] = {
//                             dir(service) {
//                                 sh "docker build --no-cache -t myrepo/${service}:latest ."
//                             }
//                         }
//                     }
//                     parallel parallelDockerBuilds
//                 }
//             }
//         }
    }

    post {
        always {
            echo "✅ Pipeline execution completed for services: ${SERVICES_CHANGED}"
        }
    }
}
