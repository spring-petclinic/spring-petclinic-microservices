pipeline {
    agent any
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }
    environment {
        WORKSPACE = "${env.WORKSPACE}"
        // List of services without test folders       
        SERVICES_WITHOUT_TESTS = "spring-petclinic-admin-server spring-petclinic-genai-service"
        // Full list of services
        ALL_SERVICES = "spring-petclinic-admin-server spring-petclinic-api-gateway spring-petclinic-config-server spring-petclinic-customers-service spring-petclinic-discovery-server spring-petclinic-genai-service spring-petclinic-vets-service spring-petclinic-visits-service"
    }
    stages {
        stage('Detect Branch and Changes') {
            steps {
                script {
                    echo "Running pipeline for Branch: ${env.BRANCH_NAME}"
                    
                    // Check if this is the main branch
                    if (env.BRANCH_NAME == 'main') {
                        echo "This is the main branch - will build all services"
                        env.CHANGED_SERVICES = env.ALL_SERVICES
                    } else {
                        // For non-main branches, detect changes
                        // Get changed files between current and previous commit
                        def changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim()
                        
                        // Define service directories to monitor
                        def services = env.ALL_SERVICES.split(" ")
                        
                        // Identify which services have changes
                        env.CHANGED_SERVICES = ""
                        for (service in services) {
                            if (changedFiles.contains(service)) {
                                env.CHANGED_SERVICES = env.CHANGED_SERVICES + " " + service
                            }
                        }
                        // If no specific service changes detected, check for common changes
                        if (env.CHANGED_SERVICES == "") {
                            if (changedFiles.contains("pom.xml") || 
                                changedFiles.contains(".github") || 
                                changedFiles.contains("docker-compose") ||
                                changedFiles.contains("Jenkinsfile")) {
                                echo "Common files changed, will build all services"
                                env.CHANGED_SERVICES = env.ALL_SERVICES
                            } else {
                                echo "No relevant changes detected"
                            }
                        }
                        
                        // Store changed files for detailed reporting
                        env.CHANGED_FILES = changedFiles
                    }
                    
                    echo "Services to build: ${env.CHANGED_SERVICES}"
                }
                
                publishChecks name: 'Detect Changes', status: 'COMPLETED', conclusion: 'SUCCESS', 
                    summary: env.BRANCH_NAME == 'main' ? "Building all services on main branch" : "Changed files detected: ${env.CHANGED_FILES?.split('\n')?.size() ?: 0}",
                    text: env.BRANCH_NAME == 'main' ? 
                        """## Main Branch Build
                        Building all services because this is the main branch.""" :
                        """## Changed Files
                        ```
                        ${env.CHANGED_FILES ?: 'No changes detected'}
                        ```
                        
                        ## Services to Build
                        ```
                        ${env.CHANGED_SERVICES ?: 'No services to build'}
                        ```"""
            }
            post {
                failure {
                    publishChecks name: 'Detect Changes', status: 'COMPLETED', conclusion: 'FAILURE', 
                        summary: 'Failed to detect changes'
                }
            }
        }
        
        stage('Test Services') {
            when {
                expression { return env.CHANGED_SERVICES != "" }
            }
            steps {
                publishChecks name: 'Test Services', status: 'IN_PROGRESS', 
                    summary: 'Running tests for changed services'
                script {
                    def serviceList = env.CHANGED_SERVICES.trim().split(" ")
                    def testDetails = [:]
                    def testFailures = 0
                    def testPasses = 0
                    
                    for (service in serviceList) {
                        echo "Testing service: ${service}"
                        dir(service) {
                            // Check if the service has tests
                            if (!env.SERVICES_WITHOUT_TESTS.contains(service)) {
                                try {
                                    def testOutput = sh(script: 'mvn clean test', returnStdout: true)
                                    testDetails[service] = [
                                        status: 'SUCCESS',
                                        output: testOutput
                                    ]
                                    testPasses++
                                    
                                    // Publish test results
                                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                                    
                                    // Publish coverage reports
                                    jacoco(
                                        execPattern: "target/jacoco.exec",
                                        classPattern: "target/classes", 
                                        sourcePattern: "src/main/java", 
                                        exclusionPattern: "src/test*",
                                        outputDirectory: "target/jacoco-reports/${service}",
                                        reportTitle: "JaCoCo Report - ${service}"
                                    )
                                } catch (Exception e) {
                                    echo "Warning: Tests failed for ${service}, but continuing pipeline"
                                    testDetails[service] = [
                                        status: 'FAILURE',
                                        error: e.getMessage()
                                    ]
                                    testFailures++
                                    currentBuild.result = 'UNSTABLE'
                                }
                            } else {
                                echo "Skipping tests for ${service} as it does not have test folders"
                                testDetails[service] = [
                                    status: 'SKIPPED',
                                    reason: 'No test folders'
                                ]
                            }
                        }
                    }
                    
                    // Store test details for report
                    env.TEST_SUMMARY = "Tests passed: ${testPasses}, failed: ${testFailures}, skipped: ${serviceList.size() - testPasses - testFailures}"
                    
                    // Create detailed test report text
                    def testReportText = "# Test Results Summary\n\n"
                    testReportText += "| Service | Status | Details |\n"
                    testReportText += "|---------|--------|--------|\n"
                    
                    for (service in serviceList) {
                        def details = testDetails[service]
                        def statusEmoji = details.status == 'SUCCESS' ? '✅' : details.status == 'SKIPPED' ? '⏭️' : '❌' 
                        def detailText = details.status == 'SUCCESS' ? 'Tests passed' : 
                                        details.status == 'SKIPPED' ? details.reason : 'Tests failed'
                        testReportText += "| ${service} | ${statusEmoji} ${details.status} | ${detailText} |\n"
                    }
                    
                    testReportText += "\n\n## JUnit Results\nSee Jenkins test results for detailed JUnit information.\n\n"
                    testReportText += "## JaCoCo Coverage\nSee Jenkins coverage reports for detailed code coverage information."
                    
                    env.TEST_REPORT = testReportText
                }
                publishChecks name: 'Test Services', status: 'COMPLETED', 
                    conclusion: currentBuild.result == 'UNSTABLE' ? 'NEUTRAL' : 'SUCCESS',
                    summary: env.TEST_SUMMARY,
                    text: env.TEST_REPORT
            }
            post {
                failure {
                    publishChecks name: 'Test Services', status: 'COMPLETED', conclusion: 'FAILURE', 
                        summary: 'Test execution failed'
                }
            }
        }
        
        stage('Build Services') {
            when {
                expression { return env.CHANGED_SERVICES != "" }
            }
            steps {
                publishChecks name: 'Build Services', status: 'IN_PROGRESS',
                    summary: 'Building changed services'
                script {
                    def serviceList = env.CHANGED_SERVICES.trim().split(" ")
                    def buildDetails = []
                    
                    for (service in serviceList) {
                        echo "Building service: ${service}"
                        dir(service) {
                            try {
                                sh 'mvn package -DskipTests'
                                def artifactName = sh(script: 'find target -name "*.jar" | head -1', returnStdout: true).trim()
                                buildDetails.add("✅ ${service}: Successfully built ${artifactName}")
                                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                            } catch (Exception e) {
                                buildDetails.add("❌ ${service}: Build failed - ${e.getMessage()}")
                                currentBuild.result = 'UNSTABLE'
                            }
                        }
                    }
                    
                    // Create build report
                    env.BUILD_DETAILS = buildDetails.join('\n')
                }
                publishChecks name: 'Build Services', status: 'COMPLETED', 
                    conclusion: currentBuild.result == 'UNSTABLE' ? 'NEUTRAL' : 'SUCCESS',
                    summary: "Built ${env.CHANGED_SERVICES.trim().split(' ').size()} services",
                    text: """## Build Results
                          ```
                          ${env.BUILD_DETAILS}
                          ```
                          
                          All artifacts have been archived."""
            }
            post {
                failure {
                    publishChecks name: 'Build Services', status: 'COMPLETED', conclusion: 'FAILURE',
                        summary: 'Build process failed'
                }
            }
        }

    }
    post {
        always {
            cleanWs()
            script{
                sh 'find . -name jacoco.exec -delete'
            }
        }
    }
}