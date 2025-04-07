pipeline {
    agent any
    
    tools {
        maven 'Maven 3'
    }
    
    environment {
        MINIMUM_COVERAGE = '70'
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timeout(time: 60, unit: 'MINUTES')
        timestamps()
    }
    
    stages {
        stage('Determine Changed Services') {
            steps {
                script {
                    // Định nghĩa SERVICES trong script block
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
                    
                    // Initialize empty list to store changed services
                    env.CHANGED_SERVICES = ""
                    
                    // For pull requests, compare with target branch
                    if (env.CHANGE_ID) {
                        echo "Processing Pull Request #${env.CHANGE_ID}"
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
                    // For direct branch builds, compare with last successful build
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
                    
                    servicesToTest.each { service ->
                        def path = SERVICES[service]
                        if (path) {
                            dir(path) {
                                echo "Running tests for ${service}"
                                sh "mvn clean test"
                                
                                // Publish JUnit test results
                                junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                                
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
                                    
                                    // Kiểm tra độ phủ bằng cách an toàn hơn
                                    def coverageScript = """
                                        if [ -f target/site/jacoco/jacoco.csv ]; then
                                            COVERAGE=\$(awk -F"," '{ instructions += \$4 + \$5; covered += \$5 } END { print (covered/instructions) * 100 }' target/site/jacoco/jacoco.csv)
                                            echo "Code coverage: \$COVERAGE%"
                                            
                                            # Sử dụng awk để so sánh thay vì bc
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
                                    
                                    // Create GitHub check for code coverage
                                    if (env.CHANGE_ID) {
                                        def conclusion = coverageResult == 0 ? 'SUCCESS' : 'FAILURE'
                                        
                                        githubChecks(
                                            name: "Test Code Coverage - ${service}",
                                            status: 'COMPLETED',
                                            conclusion: conclusion,
                                            detailsURL: env.BUILD_URL,
                                            output: [
                                                title: conclusion == 'SUCCESS' ? 'Code Coverage Check Passed' : 'Code Coverage Check Failed',
                                                summary: "Coverage must be at least ${MINIMUM_COVERAGE}%. Your coverage's of ${service} is ${codeCoverage}%.",
                                                text: conclusion == 'SUCCESS' ? 'All tests pass with sufficient coverage.' : 'Increase test coverage and retry the build.'
                                            ]
                                        )
                                    }
                                    
                                    // Nếu độ phủ không đạt, đánh dấu unstable thay vì fail
                                    if (coverageResult != 0) {
                                        unstable "Test coverage for ${service} is below the required threshold of ${MINIMUM_COVERAGE}%"
                                    }
                                } catch (Exception e) {
                                    echo "Warning: Coverage reporting failed for ${service}: ${e.message}"
                                    
                                    if (env.CHANGE_ID) {
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
                                    }
                                }
                            }
                        } else {
                            echo "Path not found for service: ${service}"
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
                    
                    servicesToBuild.each { service ->
                        def path = SERVICES[service]
                        if (path) {
                            dir(path) {
                                echo "Building ${service}"
                                sh "mvn clean package -DskipTests"
                                
                                // Archive the artifacts
                                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: true
                            }
                        } else {
                            echo "Path not found for service: ${service}"
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
        }
    }
}
