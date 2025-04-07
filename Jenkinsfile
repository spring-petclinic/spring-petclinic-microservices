pipeline {
    agent any
    
    tools {
        maven 'Maven 3'
    }
    
    environment {
        MINIMUM_COVERAGE = '70'
        GITHUB_APP_CREDENTIAL = credentials('github-app-checks')
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timeout(time: 60, unit: 'MINUTES')
        timestamps()
    }
    
    stages {
        stage('Debug Environment') {
            steps {
                script {
                    echo "Build information:"
                    echo "- Branch: ${env.BRANCH_NAME}"
                    echo "- Pull Request: ${env.CHANGE_ID ? 'Yes (#' + env.CHANGE_ID + ')' : 'No'}"
                    echo "- Target Branch: ${env.CHANGE_TARGET ?: 'N/A'}"
                    echo "- Build URL: ${env.BUILD_URL}"
                }
            }
        }
        
        stage('Determine Changed Services') {
            steps {
                script {
                    // Define SERVICES in script block
                    def SERVICES = [
                        'customers-service': 'spring-petclinic-customers-service',
                        'vets-service': 'spring-petclinic-vets-service',
                        'visits-service': 'spring-petclinic-visits-service',
                        'api-gateway': 'spring-petclinic-api-gateway',
                        'discovery-server': 'spring-petclinic-discovery-server',
                        'config-server': 'spring-petclinic-config-server',
                        'admin-server': 'spring-petclinic-admin-server',
                        'genai-service': 'spring-petclinic-genai-service'
                    ]
                    
                    // Initialize empty list for changed services
                    env.CHANGED_SERVICES = ""
                    
                    // For pull requests, compare with target branch
                    if (env.CHANGE_ID) {
                        echo "Processing Pull Request #${env.CHANGE_ID}"
                        // Set initial GitHub check for PR
                        if (env.CHANGE_ID) {
                            try {
                                githubChecks(
                                    name: "CI Pipeline",
                                    status: 'IN_PROGRESS',
                                    detailsURL: env.BUILD_URL,
                                    output: [
                                        title: 'CI Pipeline Running',
                                        summary: 'Analyzing changed services and running tests...',
                                        text: 'This check will update when the pipeline completes.'
                                    ]
                                )
                            } catch (Exception e) {
                                echo "Warning: Failed to create GitHub check: ${e.message}"
                            }
                        }
                        
                        SERVICES.each { service, path ->
                            def changes = sh(
                                script: "git diff origin/${env.CHANGE_TARGET}...HEAD --name-only | grep ^${path}/ || true",
                                returnStdout: true
                            ).trim()
                            
                            if (changes) {
                                echo "Changes detected in ${service}"
                                env.CHANGED_SERVICES = env.CHANGED_SERVICES + " " + service
                            }
                        }
                    } 
                    // For direct branch builds, compare with previous commit
                    else {
                        echo "Processing branch ${env.BRANCH_NAME}"
                        SERVICES.each { service, path ->
                            def changes = sh(
                                script: "git diff HEAD^ HEAD --name-only | grep ^${path}/ || true",
                                returnStdout: true
                            ).trim()
                            
                            if (changes) {
                                echo "Changes detected in ${service}"
                                env.CHANGED_SERVICES = env.CHANGED_SERVICES + " " + service
                            }
                        }
                    }
                    
                    // If no specific service changes detected, build all
                    if (env.CHANGED_SERVICES.trim().isEmpty()) {
                        echo "No specific service changes detected, will process all services"
                        env.CHANGED_SERVICES = SERVICES.keySet().join(" ")
                    }
                    
                    echo "Services to process: ${env.CHANGED_SERVICES}"
                    
                    // Store SERVICES map for later stages
                    env.SERVICES_JSON = groovy.json.JsonOutput.toJson(SERVICES)
                }
            }
        }
        
        stage('Test') {
            steps {
                script {
                    def SERVICES = readJSON text: env.SERVICES_JSON
                    def servicesToTest = env.CHANGED_SERVICES.trim().split(" ")
                    
                    // Track overall results
                    def allServicesPass = true
                    def allCoverageResults = [:]
                    
                    servicesToTest.each { service ->
                        def path = SERVICES[service]
                        if (path) {
                            dir(path) {
                                echo "Running tests for ${service}"
                                
                                // Set service check to pending if this is a PR
                                if (env.CHANGE_ID) {
                                    try {
                                        githubChecks(
                                            name: "Test Code Coverage - ${service}",
                                            status: 'IN_PROGRESS',
                                            detailsURL: "${env.BUILD_URL}/jacoco/",
                                            output: [
                                                title: 'Code Coverage Analysis Running',
                                                summary: "Running tests for ${service}",
                                                text: 'Tests are in progress...'
                                            ]
                                        )
                                    } catch (Exception e) {
                                        echo "Warning: Failed to create GitHub check: ${e.message}"
                                    }
                                }
                                
                                // Run tests
                                def testResult = sh(script: "mvn clean test", returnStatus: true)
                                
                                // Publish JUnit test results
                                junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                                
                                if (testResult != 0) {
                                    allServicesPass = false
                                    echo "Tests failed for ${service}"
                                    
                                    if (env.CHANGE_ID) {
                                        try {
                                            githubChecks(
                                                name: "Test Code Coverage - ${service}",
                                                status: 'COMPLETED',
                                                conclusion: 'FAILURE',
                                                detailsURL: env.BUILD_URL,
                                                output: [
                                                    title: 'Tests Failed',
                                                    summary: "Tests for ${service} have failed",
                                                    text: 'Please check the build logs for test failure details.'
                                                ]
                                            )
                                        } catch (Exception e) {
                                            echo "Warning: Failed to update GitHub check: ${e.message}"
                                        }
                                    }
                                    
                                    continue
                                }
                                
                                try {
                                    // Publish coverage report using Coverage Plugin
                                    recordCoverage(
                                        tools: [[parser: 'JACOCO', pattern: 'target/site/jacoco/jacoco.xml']],
                                        id: "${service}-coverage", 
                                        name: "${service} - JaCoCo Coverage",
                                        qualityGates: [
                                            [threshold: 70.0, metric: 'LINE']
                                        ]
                                    )
                                    
                                    // Backup using PublishHTML
                                    publishHTML([
                                        allowMissing: true,
                                        alwaysLinkToLastBuild: true,
                                        keepAll: true,
                                        reportDir: 'target/site/jacoco',
                                        reportFiles: 'index.html',
                                        reportName: "${service} - JaCoCo Coverage Report"
                                    ])
                                    
                                    // Check coverage with a safer approach
                                    def coverageScript = """
                                        if [ -f target/site/jacoco/jacoco.csv ]; then
                                            COVERAGE=\$(awk -F"," '{ instructions += \$4 + \$5; covered += \$5 } END { print (covered/instructions) * 100 }' target/site/jacoco/jacoco.csv)
                                            echo "Code coverage: \$COVERAGE%"
                                            
                                            # Use awk for comparison instead of bc
                                            if (( \$(awk 'BEGIN {print (\$COVERAGE < ${MINIMUM_COVERAGE}) ? 1 : 0}') )); then
                                                echo "Coverage below minimum threshold of ${MINIMUM_COVERAGE}%"
                                                echo "\${COVERAGE}" > coverage-result.txt
                                                exit 1
                                            else
                                                echo "Coverage meets minimum threshold of ${MINIMUM_COVERAGE}%"
                                                echo "\${COVERAGE}" > coverage-result.txt
                                            fi
                                        else
                                            echo "No coverage data found, skipping coverage check"
                                            echo "0" > coverage-result.txt
                                        fi
                                    """
                                    def coverageResult = sh(script: coverageScript, returnStatus: true)
                                    
                                    // Read actual coverage for GitHub check
                                    def codeCoverage = sh(script: "cat coverage-result.txt", returnStdout: true).trim()
                                    allCoverageResults[service] = codeCoverage
                                    
                                    // Update GitHub check for code coverage
                                    if (env.CHANGE_ID) {
                                        def conclusion = coverageResult == 0 ? 'SUCCESS' : 'FAILURE'
                                        def coverageFormatted = codeCoverage.indexOf(".") > 0 ? 
                                            codeCoverage.substring(0, codeCoverage.indexOf(".") + 2) : 
                                            codeCoverage
                                            
                                        try {
                                            githubChecks(
                                                name: "Test Code Coverage - ${service}",
                                                status: 'COMPLETED',
                                                conclusion: conclusion,
                                                detailsURL: "${env.BUILD_URL}/jacoco/",
                                                output: [
                                                    title: conclusion == 'SUCCESS' ? 'Code Coverage Check Passed' : 'Code Coverage Check Failed',
                                                    summary: "Coverage must be at least ${MINIMUM_COVERAGE}%. Your coverage of ${service} is ${coverageFormatted}%.",
                                                    text: conclusion == 'SUCCESS' ? 'All tests pass with sufficient coverage.' : 'Increase test coverage and retry the build.'
                                                ]
                                            )
                                        } catch (Exception e) {
                                            echo "Warning: Failed to update GitHub check: ${e.message}"
                                        }
                                    }
                                    
                                    // If coverage below threshold, mark unstable (not fail)
                                    if (coverageResult != 0) {
                                        allServicesPass = false
                                        unstable "Test coverage for ${service} is below the required threshold of ${MINIMUM_COVERAGE}%"
                                    }
                                } catch (Exception e) {
                                    echo "Warning: Coverage reporting failed for ${service}: ${e.message}"
                                    allServicesPass = false
                                    
                                    if (env.CHANGE_ID) {
                                        try {
                                            githubChecks(
                                                name: "Test Code Coverage - ${service}",
                                                status: 'COMPLETED',
                                                conclusion: 'FAILURE',
                                                detailsURL: env.BUILD_URL,
                                                output: [
                                                    title: 'Coverage Check Error',
                                                    summary: "Failed to analyze code coverage for ${service}",
                                                    text: "Error: ${e.message}"
                                                ]
                                            )
                                        } catch (Exception e2) {
                                            echo "Warning: Failed to update GitHub check: ${e2.message}"
                                        }
                                    }
                                }
                            }
                        } else {
                            echo "Path not found for service: ${service}"
                        }
                    }
                    
                    // Create summary report for PR
                    if (env.CHANGE_ID && !allCoverageResults.isEmpty()) {
                        def summaryText = "## Coverage Summary\n\n"
                        summaryText += "| Service | Coverage | Status |\n"
                        summaryText += "|---------|----------|--------|\n"
                        
                        allCoverageResults.each { service, coverage ->
                            def coverageNum = 0
                            try {
                                coverageNum = coverage.toFloat()
                            } catch (Exception e) {
                                coverageNum = 0
                            }
                            
                            def status = coverageNum >= MINIMUM_COVERAGE.toFloat() ? "✅ Pass" : "❌ Fail"
                            summaryText += "| ${service} | ${coverage}% | ${status} |\n"
                        }
                        
                        try {
                            githubChecks(
                                name: "Overall Coverage Summary",
                                status: 'COMPLETED',
                                conclusion: allServicesPass ? 'SUCCESS' : 'FAILURE',
                                detailsURL: env.BUILD_URL,
                                output: [
                                    title: allServicesPass ? 'All Services Pass Coverage Checks' : 'Coverage Checks Failed',
                                    summary: "Minimum required coverage: ${MINIMUM_COVERAGE}%",
                                    text: summaryText
                                ]
                            )
                        } catch (Exception e) {
                            echo "Warning: Failed to create summary check: ${e.message}"
                        }
                    }
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    def SERVICES = readJSON text: env.SERVICES_JSON
                    def servicesToBuild = env.CHANGED_SERVICES.trim().split(" ")
                    def buildSuccess = true
                    
                    servicesToBuild.each { service ->
                        def path = SERVICES[service]
                        if (path) {
                            dir(path) {
                                echo "Building ${service}"
                                
                                // Update GitHub check if this is a PR
                                if (env.CHANGE_ID) {
                                    try {
                                        githubChecks(
                                            name: "Build - ${service}",
                                            status: 'IN_PROGRESS',
                                            detailsURL: env.BUILD_URL,
                                            output: [
                                                title: 'Building Service',
                                                summary: "Building ${service}...",
                                                text: 'Creating deployable artifact.'
                                            ]
                                        )
                                    } catch (Exception e) {
                                        echo "Warning: Failed to create GitHub check: ${e.message}"
                                    }
                                }
                                
                                def buildResult = sh(script: "mvn clean package -DskipTests", returnStatus: true)
                                
                                // Archive the artifacts
                                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: true
                                
                                if (buildResult != 0) {
                                    buildSuccess = false
                                    echo "Build failed for ${service}"
                                }
                                
                                // Update GitHub check for build result
                                if (env.CHANGE_ID) {
                                    def conclusion = buildResult == 0 ? 'SUCCESS' : 'FAILURE'
                                    try {
                                        githubChecks(
                                            name: "Build - ${service}",
                                            status: 'COMPLETED',
                                            conclusion: conclusion,
                                            detailsURL: env.BUILD_URL,
                                            output: [
                                                title: conclusion == 'SUCCESS' ? 'Build Successful' : 'Build Failed',
                                                summary: "${service} build ${conclusion == 'SUCCESS' ? 'completed successfully' : 'failed'}",
                                                text: conclusion == 'SUCCESS' ? 
                                                    "The artifact is ready for deployment." : 
                                                    "The build process encountered errors. Check the logs for details."
                                            ]
                                        )
                                    } catch (Exception e) {
                                        echo "Warning: Failed to update GitHub check: ${e.message}"
                                    }
                                }
                            }
                        } else {
                            echo "Path not found for service: ${service}"
                        }
                    }
                    
                    // Update overall CI status for PR
                    if (env.CHANGE_ID) {
                        try {
                            githubChecks(
                                name: "CI Pipeline",
                                status: 'COMPLETED',
                                conclusion: buildSuccess ? 'SUCCESS' : 'FAILURE',
                                detailsURL: env.BUILD_URL,
                                output: [
                                    title: buildSuccess ? 'CI Pipeline Successful' : 'CI Pipeline Failed',
                                    summary: buildSuccess ? 
                                        "All services built successfully." : 
                                        "One or more services failed to build.",
                                    text: "Check individual service builds for details."
                                ]
                            )
                        } catch (Exception e) {
                            echo "Warning: Failed to update GitHub check: ${e.message}"
                        }
                    }
                }
            }
        }
    }
    
    post {
        always {
            // Clean workspace after build
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        unstable {
            echo 'Pipeline completed but with unstable status (e.g. test coverage below threshold)'
        }
        failure {
            echo 'Pipeline failed!'
            
            // Update overall GitHub check if it failed
            script {
                if (env.CHANGE_ID) {
                    try {
                        githubChecks(
                            name: "CI Pipeline",
                            status: 'COMPLETED',
                            conclusion: 'FAILURE',
                            detailsURL: env.BUILD_URL,
                            output: [
                                title: 'CI Pipeline Failed',
                                summary: "The pipeline encountered errors and could not complete successfully.",
                                text: "Check the build logs for more details about the failure."
                            ]
                        )
                    } catch (Exception e) {
                        echo "Warning: Failed to update GitHub check: ${e.message}"
                    }
                }
            }
        }
    }
}
