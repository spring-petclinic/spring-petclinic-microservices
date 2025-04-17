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
        // Common Dockerfile path relative to workspace root
        DOCKERFILE_PATH = "docker/Dockerfile"
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
        // Stage 3: Build, Package & Push Docker Images (Correct Build Arg Value)
        // ============================================================
        stage('Build, Package & Push Docker Images') {
            when {
                expression { return env.CHANGED_SERVICES?.trim() && currentBuild.currentResult != 'FAILURE' }
            }
            steps {
                script {
                    if (!env.DOCKERHUB_USERNAME || env.DOCKERHUB_USERNAME == 'your-dockerhub-username') {
                        error "FATAL: DOCKERHUB_USERNAME environment variable is not set or not replaced in Jenkinsfile."
                    }
                    if (!env.DOCKERHUB_CREDENTIALS_ID || env.DOCKERHUB_CREDENTIALS_ID == 'dockerhub-credentials') {
                        error "FATAL: DOCKERHUB_CREDENTIALS_ID environment variable is not set or not replaced in Jenkinsfile."
                    }
                     if (!fileExists(env.DOCKERFILE_PATH)) {
                         error "FATAL: Common Dockerfile not found at ${env.DOCKERFILE_PATH}"
                    }

                    def serviceList = env.CHANGED_SERVICES.trim().split(" ")
                    def commitId = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    if (!commitId) {
                        error "FATAL: Could not retrieve Git commit ID."
                    }
                    echo "Using Commit ID for tagging: ${commitId}"

                    def buildFailed = false // Track build/push failures in this stage

                    withCredentials([usernamePassword(credentialsId: env.DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        try {
                            echo "Attempting explicit Docker login..."
                            sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                            echo "Explicit Docker login successful."
                        } catch (e) {
                            error "FATAL: Explicit Docker login failed. Check credentials and permissions. Error: ${e.message}"
                        }
                        
                        for (service in serviceList) {
                            echo "--- Processing Service: ${service} ---"
                            def artifactNameArgValue = "" // Will hold the path *without* .jar suffix

                            // 1. Package the service JAR (inside service directory)
                            dir(service) {
                                try {
                                    echo "Running 'mvn clean package -DskipTests' for ${service}..."
                                    sh 'mvn clean package -DskipTests'
                                    echo "Maven package successful for ${service}."

                                    // Find the JAR relative path
                                    def jarFileFullPath = sh(script: 'find target -maxdepth 1 -name "*.jar" -print -quit', returnStdout: true).trim()
                                    if (!jarFileFullPath) {
                                        error "Could not find JAR file in target/ for ${service}."
                                    }
                                    // Construct path relative to workspace and remove suffix for the build arg
                                    def jarFileRelativePath = "${service}/${jarFileFullPath}"
                                    if (jarFileRelativePath.endsWith('.jar')) {
                                      artifactNameArgValue = jarFileRelativePath.substring(0, jarFileRelativePath.length() - 4)
                                    } else {
                                      // Should not happen if find command works, but handle defensively
                                      error("Could not remove .jar suffix from ${jarFileRelativePath}")
                                    }
                                    echo "JAR path relative to root: ${jarFileRelativePath}"
                                    echo "ARTIFACT_NAME build-arg value: ${artifactNameArgValue}"

                                } catch (err) {
                                    echo "ERROR: Maven package FAILED for ${service}: ${err.getMessage()}"
                                    buildFailed = true
                                    if (currentBuild.currentResult != 'FAILURE') {
                                        currentBuild.result = 'UNSTABLE'
                                    }
                                    continue // Skip Docker steps for this service if package failed
                                }
                            } // End dir(service)

                            // 2. Build and Push Docker Image (from workspace root)
                            if (artifactNameArgValue) { // Check if artifact path (without suffix) was set
                                try {
                                    def imageName = "${env.DOCKERHUB_USERNAME}/${service}"
                                    def imageTag = commitId
                                    echo "Building Docker image: ${imageName}:${imageTag} using ${env.DOCKERFILE_PATH}"

                                    // Pass the path WITHOUT .jar as ARTIFACT_NAME
                                    def dockerImage = docker.build("${imageName}:${imageTag}", "-f ${env.DOCKERFILE_PATH} --build-arg ARTIFACT_NAME=${artifactNameArgValue} .")

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
                                    buildFailed = true
                                    if (currentBuild.currentResult != 'FAILURE') {
                                        currentBuild.result = 'UNSTABLE'
                                    }
                                }
                            } // End if (artifactNameArgValue)
                        } // End for loop
                    } // End withCredentials

                    // Final check for this stage
                    if (buildFailed) {
                        echo "One or more services failed during package or docker build/push."
                        if (currentBuild.currentResult != 'FAILURE') {
                           currentBuild.result = 'UNSTABLE'
                        }
                    } else {
                        echo "All processed services completed package and docker build/push successfully."
                    }

                } // End script
            } // End steps
        } // End Stage 3 (Merged)

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
            echo "Build finished with status SUCCESS."
        }
        unstable {
            echo "Build finished with status UNSTABLE. Check logs for test failures, build issues, or Docker push problems."
        }
        failure {
            echo "Build FAILED. Check logs for critical errors."
        }
    } // End post

} // End pipeline
