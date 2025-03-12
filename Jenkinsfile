def SERVICES_CHANGED = ""


pipeline {
    agent {
        node {
            label 'docker-agent-petclinic'
        }
    }

    environment {
            GITHUB_TOKEN = credentials('github-token')
        }

    stages {
        stage('Detect Changes') {
            steps {
                script {
//                     githubNotify context: 'Detect Changes', status: 'PENDINGG'

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
//                         githubNotify context: 'Detect Changes', status: 'FAILURE'
                        error("❌ No relevant services detected. Verify file path matching logic.")
                    }
                    else {
//                         githubNotify context: 'Detect Changes', status: 'SUCCESS'
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
                    def servicesList = SERVICES_CHANGED.tokenize(',')

                    if (servicesList.isEmpty()) {
                        error("❌ No changed services found.")
                    }

                    // Run tests sequentially instead of in parallel
                    for (service in servicesList) {
                        echo "🔬 Running tests for ${service}..."
                        withEnv(["MAVEN_USER_HOME=${env.WORKSPACE}/m2-wrapper-${service}"]) {
                            dir(service) {
                                sh '../mvnw clean verify -PbuildDocker'

                                def jacocoFile = sh(script: "find target -name jacoco.xml", returnStdout: true).trim()
                                if (!jacocoFile) {
                                    echo "⚠️ No JaCoCo report found for ${service}."
                                } else {
                                    def missed = sh(script: """
                                        awk -F 'missed="' '/<counter type="LINE"/ {gsub(/".*/, "", \$2); sum += \$2} END {print sum}' ${jacocoFile}
                                    """, returnStdout: true).trim()

                                    def covered = sh(script: """
                                        awk -F 'covered="' '/<counter type="LINE"/ {gsub(/".*/, "", \$2); sum += \$2} END {print sum}' ${jacocoFile}
                                    """, returnStdout: true).trim()

                                    def total = missed.toInteger() + covered.toInteger()
                                    def coveragePercent = (total > 0) ? (covered.toInteger() * 100 / total) : 0

                                    echo "🚀 Test coverage for ${service}: ${coveragePercent}%"

                                    if (coveragePercent < 70) {
                                        error("❌ Test coverage below 70% for ${service}.")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Build') {
            when {
                expression { SERVICES_CHANGED?.trim() != "" }
            }
            steps {
                script {
                    def servicesList = SERVICES_CHANGED.tokenize(',')

                    if (servicesList.isEmpty()) {
                        error("❌ No changed services found.")
                    }

                    for (service in servicesList) {
                        echo "🏗️ Building ${service}..."
                        dir(service) {
                            sh '../mvnw package -DskipTests -T 1C'
                        }
                    }
                }
            }
        }


//         stage('Test & Coverage Check') {
//             when {
//                 expression { SERVICES_CHANGED?.trim() != "" }
//             }
//             steps {
//                 script {
// //                     githubNotify context: 'Test & Coverage', status: 'PENDING'
//
//                     def parallelStages = [:]
//                     def servicesList = SERVICES_CHANGED.tokenize(',')
//
//                     if (servicesList.isEmpty()) {
//                         error("❌ No changed services found. Verify 'Detect Changes' stage.")
//                     }
//
//                     servicesList.each { service ->
//                         parallelStages["Test & Coverage: ${service}"] = {
//                             withEnv(["MAVEN_USER_HOME=${env.WORKSPACE}/m2-wrapper-${service}"]) {
//                                 dir(service) {
//     //                                 sh 'rm -rf ~/.m2/wrapper/dists/apache-maven* || true'
//
//     //                                 sh '../mvnw dependency:purge-local-repository -DskipTests'
//
//                                     sh '../mvnw clean verify -PbuildDocker'
//
//                                     sh 'pwd && ls -lah target/site/jacoco'
//
//                                     // Find JaCoCo file
//                                     def jacocoFile = sh(script: "find target -name jacoco.xml", returnStdout: true).trim()
//
//                                     if (!jacocoFile) {
//                                         echo "⚠️ JaCoCo report not found. Skipping coverage validation."
//                                     } else {
//                                         echo "✅ Found JaCoCo report: ${jacocoFile}"
//
//                                         def missed = sh(script: """
//                                             awk -F 'missed="' '/<counter type="LINE"/ {gsub(/".*/, "", \$2); sum += \$2} END {print sum}' ${jacocoFile}
//                                         """, returnStdout: true).trim()
//
//                                         def covered = sh(script: """
//                                             awk -F 'covered="' '/<counter type="LINE"/ {gsub(/".*/, "", \$2); sum += \$2} END {print sum}' ${jacocoFile}
//                                         """, returnStdout: true).trim()
//
//                                         if (!missed.isNumber() || !covered.isNumber()) {
//                                             error("❌ Could not extract JaCoCo coverage data from ${jacocoFile}")
//                                         }
//
//                                         def total = missed.toInteger() + covered.toInteger()
//                                         def coveragePercent = (total > 0) ? (covered.toInteger() * 100 / total) : 0
//
//                                         echo "🚀 Test coverage for ${service} is ${coveragePercent}%"
//
//                                         if (coveragePercent < 70) {
//     //                                         githubNotify context: "Test & Coverage: ${service}", status: 'FAILURE'
//                                             error("❌ Test coverage for ${service} is below 70% threshold.")
//                                         }
//                                         else {
//     //                                         githubNotify context: "Test & Coverage: ${service}", status: 'SUCCESS'
//                                         }
//                                     }
//                                 }
//                             }
//                         }
//                     }
//                     parallel parallelStages
// //                     githubNotify context: 'Test & Coverage', status: 'SUCCESS'
//                 }
//             }
//         }


//         stage('Build') {
//             when {
//                 expression { SERVICES_CHANGED?.trim() != "" }
//             }
//             steps {
//                 script {
// //                     githubNotify context: 'Build', status: 'PENDING'
//
//                     def parallelBuilds = [:]
//                     def servicesList = SERVICES_CHANGED.tokenize(',')
//
//                     if (servicesList.isEmpty()) {
//                         error("❌ No changed services found. Verify 'Detect Changes' stage.")
//                     }
//
//                     servicesList.each { service ->
//                         parallelBuilds["Build: ${service}"] = {
//                             dir(service) {
//                                 sh '../mvnw package -DskipTests -T 1C'
//                             }
//                         }
//                     }
//                     parallel parallelBuilds
// //                     githubNotify context: 'Build', status: 'SUCCESS'
//                 }
//             }
//         }

        //
        // stage('Docker Build') {
        //     when {
        //         expression { SERVICES_CHANGED?.trim() != "" }
        //     }
        //     steps {
        //         script {
        //             def parallelDockerBuilds = [:]
        //             def servicesList = SERVICES_CHANGED.tokenize(',')

        //             if (servicesList.isEmpty()) {
        //                 error("❌ No changed services found. Verify 'Detect Changes' stage.")
        //             }

        //             servicesList.each { service ->
        //                 parallelDockerBuilds["Docker Build: ${service}"] = {
        //                     dir(service) {
        //                         sh "docker build --no-cache -t hzeroxium/${service}:latest ."
        //                     }
        //                 }
        //             }
        //             parallel parallelDockerBuilds
        //         }
        //     }
        // }
    }

    post {
        failure {
            script {
                   echo "❌ CI/CD Pipeline failed!"
//                 githubNotify context: 'CI/CD Pipeline', status: 'FAILURE'
            }
        }
        success {
            script {
                   echo "✅ CI/CD Pipeline succeeded!"
//                 githubNotify context: 'CI/CD Pipeline', status: 'SUCCESS'
            }
        }
        always {
            echo "✅ Pipeline execution completed for services: ${SERVICES_CHANGED}"
        }
    }
}

