pipeline {
    agent any
    environment {
        MAVEN_HOME = tool 'Maven'
        GITHUB_TOKEN = credentials('github-token')
    }
    stages {
        stage('Checkout') {
            steps {
                githubNotify context: 'jenkins-ci', 
                           description: 'Jenkins Pipeline Started',
                           status: 'PENDING'
                checkout scm
            }
        }
        stage('Detect Changes') {
            steps {
                script {
                    // Debug: Print all environment variables
                    echo "Environment variables:"
                    sh 'env | sort'
                    
                    // Get all changed files
                    def changes = []
                    if (env.CHANGE_TARGET) {
                        // If this is a PR build, fetch the target branch first
                        sh """
                            git fetch --no-tags origin ${env.CHANGE_TARGET}:refs/remotes/origin/${env.CHANGE_TARGET}
                            git fetch --no-tags origin ${env.GIT_COMMIT}:refs/remotes/origin/PR-${env.CHANGE_ID}
                        """
                        changes = sh(script: "git diff --name-only origin/${env.CHANGE_TARGET} HEAD", returnStdout: true).trim().split('\n')
                    } else if (env.GIT_PREVIOUS_SUCCESSFUL_COMMIT) {
                        // If this is a branch build with previous successful build
                        changes = sh(script: "git diff --name-only ${env.GIT_PREVIOUS_SUCCESSFUL_COMMIT}", returnStdout: true).trim().split('\n')
                    } else {
                        // Fallback to comparing with the previous commit
                        changes = sh(script: "git diff --name-only HEAD^", returnStdout: true).trim().split('\n')
                    }

                    // Map to store which services need to be built
                    def servicesToBuild = [:]
                    def services = [
                        'admin-server': 'spring-petclinic-admin-server',
                        'api-gateway': 'spring-petclinic-api-gateway',
                        'config-server': 'spring-petclinic-config-server',
                        'customers-service': 'spring-petclinic-customers-service',
                        'discovery-server': 'spring-petclinic-discovery-server',
                        'vets-service': 'spring-petclinic-vets-service',
                        'visits-service': 'spring-petclinic-visits-service',
                        'genai-service': 'spring-petclinic-genai-service'
                    ]

                    // Check root pom.xml changes
                    boolean rootPomChanged = changes.any { it == 'pom.xml' }
                    
                    // Check shared resources changes (like docker configs, scripts, etc.)
                    boolean sharedResourcesChanged = changes.any { change ->
                        change.startsWith('docker/') || 
                        change.startsWith('scripts/') || 
                        change.startsWith('.mvn/') ||
                        change == 'docker-compose.yml'
                    }

                    // If shared resources changed, build all services
                    if (rootPomChanged || sharedResourcesChanged) {
                        echo "Shared resources changed. Building all services."
                        services.each { serviceKey, servicePath ->
                            servicesToBuild[serviceKey] = true
                        }
                    } else {
                        // Determine which services have changes
                        services.each { serviceKey, servicePath ->
                            if (changes.any { change ->
                                change.startsWith("${servicePath}/")
                            }) {
                                servicesToBuild[serviceKey] = true
                                echo "Will build ${serviceKey} due to changes in ${servicePath}"
                            }
                        }
                    }

                    // If no services need building, set a flag
                    env.NO_SERVICES_TO_BUILD = servicesToBuild.isEmpty() ? 'true' : 'false'
                    // Store the services to build in environment variable
                    env.SERVICES_TO_BUILD = servicesToBuild.keySet().join(',')
                    
                    // Print summary
                    if (env.NO_SERVICES_TO_BUILD == 'true') {
                        echo "No service changes detected. Pipeline will skip build and test stages."
                    } else {
                        echo "Services to build: ${env.SERVICES_TO_BUILD}"
                    }
                }
            }
        }
        stage('Test') {
            when {
                expression { env.NO_SERVICES_TO_BUILD == 'false' }
            }
            steps {
                script {
                    def failures = []
                    
                    env.SERVICES_TO_BUILD.split(',').each { service ->
                        dir("spring-petclinic-${service}") {
                            echo "Testing ${service}..."
                            try {
                                // Run tests with JaCoCo coverage for specific service
                                sh """
                                    echo "Running tests for ${service}"
                                    ../mvnw clean test verify -Pcoverage
                                """

                                def coverageReport = readFile('target/site/jacoco/index.html')
                                def matcher = coverageReport =~ /Total.*?([0-9.]+)%/
                                if (matcher.find()) {
                                    def coverage = matcher[0][1] as Double
                                    echo "Code coverage for ${service}: ${coverage}%"
                                    if (coverage < 60) {
                                        failures.add("${service}: Code coverage ${coverage}% is below minimum required 70%")
                                    }
                                } else {
                                    failures.add("${service}: Could not determine code coverage")
                                }
                            } catch (Exception e) {
                                echo "Tests or coverage check failed for ${service}: ${e.message}"
                                failures.add("${service}: ${e.message}")
                                // Continue with next service instead of throwing
                            }
                        }
                    }
                    
                    // After all services are tested, fail the build if any service failed
                    if (failures.size() > 0) {
                        error """Multiple services failed tests or coverage checks:
                                ${failures.join('\n')}"""
                    }
                }
            }
            post {
                always {
                    script {
                        // Publish test results and coverage for all changed services
                        env.SERVICES_TO_BUILD.split(',').each { service ->
                            dir("spring-petclinic-${service}") {
                                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                                jacoco(
                                    execPattern: '**/target/jacoco.exec',
                                    classPattern: '**/target/classes',
                                    sourcePattern: '**/src/main/java',
                                    exclusionPattern: '**/test/**'
                                )
                            }
                        }
                    }
                }
            }
        }
        stage('Build') {
            when {
                expression { env.NO_SERVICES_TO_BUILD == 'false' }
            }
            steps {
                script {
                    env.SERVICES_TO_BUILD.split(',').each { service ->
                        dir("spring-petclinic-${service}") {
                            echo "Building ${service}..."
                            try {
                                sh """
                                    echo "Building ${service}"
                                    ../mvnw clean package -DskipTests
                                """
                            } catch (Exception e) {
                                echo "Build failed for ${service}"
                                throw e
                            }
                        }
                    }
                }
            }
            post {
                success {
                    script {
                        // Archive artifacts for changed services
                        env.SERVICES_TO_BUILD.split(',').each { service ->
                            dir("spring-petclinic-${service}") {
                                archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                            }
                        }
                    }
                }
            }
        }
        stage('Auto Merge') {
            when {
                allOf {
                    expression { env.NO_SERVICES_TO_BUILD == 'false' }
                    expression { env.CHANGE_ID != null }
                }
            }
            steps {
                script {
                    def prNumber = env.CHANGE_ID
                    def repo = env.GIT_URL.replaceFirst(/^.*github\.com[:/]/, '').replaceFirst(/\.git$/, '')
                    
                    // Get PR details and check approvals
                    def apiUrl = "https://api.github.com/repos/${repo}/pulls/${prNumber}/reviews"
                    def response = sh(
                        script: """
                            curl -s -H "Authorization: token ${GITHUB_TOKEN}" ${apiUrl}
                        """,
                        returnStdout: true
                    )
                    
                    def reviews = readJSON(text: response)
                    def approvalCount = reviews.count { it.state == 'APPROVED' }
                    
                    echo "Number of approvals: ${approvalCount}"
                    
                    if (approvalCount >= 1) {
                        echo "PR has sufficient approvals (${approvalCount}). Proceeding with merge..."
                        
                        // Merge the PR
                        def mergeUrl = "https://api.github.com/repos/${repo}/pulls/${prNumber}/merge"
                        def mergeResponse = sh(
                            script: """
                                curl -s -X PUT \
                                -H "Authorization: token ${GITHUB_TOKEN}" \
                                -H "Accept: application/vnd.github.v3+json" \
                                -d '{"merge_method":"squash"}' \
                                ${mergeUrl}
                            """,
                            returnStdout: true
                        )
                        
                        echo "Merge response: ${mergeResponse}"
                    } else {
                        echo "PR needs at least 2 approvals to auto-merge (current: ${approvalCount})"
                    }
                }
            }
        }
    }
    post {
        success {
            githubNotify context: 'jenkins-ci',
                        description: 'Pipeline completed successfully',
                        status: 'SUCCESS'
            cleanWs()
        }
        failure {
            githubNotify context: 'jenkins-ci',
                        description: 'Pipeline failed',
                        status: 'FAILURE'
            cleanWs()
        }
        unstable {
            githubNotify context: 'jenkins-ci',
                        description: 'Pipeline is unstable',
                        status: 'ERROR'
            cleanWs()
        }
    }
}