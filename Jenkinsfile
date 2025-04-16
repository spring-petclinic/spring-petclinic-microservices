// Jenkinsfile - Updated with Parameters and Helm Deployment Stage
pipeline {
    agent any // Use any available agent configured with Docker, JDK, git, kubectl, helm
    tools { jdk 'jdk-17' }
    // ================== PARAMETERS ==================
    // Define parameters for developers to specify branch/tag for each service
    parameters {
        string(name: 'CONFIG_SERVER_BRANCH', defaultValue: 'main', description: 'Branch/Tag for config-server')
        string(name: 'DISCOVERY_SERVER_BRANCH', defaultValue: 'main', description: 'Branch/Tag for discovery-server')
        string(name: 'CUSTOMERS_SERVICE_BRANCH', defaultValue: 'main', description: 'Branch/Tag for customers-service')
        string(name: 'VISITS_SERVICE_BRANCH', defaultValue: 'main', description: 'Branch/Tag for visits-service')
        string(name: 'VETS_SERVICE_BRANCH', defaultValue: 'main', description: 'Branch/Tag for vets-service')
        string(name: 'GENAI_SERVICE_BRANCH', defaultValue: 'main', description: 'Branch/Tag for genai-service')
        string(name: 'API_GATEWAY_BRANCH', defaultValue: 'main', description: 'Branch/Tag for api-gateway')
        string(name: 'ADMIN_SERVER_BRANCH', defaultValue: 'main', description: 'Branch/Tag for admin-server')
        // Parameter for deployment namespace
        string(name: 'DEPLOY_NAMESPACE', defaultValue: 'dev-build', description: 'Kubernetes namespace for deployment')
        // Parameter for Helm release name
        string(name: 'HELM_RELEASE_NAME', defaultValue: 'dev-build-petclinic', description: 'Helm release name for this deployment')
    }
    // =================================================

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timestamps()
        // Disable concurrent builds for this parameterized job to avoid conflicts deploying the same Helm release
        disableConcurrentBuilds()
    }
    environment {
        // Keep existing service lists
        ALL_SERVICES = "spring-petclinic-admin-server spring-petclinic-api-gateway spring-petclinic-config-server spring-petclinic-customers-service spring-petclinic-discovery-server spring-petclinic-genai-service spring-petclinic-vets-service spring-petclinic-visits-service"
        SERVICES_WITHOUT_TESTS = "spring-petclinic-admin-server spring-petclinic-genai-service"
        // Ensure Docker Hub username and credentials ID are correct
        DOCKERHUB_USERNAME = "22127422" // <--- *** REPLACE THIS ***
        DOCKERHUB_CREDENTIALS_ID = "docker-credentials"     // <--- *** REPLACE THIS if you used a different ID ***
        TESTS_FAILED_FLAG = "false"
        DOCKERFILE_PATH = "docker/Dockerfile"
        // Helm chart location relative to workspace root
        HELM_CHART_PATH = "./helm-charts/petclinic-umbrella"
        // Path to the kubeconfig file on the agent (assuming standard location)
        // Ensure this file has the insecure-skip-tls-verify: true set for the cluster
        KUBECONFIG_PATH = "/home/jenkins/.kube/config" // Adjust if your agent user/path is different
    }

    stages {

        // ============================================================
        // Stage 1: Detect Branch and Changes (Optional for Parameterized Build?)
        // ============================================================
        // Note: For a purely parameterized deployment job, you might skip change
        // detection, testing, and image building if you assume images built by a
        // separate CI job already exist. However, keeping them allows this single
        // job to perform CI *and* CD based on the trigger (commit vs. manual build).
        // For now, we keep it to ensure the necessary commit/branch tags are available if needed.
        stage('Detect Branch and Changes') {
            steps {
                script {
                    // --- Keep existing change detection logic ---
                    // This ensures env.BRANCH_NAME and env.CHANGED_SERVICES are set
                    // if the build was triggered by a commit. For manual builds,
                    // BRANCH_NAME might be null, and the logic defaults to 'main'.
                    // The deployment stage below will primarily use parameters.
                    echo "Pipeline started for Branch: ${env.BRANCH_NAME ?: 'manual trigger'}"
                    env.CHANGED_SERVICES = "" // Initialize list of services to process

                    def currentBranch = env.BRANCH_NAME ?: 'main' // Default context if manually triggered
                    echo "Current effective branch for processing: ${currentBranch}"

                    if (currentBranch == 'main') {
                        echo "Running on 'main' branch context or manual trigger default. Processing ALL services for CI steps."
                        env.CHANGED_SERVICES = env.ALL_SERVICES
                    } else {
                        echo "Running on feature branch context '${currentBranch}'. Detecting changes..."
                        // ...(Existing change detection logic remains here)...
                        // Fetch main to ensure comparison baseline is up-to-date
                        sh 'git fetch origin main:refs/remotes/origin/main'
                        def changedFilesOutput = ''
                        try {
                            changedFilesOutput = sh(script: "git diff --name-only origin/main...HEAD || exit 0", returnStdout: true).trim()
                            if (changedFilesOutput.isEmpty()) {
                                echo "No changes detected between '${currentBranch}' and 'origin/main'. Comparing against HEAD~1 as fallback."
                                changedFilesOutput = sh(script: "git diff --name-only HEAD~1 HEAD || exit 0", returnStdout: true).trim()
                            }
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
                            file == 'pom.xml' || file == 'Jenkinsfile' || file == env.DOCKERFILE_PATH ||
                            file.startsWith('docker/') || file.startsWith('.mvn/') || file.startsWith('.github/') ||
                            file.startsWith(env.HELM_CHART_PATH) || // Include Helm chart changes
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
                    } // End else (feature branch)

                    // Log final decision
                    if (env.CHANGED_SERVICES?.trim()) {
                        echo "Services to process in CI stages: ${env.CHANGED_SERVICES}"
                    } else {
                        echo "No services require processing in CI stages."
                    }
                    // --- End of existing change detection logic ---
                } // End script
            } // End steps
        } // End Stage 1

        // ============================================================
        // Stage 2: Test Services
        // ============================================================
        // Only run if triggered by a commit resulting in changes
        stage('Test Services') {
            when { expression { return env.CHANGE_ID || env.BRANCH_NAME } } // Crude check if commit-triggered
            steps {
                script {
                     // --- Keep existing test logic ---
                    if (!env.CHANGED_SERVICES?.trim()) {
                        echo "Skipping tests as no relevant service changes detected."
                        return // Exit steps block
                    }
                    def serviceList = env.CHANGED_SERVICES.trim().split(" ")
                    def jacocoExecFiles = []
                    def jacocoClassDirs = []
                    def jacocoSrcDirs = []
                    env.TESTS_FAILED_FLAG = "false" // Reset flag

                    for (service in serviceList) {
                        echo "--- Preparing to Test Service: ${service} ---"
                        if (env.SERVICES_WITHOUT_TESTS.contains(service)) {
                            echo "Skipping tests for ${service}."
                            continue
                        }
                        dir(service) {
                            try {
                                echo "Running 'mvn clean test' for ${service}..."
                                sh 'mvn clean test'
                                echo "Tests completed for ${service}."
                                if (fileExists('target/jacoco.exec')) {
                                    jacocoExecFiles.add("${service}/target/jacoco.exec")
                                    jacocoClassDirs.add("${service}/target/classes")
                                    jacocoSrcDirs.add("${service}/src/main/java")
                                } else {
                                    echo "WARNING: jacoco.exec not found for ${service}."
                                }
                            } catch (err) {
                                echo "ERROR: Tests FAILED for ${service}. Marking build as UNSTABLE."
                                env.TESTS_FAILED_FLAG = "true"
                                currentBuild.result = 'UNSTABLE'
                            } finally {
                                echo "Publishing JUnit results for ${service}..."
                                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                            }
                        } // End dir
                    } // End for

                    if (!jacocoExecFiles.isEmpty()) {
                        echo "--- Generating Aggregated JaCoCo Coverage Report ---"
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
                        echo "All CI tests passed or were skipped."
                    }
                    // --- End of existing test logic ---
                } // End script
            } // End steps
        } // End Stage 2


        // ============================================================
        // Stage 3: Build, Package & Push Docker Images
        // ============================================================
        // Only run if triggered by a commit resulting in changes AND tests didn't fail critically
        stage('Build, Package & Push Docker Images') {
            when {
                expression { return (env.CHANGE_ID || env.BRANCH_NAME) && currentBuild.currentResult != 'FAILURE' }
            }
            steps {
                script {
                    // --- Keep existing build/push logic ---
                    if (!env.CHANGED_SERVICES?.trim()) {
                        echo "Skipping image build/push as no relevant service changes detected."
                        return // Exit steps block
                    }
                    // Sanity checks for Docker Hub config
                    if (!env.DOCKERHUB_USERNAME || env.DOCKERHUB_USERNAME == 'YOUR_DOCKERHUB_USERNAME_HERE') {
                        error "FATAL: DOCKERHUB_USERNAME environment variable is not set or not replaced."
                    }
                    if (!env.DOCKERHUB_CREDENTIALS_ID) { // Allow if explicitly set, even if default name
                         error "FATAL: DOCKERHUB_CREDENTIALS_ID environment variable is not set."
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

                    def buildFailed = false // Track build/push failures

                    withCredentials([usernamePassword(credentialsId: env.DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
                        for (service in serviceList) {
                            echo "--- Processing Service: ${service} ---"
                            def artifactNameArgValue = ""
                            dir(service) { // Package within service directory
                                try {
                                    echo "Running 'mvn clean package -DskipTests' for ${service}..."
                                    sh 'mvn clean package -DskipTests'
                                    echo "Maven package successful for ${service}."
                                    def jarFileInTarget = sh(script: 'find target -maxdepth 1 -name "*.jar" -print -quit', returnStdout: true).trim()
                                    if (!jarFileInTarget) { error "Could not find JAR file in target/ for ${service}." }
                                    def jarFileRelativePath = "${service}/${jarFileInTarget}"
                                    if (jarFileRelativePath.endsWith('.jar')) {
                                      artifactNameArgValue = jarFileRelativePath.substring(0, jarFileRelativePath.length() - 4)
                                    } else { error("Could not remove .jar suffix from ${jarFileRelativePath}") }
                                    echo "JAR path relative to root: ${jarFileRelativePath}"
                                    echo "ARTIFACT_NAME build-arg value: ${artifactNameArgValue}"
                                } catch (err) {
                                    echo "ERROR: Maven package FAILED for ${service}: ${err.getMessage()}"
                                    buildFailed = true; currentBuild.result = 'UNSTABLE'; continue
                                }
                            } // End dir

                            if (artifactNameArgValue) { // Build/Push from workspace root
                                try {
                                    // Construct image name WITH prefix
                                    def imageName = "${env.DOCKERHUB_USERNAME}/${service.replaceFirst('spring-petclinic-', 'spring-petclinic/')}" // Assuming repo structure matches name
                                    // Correcting the construction to handle potential double slashes or just use the service name directly if it includes the prefix
                                    imageName = "${env.DOCKERHUB_USERNAME}/${service}" // Use full service name from list

                                    def imageTagCommit = commitId
                                    echo "Building Docker image: ${imageName}:${imageTagCommit} using ${env.DOCKERFILE_PATH}"
                                    def dockerImage = docker.build("${imageName}:${imageTagCommit}", "-f ${env.DOCKERFILE_PATH} --build-arg ARTIFACT_NAME=${artifactNameArgValue} .")

                                    echo "Pushing Docker image: ${imageName}:${imageTagCommit}"
                                    dockerImage.push("${imageTagCommit}") // Push commit-ID tag

                                    def currentBranch = env.BRANCH_NAME ?: 'main'
                                    if (currentBranch == 'main') {
                                        echo "Pushing additional tag 'latest' for main branch build: ${imageName}:latest"
                                        dockerImage.push('latest') // Push 'latest' tag
                                    } else {
                                        def branchTag = currentBranch.replaceAll(/[^a-zA-Z0-9_.-]/, '-') // Sanitize branch name for tag
                                        echo "Pushing additional tag for branch name: ${imageName}:${branchTag}"
                                        dockerImage.push("${branchTag}") // Push branch name tag
                                    }
                                    echo "Docker build and push successful for ${service}."
                                } catch (err) {
                                    echo "ERROR: Docker build or push FAILED for ${service}: ${err.getMessage()}"
                                    buildFailed = true; currentBuild.result = 'UNSTABLE'
                                }
                            } // End if artifactNameArgValue
                        } // End for loop
                        sh "docker logout"
                    } // End withCredentials

                    if (buildFailed) { echo "One or more services failed CI build/push." }
                    else { echo "All processed services completed CI build/push successfully." }
                     // --- End of existing build/push logic ---
                } // End script
            } // End steps
        } // End Stage 3

        // ============================================================
        // STAGE 4: Deploy Developer Build via Helm
        // ============================================================
        // This stage runs regardless of trigger type (commit or manual)
        // if the previous stages didn't result in FAILURE
        stage('Deploy Developer Build') {
            when { expression { currentBuild.currentResult != 'FAILURE' } }
            steps {
                script {
                    echo "Starting Helm deployment for Release: ${params.HELM_RELEASE_NAME} into Namespace: ${params.DEPLOY_NAMESPACE}"

                    // 1. Check Prerequisites
                    if (!fileExists(env.HELM_CHART_PATH)) {
                        error "FATAL: Helm chart not found at ${env.HELM_CHART_PATH}. Ensure helm-charts directory exists at workspace root."
                    }
                     if (!fileExists(env.KUBECONFIG_PATH)) {
                        echo "WARNING: KUBECONFIG_PATH ${env.KUBECONFIG_PATH} not found. Assuming kubectl/helm are configured externally."
                        // Allow continuing, but flag it. Helm/kubectl might use default location or require manual agent setup.
                    } else {
                        echo "Using kubeconfig from ${env.KUBECONFIG_PATH}"
                    }

                    // 2. Construct Helm --set arguments for image tags based on parameters
                    def imageTagSetArgs = []
                    def services = env.ALL_SERVICES.split(" ")
                    def commitId = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim() // Get current commit ID again

                    for (service in services) {
                        // Convert Jenkins service name (e.g., spring-petclinic-customers-service) to param name (e.g., CUSTOMERS_SERVICE_BRANCH)
                        // And also to Helm sub-chart name (e.g., customers-service)
                        def paramName = service.replace('spring-petclinic-', '').toUpperCase().replaceAll("-", "_") + "_BRANCH"
                        def subChartName = service.replace('spring-petclinic-', '')
                        def paramValue = params[paramName] // Get the value selected by the developer

                        def imageTagToDeploy = "latest" // Default tag for 'main' branch parameter
                        if (paramValue && paramValue != 'main') {
                            // If parameter is not 'main', assume it's a branch name.
                            // Use the sanitized branch name tag pushed by the CI stage.
                            imageTagToDeploy = paramValue.replaceAll(/[^a-zA-Z0-9_.-]/, '-')
                            echo "Service '${subChartName}': Using branch tag '${imageTagToDeploy}' based on parameter '${paramValue}'."
                        } else {
                             echo "Service '${subChartName}': Using default tag 'latest' based on parameter 'main'."
                        }
                        // Add the --set argument for this service's image tag
                        imageTagSetArgs.add("--set ${subChartName}.image.tag=${imageTagToDeploy}")
                    }

                    // Join all --set arguments into a single string
                    def helmSetString = imageTagSetArgs.join(" ")
                    echo "Helm --set arguments for image tags: ${helmSetString}"

                    // 3. Execute Helm Deployment inside withKubeConfig block
                    // This ensures helm uses the correct context and insecure skip setting
                    withKubeConfig(credentialsId: '', configFile: env.KUBECONFIG_PATH) { // Use file, no credentialsId needed if config has user auth
                        try {
                            echo "Updating Helm dependencies for chart at ${env.HELM_CHART_PATH}..."
                            // Run update inside the chart directory
                            dir(env.HELM_CHART_PATH) {
                                sh "helm dependency update"
                            }

                            echo "Running Helm upgrade --install..."
                            // Use sh step for better control over the command string
                            sh """
                                helm upgrade --install ${params.HELM_RELEASE_NAME} \\
                                    ${env.HELM_CHART_PATH} \\
                                    --namespace ${params.DEPLOY_NAMESPACE} \\
                                    --create-namespace \\
                                    --set global.dockerHubUser=${env.DOCKERHUB_USERNAME} \\
                                    ${helmSetString} \\
                                    --timeout 10m \\
                                    --wait
                            """
                            // --wait flag waits for resources to become ready (respecting probes)
                            // --timeout prevents waiting forever if something goes wrong

                            echo "Helm deployment successful for Release: ${params.HELM_RELEASE_NAME}"
                            currentBuild.result = 'SUCCESS' // Explicitly mark success if helm succeeds

                            // Provide access info (NodePort details are dynamic)
                            echo "Deployment complete. Access services via NodePorts assigned by Kubernetes."
                            echo "Run 'kubectl get svc -n ${params.DEPLOY_NAMESPACE}' to find the NodePorts."
                            echo "Access URL will be http://<WorkerNodeIP>:<NodePort>"
                            echo "Remember to potentially add '<WorkerNodeIP> <some-domain-name>' to your local hosts file."

                        } catch (err) {
                            echo "ERROR: Helm deployment FAILED for Release: ${params.HELM_RELEASE_NAME}"
                            error "Helm deployment failed: ${err.getMessage()}" // Marks build as FAILED
                        }
                    } // End withKubeConfig
                } // End script
            } // End steps
        } // End Stage 4 (Deploy)

    } // End stages

    // ============================================================
    // Post Actions
    // ============================================================
    post {
        always {
            echo "Pipeline finished with final status: ${currentBuild.currentResult}"
            // Optional: Add cleanup for Helm release ONLY if build failed AND helm release exists?
            // Be careful with automatic cleanup. Usually done via separate job.
            cleanWs()
        }
        success {
            echo "Build finished with status SUCCESS."
            // Could add a link to the deployed app if NodePort/IP could be determined reliably
        }
        unstable {
            echo "Build finished with status UNSTABLE. Check logs for test failures, build issues, or Docker push problems. Deployment might have been skipped or failed."
        }
        failure {
            echo "Build FAILED. Check logs for critical errors."
            // Consider Helm uninstall here? Needs helm release name. Risky.
            // Example (Use with caution):
            // script {
            //     try {
            //         withKubeConfig(credentialsId: '', configFile: env.KUBECONFIG_PATH) {
            //             echo "Attempting cleanup of failed Helm release: ${params.HELM_RELEASE_NAME}"
            //             sh "helm uninstall ${params.HELM_RELEASE_NAME} -n ${params.DEPLOY_NAMESPACE} --wait"
            //         }
            //     } catch (cleanupErr) {
            //         echo "Helm cleanup failed: ${cleanupErr.getMessage()}"
            //     }
            // }
        }
    } // End post

} // End pipeline
