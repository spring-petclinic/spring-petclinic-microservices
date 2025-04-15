// Jenkinsfile
pipeline {
    agent any
    tools { jdk 'jdk-17' }
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timestamps()
    }
    environment {
        ALL_SERVICES = "spring-petclinic-admin-server spring-petclinic-api-gateway spring-petclinic-config-server spring-petclinic-customers-service spring-petclinic-discovery-server spring-petclinic-genai-service spring-petclinic-vets-service spring-petclinic-visits-service"
        SERVICES_WITHOUT_TESTS = "spring-petclinic-admin-server spring-petclinic-genai-service"
        DOCKERHUB_USERNAME = "22127422" // <--- *** REPLACE THIS ***
        DOCKERHUB_CREDENTIALS_ID = "docker-credentials" // <--- *** Use the ID you created ***
        TESTS_FAILED_FLAG = "false"
        // BUILT_SERVICES will be populated directly in Stage 3 now
        BUILT_SERVICES = ""
    }

    stages {

        // ============================================================
        // Stage 1: Detect Branch and Changes
        // ============================================================
        stage('Detect Branch and Changes') {
            steps {
                script {
                    echo "Pipeline started for Branch: ${env.BRANCH_NAME}"
                    env.CHANGED_SERVICES = "" // Initialize list of services to process

                    if (env.BRANCH_NAME == 'main') {
                        echo "Running on 'main' branch. Processing ALL services."
                        env.CHANGED_SERVICES = env.ALL_SERVICES
                    } else {
                        echo "Running on feature branch '${env.BRANCH_NAME}'. Detecting changes..."
                        def changedFilesOutput = ''
                        try {
                           sh 'git fetch origin main:refs/remotes/origin/main'
                           changedFilesOutput = sh(script: "git diff --name-only origin/main...HEAD || git diff --name-only HEAD~1 HEAD || exit 0", returnStdout: true).trim()
                        } catch (e) {
                           echo "Warning: Could not compare against origin/main, falling back to HEAD~1 comparison. Error: ${e.message}"
                           changedFilesOutput = sh(script: "git diff --name-only HEAD~1 HEAD || exit 0", returnStdout: true).trim()
                        }

                        if (changedFilesOutput.isEmpty()) {
                            echo "No files found changed compared to origin/main or HEAD~1."
                        } else {
                           echo "Changed files detected:\n${changedFilesOutput}"
                        }
                        def changedFilesList = changedFilesOutput.split('\n').findAll { it }

                        def services = env.ALL_SERVICES.split(" ")
                        def detectedServiceChanges = []

                        for (service in services) {
                            if (changedFilesList.any { file -> file.startsWith(service + "/") }) {
                                detectedServiceChanges.add(service)
                            }
                        }

                        def commonFilesChanged = changedFilesList.any { file ->
                            file == 'pom.xml' || file == 'Jenkinsfile' || file == 'docker-compose' ||
                            file.startsWith('.mvn/') || file.startsWith('.github/') ||
                            file == 'mvnw' || file == 'mvnw.cmd'
                        }

                        if (commonFilesChanged) {
                            echo "Common file(s) changed. Processing ALL services."
                            env.CHANGED_SERVICES = env.ALL_SERVICES
                        } else if (!detectedServiceChanges.isEmpty()) {
                            echo "Changes detected in specific services."
                            env.CHANGED_SERVICES = detectedServiceChanges.join(" ")
                        } else {
                            echo "No relevant service or common file changes detected."
                            env.CHANGED_SERVICES = ""
                        }
                    } // End script

                    script { // Separate script block just for logging
                        if (env.CHANGED_SERVICES?.trim()) {
                            echo "Services to process in subsequent stages: ${env.CHANGED_SERVICES}"
                        } else {
                            echo "No services require processing. Subsequent stages will be skipped."
                        }
                    }
                }
            }
        } // End Stage 1

        // ============================================================
        // Stage 2: Test Services
        // ============================================================
        stage('Test Services') {
            when { expression { return env.CHANGED_SERVICES?.trim() } }
            steps {
                script {
                    def serviceList = env.CHANGED_SERVICES.trim().split(" ")
                    def jacocoExecFiles = []
                    def jacocoClassDirs = []
                    def jacocoSrcDirs = []
                    env.TESTS_FAILED_FLAG = "false"

                    for (service in serviceList) {
                        echo "--- Preparing to Test Service: ${service} ---"
                        if (env.SERVICES_WITHOUT_TESTS.contains(service)) {
                            echo "Skipping tests for ${service} (marked as having no tests)."
                            continue
                        }

                        dir(service) {
                            try {
                                echo "Running 'mvn clean test' for ${service}..."
                                sh 'mvn clean test'
                                echo "Tests completed for ${service}."

                                if (fileExists('target/jacoco.exec')) {
                                    echo "Found jacoco.exec for ${service}. Adding to aggregation list."
                                    jacocoExecFiles.add("${service}/target/jacoco.exec")
                                    jacocoClassDirs.add("${service}/target/classes")
                                    jacocoSrcDirs.add("${service}/src/main/java")
                                } else {
                                    echo "WARNING: jacoco.exec not found for ${service} after tests ran."
                                }

                            } catch (err) {
                                echo "ERROR: Tests FAILED for ${service}. Marking build as UNSTABLE."
                                env.TESTS_FAILED_FLAG = "true"
                            } finally {
                                echo "Publishing JUnit results for ${service}..."
                                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                            }
                        }
                    }

                    if (!jacocoExecFiles.isEmpty()) {
                        echo "--- Generating Aggregated JaCoCo Coverage Report ---"
                        echo "Aggregating JaCoCo data from ${jacocoExecFiles.size()} service(s)."
                        try {
                            jacoco(
                                execPattern: jacocoExecFiles.join(','),
                                classPattern: jacocoClassDirs.join(','),
                                sourcePattern: jacocoSrcDirs.join(','),
                                inclusionPattern: '**/*.class',
                                exclusionPattern: '**/test/**,**/model/**,**/domain/**,**/entity/**',
                                skipCopyOfSrcFiles: true
                            )
                            echo "JaCoCo aggregated report generated successfully."
                        } catch (err) {
                            echo "ERROR: Failed to generate JaCoCo aggregated report: ${err.getMessage()}"
                            currentBuild.result = 'UNSTABLE'
                        }
                    } else {
                        echo "No JaCoCo execution data found to aggregate."
                    }

                    if (env.TESTS_FAILED_FLAG == "true") {
                        echo "Setting build status to UNSTABLE due to test failures."
                        currentBuild.result = 'UNSTABLE'
                    } else {
                        echo "All tests passed or were skipped."
                    }

                } // End script
            } // End steps
        } // End Stage 2

        // ============================================================
        // Stage 3: Build Services (JARs) - REVISED LOGIC
        // ============================================================
        stage('Build Services') {
            when { expression { return env.CHANGED_SERVICES?.trim() } }
            steps {
                script {
                    def serviceList = env.CHANGED_SERVICES.trim().split(" ")
                    // Initialize BUILT_SERVICES as empty string at the start of the stage
                    env.BUILT_SERVICES = ""
                    def buildFailed = false // Track if any build in this stage fails

                    for (service in serviceList) {
                        echo "--- Preparing to Build Service: ${service} ---"
                        dir(service) {
                            try {
                                echo "Running 'mvn clean package -DskipTests' for ${service}..."
                                sh 'mvn clean package -DskipTests'
                                echo "Build successful for ${service}."

                                // *** MODIFIED: Append directly to env var string on success ***
                                if (env.BUILT_SERVICES == "") {
                                    // First successful build
                                    env.BUILT_SERVICES = service
                                } else {
                                    // Subsequent successful builds, add with a space separator
                                    env.BUILT_SERVICES = "${env.BUILT_SERVICES} ${service}"
                                }
                                // *** END MODIFICATION ***

                                def artifactPath = sh(script: 'find target -maxdepth 1 -name "*.jar" -print -quit', returnStdout: true).trim()
                                if (artifactPath) {
                                    echo "Archiving artifact: ${artifactPath}"
                                    archiveArtifacts artifacts: artifactPath, fingerprint: true
                                } else {
                                    echo "WARNING: Could not find JAR artifact for ${service} in target/ directory after build."
                                }
                            } catch (err) {
                                echo "ERROR: Build FAILED for ${service}."
                                buildFailed = true
                            }
                        } // End dir(service)
                    } // End for loop

                    // *** Keep DEBUG LOGGING to verify the final string ***
                    echo "[DEBUG] Value of env.BUILT_SERVICES at end of Stage 3: '${env.BUILT_SERVICES}'"

                    if (buildFailed) {
                        echo "Setting build status to UNSTABLE due to build failures in this stage."
                        currentBuild.result = 'UNSTABLE'
                    } else if (env.TESTS_FAILED_FLAG == "true") {
                         echo "Builds successful, but marking UNSTABLE due to earlier test failures."
                         currentBuild.result = 'UNSTABLE'
                    } else if (env.BUILT_SERVICES?.trim()) { // Check if *any* service was built successfully
                         echo "Some or all selected services built successfully."
                    } else {
                         // This case should now only happen if CHANGED_SERVICES was not empty, but *all* builds failed.
                         echo "No services were built successfully in this stage."
                         // Optional: Mark as unstable if no services built, even if no explicit error was caught above?
                         // currentBuild.result = 'UNSTABLE'
                    }
                } // End script
            } // End steps
        } // End Stage 3

        // ============================================================
        // Stage 4: Build & Push Docker Images
        // ============================================================
        stage('Build & Push Docker Images') {
            // Condition remains the same: Run if BUILT_SERVICES is not empty AND build didn't hard fail
            when {
                expression { return env.BUILT_SERVICES?.trim() && currentBuild.currentResult != 'FAILURE' }
            }
            steps {
                script {
                    if (!env.DOCKERHUB_USERNAME || env.DOCKERHUB_USERNAME == 'your-dockerhub-username') {
                        error "FATAL: DOCKERHUB_USERNAME environment variable is not set or not replaced in Jenkinsfile."
                    }
                    if (!env.DOCKERHUB_CREDENTIALS_ID || env.DOCKERHUB_CREDENTIALS_ID == 'dockerhub-credentials') {
                        error "FATAL: DOCKERHUB_CREDENTIALS_ID environment variable is not set or not replaced in Jenkinsfile."
                    }

                    // Now BUILT_SERVICES should be a space-separated string
                    def serviceList = env.BUILT_SERVICES.trim().split(" ")
                    def commitId = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    if (!commitId) {
                        error "FATAL: Could not retrieve Git commit ID."
                    }
                    echo "Using Commit ID for tagging: ${commitId}"

                    withCredentials([usernamePassword(credentialsId: env.DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        for (service in serviceList) {
                            echo "--- Preparing Docker Image for Service: ${service} ---"
                            def imageName = "${env.DOCKERHUB_USERNAME}/${service}"
                            def imageTag = commitId

                            dir(service) {
                                try {
                                    echo "Building Docker image: ${imageName}:${imageTag}"
                                    def jarFile = sh(script: 'find target -maxdepth 1 -name "*.jar" -print -quit', returnStdout: true).trim()
                                    if (!jarFile) {
                                        error "Could not find JAR file in target/ for ${service}. Cannot build Docker image."
                                    }
                                    def dockerImage = docker.build("${imageName}:${imageTag}", "--build-arg JAR_FILE=${jarFile} .")

                                    echo "Pushing Docker image: ${imageName}:${imageTag}"
                                    dockerImage.push()

                                    if (env.BRANCH_NAME == 'main') {
                                        echo "Pushing additional tag 'latest' for main branch build: ${imageName}:latest"
                                        dockerImage.push('latest')
                                    } else if (env.BRANCH_NAME) {
                                        def branchTag = env.BRANCH_NAME.replaceAll('/','-')
                                        echo "Pushing additional tag for branch name: ${imageName}:${branchTag}"
                                        dockerImage.push("${branchTag}")
                                    }

                                    echo "Docker build and push successful for ${service}."

                                } catch (err) {
                                    echo "ERROR: Docker build or push FAILED for ${service}: ${err.getMessage()}"
                                    if (currentBuild.currentResult != 'FAILURE') {
                                        currentBuild.result = 'UNSTABLE'
                                    }
                                }
                            } // End dir(service)
                        } // End for loop
                    } // End withCredentials
                } // End script
            } // End steps
        } // End Stage 4

    } // End stages

    // ============================================================
    // Post Actions
    // ============================================================
    post {
        always {
            echo "Pipeline finished with final status: ${currentBuild.currentResult}"
            cleanWs()
        }
        success {
            echo "Build was successful!"
        }
        unstable {
            echo "Build is UNSTABLE. Check logs for test failures, build issues, or Docker push problems."
        }
        failure {
            echo "Build FAILED. Check logs for critical errors."
        }
    } // End post

} // End pipeline
