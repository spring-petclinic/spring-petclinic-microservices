// Jenkinsfile
pipeline {
    // Agent: Run on any available agent. Consider using labeled agents with specific tools (Maven, JDK) if needed.
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
        // Updated based on "Đồ án 2" description (Eureka, Admin, Zipkin, API GW, Customers, Genai, Vets, Visits)
        ALL_SERVICES = "spring-petclinic-admin-server spring-petclinic-api-gateway spring-petclinic-config-server spring-petclinic-customers-service spring-petclinic-discovery-server spring-petclinic-genai-service spring-petclinic-vets-service spring-petclinic-visits-service"

        // List service directories that DO NOT have a standard /src/test or meaningful unit/integration tests
        // Verification needed by checking the actual source code. This is a likely list:
        SERVICES_WITHOUT_TESTS = "spring-petclinic-admin-server spring-petclinic-genai-service" // Tentative list

        // === Internal Flags ===
        // Flag to track if any tests failed during the Test stage
        TESTS_FAILED_FLAG = "false"
    }

    // Stages: Define the main workflow phases
    stages {

        // ============================================================
        // Stage 1: Detect Branch and Changes
        // Determines which services need to be processed based on branch name and file changes.
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
                        // Get changed files between the current commit (HEAD) and the one before it (HEAD~1).
                        // Note: This is simple but might miss changes if multiple commits were pushed at once
                        // or if comparing against a divergence point other than the immediate parent.
                        // Using `|| exit 0` prevents failure if git diff returns no changes (e.g., first commit on branch)
                        def changedFilesOutput = sh(script: "git diff --name-only HEAD~1 HEAD || exit 0", returnStdout: true).trim()

                        if (changedFilesOutput.isEmpty()) {
                            echo "No files found changed between HEAD and HEAD~1."
                            // Optional: Add logic here to compare against 'main' if needed for more complex scenarios
                            // e.g., changedFilesOutput = sh(script: "git diff --name-only origin/main...HEAD || exit 0", returnStdout: true).trim()
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
                            file == 'pom.xml' ||                 // Root Maven POM
                            file == 'Jenkinsfile' ||             // This pipeline script
                            file == 'docker-compose' ||          // Docker compose file
                            file.startsWith('.mvn/') ||          // Maven wrapper config
                            file.startsWith('.github/') ||       // Github workflows
                            file == 'mvnw' || file == 'mvnw.cmd' // Maven wrapper scripts
                            // Add other critical shared files/directories if needed: e.g., shared libraries, parent POMs in specific locations
                        }

                        if (commonFilesChanged) {
                            echo "Common file(s) changed. Processing ALL services."
                            env.CHANGED_SERVICES = env.ALL_SERVICES
                        } else if (!detectedServiceChanges.isEmpty()) {
                            echo "Changes detected in specific services."
                            env.CHANGED_SERVICES = detectedServiceChanges.join(" ")
                        } else {
                            echo "No relevant service or common file changes detected."
                            env.CHANGED_SERVICES = "" // Ensure it's empty if nothing relevant changed
                        }
                    } // End script

                    // --- Final Check and Log ---
                    script {
                        if (env.CHANGED_SERVICES?.trim()) {
                            echo "Services to process in subsequent stages: ${env.CHANGED_SERVICES}"
                        } else {
                            echo "No services require processing. Subsequent stages will be skipped."
                            // Optional: If you want the pipeline to show as 'Success' immediately when no changes
                            // currentBuild.result = 'SUCCESS'
                        }
                    }
                }
            }
        } // End Stage 1

        // ============================================================
        // Stage 2: Test Services
        // Runs tests, publishes JUnit results, and collects JaCoCo coverage data for changed services.
        // ============================================================
        stage('Test Services') {
            // Only run this stage if the CHANGED_SERVICES variable is not empty
            when { expression { return env.CHANGED_SERVICES?.trim() } }
            steps {
                script {
                    def serviceList = env.CHANGED_SERVICES.trim().split(" ")
                    def jacocoExecFiles = [] // List to hold paths to jacoco.exec files
                    def jacocoClassDirs = [] // List to hold paths to class directories
                    def jacocoSrcDirs = []   // List to hold paths to source directories
                    env.TESTS_FAILED_FLAG = "false" // Reset failure flag for this run

                    // Loop through each service identified in Stage 1
                    for (service in serviceList) {
                        echo "--- Preparing to Test Service: ${service} ---"
                        // Check if the service is in the list of services without tests
                        if (env.SERVICES_WITHOUT_TESTS.contains(service)) {
                            echo "Skipping tests for ${service} (marked as having no tests)."
                            continue // Go to the next service in the loop
                        }

                        // Execute tests within the service's directory
                        dir(service) {
                            try {
                                echo "Running 'mvn clean test' for ${service}..."
                                // Run Maven clean and test goals. Assumes JaCoCo plugin runs automatically during test phase.
                                sh 'mvn clean test'
                                echo "Tests completed for ${service}."

                                // Collect paths for JaCoCo aggregation *only if tests passed* and file exists
                                if (fileExists('target/jacoco.exec')) {
                                    echo "Found jacoco.exec for ${service}. Adding to aggregation list."
                                    jacocoExecFiles.add("${service}/target/jacoco.exec")
                                    // Adjust these paths if your project structure is different
                                    jacocoClassDirs.add("${service}/target/classes")
                                    jacocoSrcDirs.add("${service}/src/main/java")
                                } else {
                                    echo "WARNING: jacoco.exec not found for ${service} after tests ran (or tests didn't run)."
                                }

                            } catch (err) {
                                // Test execution failed
                                echo "ERROR: Tests FAILED for ${service}. Marking build as UNSTABLE."
                                env.TESTS_FAILED_FLAG = "true" // Set failure flag
                                // Pipeline continues to allow other tests and build stage, but result will be UNSTABLE
                            } finally {
                                // Always attempt to publish JUnit test results, even if tests failed
                                echo "Publishing JUnit results for ${service}..."
                                // allowEmptyResults: Prevents failure if no XML reports are found (e.g., module has no tests)
                                // testResults: Glob pattern to find Surefire XML reports
                                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                            }
                        } // End dir(service)
                    } // End for loop for services

                    // --- Generate Aggregated JaCoCo Report ---
                    // This happens *after* all selected services have been tested
                    if (!jacocoExecFiles.isEmpty()) {
                        echo "--- Generating Aggregated JaCoCo Coverage Report ---"
                        echo "Aggregating JaCoCo data from ${jacocoExecFiles.size()} service(s)."
                        try {
                            // Use the JaCoCo plugin step
                            jacoco(
                                execPattern: jacocoExecFiles.join(','),    // Comma-separated list of .exec file paths
                                classPattern: jacocoClassDirs.join(','),  // Comma-separated list of compiled class directories
                                sourcePattern: jacocoSrcDirs.join(','), // Comma-separated list of source code directories
                                inclusionPattern: '**/*.class',          // Include all compiled classes
                                exclusionPattern: '**/test/**,**/model/**,**/domain/**,**/entity/**', // Exclude test classes, models/DTOs etc. Adjust as needed.
                                skipCopyOfSrcFiles: true                // Optimization: don't copy sources to report output
                            )
                            echo "JaCoCo aggregated report generated successfully."
                        } catch (err) {
                            echo "ERROR: Failed to generate JaCoCo aggregated report: ${err.getMessage()}"
                            // Log the error but don't necessarily fail the build here, maybe just mark unstable
                            currentBuild.result = 'UNSTABLE'
                        }
                    } else {
                        echo "No JaCoCo execution data found to aggregate (either no tests ran, tests failed before coverage, or no services with tests were selected)."
                    }

                    // Set final build status based on test flag
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
        // Stage 3: Build Services
        // Compiles and packages the application artifacts (JARs) for changed services.
        // ============================================================
        stage('Build Services') {
            // Only run this stage if the CHANGED_SERVICES variable is not empty
            when { expression { return env.CHANGED_SERVICES?.trim() } }
            steps {
                script {
                    def serviceList = env.CHANGED_SERVICES.trim().split(" ")
                    def buildFailed = false // Track if any build in this stage fails

                    // Loop through each service identified in Stage 1
                    for (service in serviceList) {
                        echo "--- Preparing to Build Service: ${service} ---"
                        // Build within the service's directory
                        dir(service) {
                            try {
                                echo "Running 'mvn clean package -DskipTests' for ${service}..."
                                // Clean, compile, and package. Skip tests as they ran in the previous stage.
                                sh 'mvn clean package -DskipTests'
                                echo "Build successful for ${service}."

                                // Archive the resulting JAR artifact(s)
                                // Use find to reliably get the JAR name(s) in the target directory
                                def artifactPath = sh(script: 'find target -maxdepth 1 -name "*.jar" -print -quit', returnStdout: true).trim()
                                if (artifactPath) {
                                    echo "Archiving artifact: ${artifactPath}"
                                    // archiveArtifacts stores build outputs associated with the Jenkins build record
                                    archiveArtifacts artifacts: artifactPath, fingerprint: true // Fingerprinting helps track artifact usage
                                } else {
                                    echo "WARNING: Could not find JAR artifact for ${service} in target/ directory after build."
                                }
                            } catch (err) {
                                // Build failed for this service
                                echo "ERROR: Build FAILED for ${service}."
                                buildFailed = true
                                // Let the loop continue to attempt building other services
                            }
                        } // End dir(service)
                    } // End for loop

                    // Set final status: If this stage had build failures OR if tests failed earlier, mark UNSTABLE
                    if (buildFailed) {
                        echo "Setting build status to UNSTABLE due to build failures in this stage."
                        currentBuild.result = 'UNSTABLE'
                    } else if (env.TESTS_FAILED_FLAG == "true") {
                         echo "Builds successful, but marking UNSTABLE due to earlier test failures."
                         currentBuild.result = 'UNSTABLE'
                    }
                    else {
                         echo "All selected services built successfully."
                         // If we reach here and TESTS_FAILED_FLAG is false, build remains SUCCESS (default Jenkins state)
                    }
                } // End script
            } // End steps
        } // End Stage 3

    } // End stages

    // ============================================================
    // Post Actions: Define actions to run after stages complete
    // ============================================================
    post {
        // Always: Runs regardless of the build's final status (SUCCESS, UNSTABLE, FAILURE, etc.)
        always {
            echo "Pipeline finished with final status: ${currentBuild.currentResult}"
            // Clean up the workspace to save disk space
            cleanWs()
        }
        // Success: Runs only if the build status is SUCCESS
        success {
            echo "Build was successful!"
            // Add success notifications if needed (e.g., Slack, Email)
        }
        // Unstable: Runs only if the build status is UNSTABLE (e.g., test failures, build failures handled gracefully)
        unstable {
            echo "Build is UNSTABLE. Check logs for test failures or non-critical build issues."
            // Add unstable notifications if needed
        }
        // Failure: Runs only if the build status is FAILURE (e.g., unhandled error, script compilation error)
        failure {
            echo "Build FAILED. Check logs for critical errors."
            // Add failure notifications if needed
        }
    } // End post

} // End pipeline
