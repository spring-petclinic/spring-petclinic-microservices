// Jenkinsfile
pipeline {
    // Agent: Run on any available agent. Requires Docker client installed and configured on the agent.
    agent any

    // Tools: Force agents to use jdk-17
    tools {
        jdk 'jdk-17'
    }

    // Options: Configure pipeline behavior
    options {
        buildDiscarder(logRotator(numToKeepStr: '5')) // Keep only the last 5 build logs
        timestamps() // Prepend timestamps to console output lines
    }

    // Environment: Define variables accessible throughout the pipeline
    environment {
        // === Configuration: Adjust these lists based on your project ===
        // List ALL service directory names expected in the repository
        ALL_SERVICES = "spring-petclinic-admin-server spring-petclinic-api-gateway spring-petclinic-config-server spring-petclinic-customers-service spring-petclinic-discovery-server spring-petclinic-genai-service spring-petclinic-vets-service spring-petclinic-visits-service"

        // List service directories that DO NOT have a standard /src/test or meaningful unit/integration tests
        SERVICES_WITHOUT_TESTS = "spring-petclinic-admin-server spring-petclinic-genai-service" // Tentative list

        // === Docker Configuration ===
        // Your Docker Hub Username (or organization name)
        DOCKERHUB_USERNAME = "22127422" // <--- *** REPLACE THIS ***
        // Jenkins Credentials ID for Docker Hub (created in Jenkins > Manage Jenkins > Credentials)
        DOCKERHUB_CREDENTIALS_ID = "docker-credentials" // <--- *** Use the ID you created ***

        // === Internal Flags & Variables ===
        TESTS_FAILED_FLAG = "false"
    }

    // Stages: Define the main workflow phases
    stages {

        // ============================================================
        // Stage 1: Detect Branch and Changes
        // ============================================================
        stage('Detect Branch and Changes') {
            steps {
                script {
                    echo "Pipeline started for Branch: ${env.BRANCH_NAME}"
                    env.CHANGED_SERVICES = "" // Initialize list of services to process

                    // --- Logic to determine services ---
                    if (env.BRANCH_NAME == 'main') {
                        echo "Running on 'main' branch. Processing ALL services."
                        env.CHANGED_SERVICES = env.ALL_SERVICES
                    } else {
                        echo "Running on feature branch '${env.BRANCH_NAME}'. Detecting changes..."
                        // Use `git diff` comparing current HEAD to the target branch merge base (e.g., origin/main)
                        // This handles multiple commits pushed at once better than HEAD~1
                        // Requires 'main' to be fetched. Add 'git fetch origin main' if needed.
                        // Fallback to HEAD~1 if getting merge base fails (e.g., detached HEAD or very new branch)
                        def changedFilesOutput = ''
                        try {
                           // Ensure main is available for comparison
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
                        def changedFilesList = changedFilesOutput.split('\n').findAll { it } // Split and remove empty lines

                        def services = env.ALL_SERVICES.split(" ")
                        def detectedServiceChanges = []

                        // Check for changes within specific service directories
                        for (service in services) {
                            if (changedFilesList.any { file -> file.startsWith(service + "/") }) {
                                detectedServiceChanges.add(service)
                            }
                        }

                        // Check for changes in common/root files that necessitate building all services
                        def commonFilesChanged = changedFilesList.any { file ->
                            file == 'pom.xml' ||
                            file == 'Jenkinsfile' ||
                            file == 'docker-compose' ||
                            file.startsWith('.mvn/') ||
                            file.startsWith('.github/') ||
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
                    }

                    // --- Final Check and Log ---
                    script {
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
        // Stage 3: Build Services (JARs)
        // ============================================================
        stage('Build Services') {
            when { expression { return env.CHANGED_SERVICES?.trim() } }
            steps {
                script {
                    def serviceList = env.CHANGED_SERVICES.trim().split(" ")
                    def successfullyBuilt = [] // Track services that build successfully
                    def buildFailed = false

                    for (service in serviceList) {
                        echo "--- Preparing to Build Service: ${service} ---"
                        dir(service) {
                            try {
                                echo "Running 'mvn clean package -DskipTests' for ${service}..."
                                sh 'mvn clean package -DskipTests'
                                echo "Build successful for ${service}."
                                successfullyBuilt.add(service) // Add to list for Docker stage

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

                    // Store the list of successfully built services for the next stage
                    env.BUILT_SERVICES = successfullyBuilt.join(" ")
                    echo "[DEBUG] Value of env.BUILT_SERVICES before ending stage: '${env.BUILT_SERVICES}'"

                    if (buildFailed) {
                        echo "Setting build status to UNSTABLE due to build failures in this stage."
                        currentBuild.result = 'UNSTABLE'
                    } else if (env.TESTS_FAILED_FLAG == "true") {
                         echo "Builds successful, but marking UNSTABLE due to earlier test failures."
                         currentBuild.result = 'UNSTABLE'
                    } else {
                         echo "All selected services built successfully."
                    }
                } // End script
            } // End steps
        } // End Stage 3


        // ============================================================
        // Stage 4: Build & Push Docker Images
        // Builds images for successfully built services and pushes them to Docker Hub.
        // ============================================================
        stage('Build & Push Docker Images') {
            // Only run if the BUILT_SERVICES variable is not empty AND build is not FAILED (UNSTABLE is ok)
            when {
                expression { return env.BUILT_SERVICES?.trim() && currentBuild.currentResult != 'FAILURE' }
            }
            steps {
                script {
                    // Ensure Docker Hub username is set
                    if (!env.DOCKERHUB_USERNAME || env.DOCKERHUB_USERNAME == 'your-dockerhub-username') {
                        error "FATAL: DOCKERHUB_USERNAME environment variable is not set or not replaced in Jenkinsfile."
                    }
                    // Ensure Docker Hub credentials ID is set
                    if (!env.DOCKERHUB_CREDENTIALS_ID || env.DOCKERHUB_CREDENTIALS_ID == 'dockerhub-credentials-id') {
                        error "FATAL: DOCKERHUB_CREDENTIALS_ID environment variable is not set or not replaced in Jenkinsfile."
                    }


                    def serviceList = env.BUILT_SERVICES.trim().split(" ")
                    // Get the short commit ID for tagging (requirement: latest commit ID)
                    // Ensure git tool is available or git commands work in the agent environment
                    def commitId = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    if (!commitId) {
                        error "FATAL: Could not retrieve Git commit ID."
                    }
                    echo "Using Commit ID for tagging: ${commitId}"

                    // Use Docker Hub credentials
                    withCredentials([usernamePassword(credentialsId: env.DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        // Login to Docker Hub (optional but recommended for reliability, esp. if using docker sh steps)
                        // sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"

                        for (service in serviceList) {
                            echo "--- Preparing Docker Image for Service: ${service} ---"
                            // Construct the full image name and tag
                            // Replace 'spring-petclinic-' prefix for shorter image name if desired, e.g. 'api-gateway' instead of 'spring-petclinic-api-gateway'
                            // Let's keep the full name for clarity based on directory structure
                            def imageName = "${env.DOCKERHUB_USERNAME}/${service}"
                            def imageTag = commitId // Tag with the short commit ID

                            // Navigate to the service directory containing the Dockerfile
                            dir(service) {
                                try {
                                    echo "Building Docker image: ${imageName}:${imageTag}"
                                    // Build the image using the Dockerfile in the current directory (.)
                                    // The '--build-arg' passes the JAR filename to the Dockerfile. Check your Dockerfiles
                                    // to ensure they expect a JAR_FILE argument (e.g., ARG JAR_FILE=target/*.jar)
                                    // If Dockerfiles just copy 'target/*.jar', this build-arg might be optional,
                                    // but it's safer to be explicit.
                                    def jarFile = sh(script: 'find target -maxdepth 1 -name "*.jar" -print -quit', returnStdout: true).trim()
                                    if (!jarFile) {
                                        error "Could not find JAR file in target/ for ${service}. Cannot build Docker image."
                                    }
                                    // Use Docker Pipeline plugin's build command
                                    def dockerImage = docker.build("${imageName}:${imageTag}", "--build-arg JAR_FILE=${jarFile} .")

                                    echo "Pushing Docker image: ${imageName}:${imageTag}"
                                    // Use Docker Pipeline plugin's push command (implicitly uses credentials bound by withCredentials)
                                    dockerImage.push()
                                    // Alternative push with specific tag if needed: dockerImage.push("${imageTag}")

                                    // Also push a 'latest' tag if building from the 'main' branch
                                    if (env.BRANCH_NAME == 'main') {
                                        echo "Pushing additional tag 'latest' for main branch build: ${imageName}:latest"
                                        dockerImage.push('latest')
                                    }
                                     // Also push a 'BRANCH_NAME' tag for non-main branch build
                                    else if (env.BRANCH_NAME) {
                                        // Sanitize branch name for Docker tag (replace / with -)
                                        def branchTag = env.BRANCH_NAME.replaceAll('/','-')
                                        echo "Pushing additional tag for branch name: ${imageName}:${branchTag}"
                                        dockerImage.push("${branchTag}")
                                    }


                                    echo "Docker build and push successful for ${service}."

                                } catch (err) {
                                    echo "ERROR: Docker build or push FAILED for ${service}: ${err.getMessage()}"
                                    // Mark build as unstable if not already failed
                                    if (currentBuild.currentResult != 'FAILURE') {
                                        currentBuild.result = 'UNSTABLE'
                                    }
                                    // Decide if you want to stop the whole process on a single docker failure
                                    // error "Stopping build due to Docker failure for ${service}"
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
