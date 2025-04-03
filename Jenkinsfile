pipeline {
    agent any
    
    environment {
        // Chỉ định nghĩa các biến đơn giản trong environment
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
                                sh "mvn clean test jacoco:report"
                                
                                // Publish JUnit test results
                                junit 'target/surefire-reports/*.xml'
                                
                                // Check code coverage and fail if below threshold
                                def coverageResult = sh(
                                    script: """
                                    COVERAGE=\$(awk -F"," '{ instructions += \$4 + \$5; covered += \$5 } END { print 100*covered/instructions }' target/site/jacoco/jacoco.csv)
                                    echo "Code coverage: \$COVERAGE%"
                                    if (( \$(echo "\$COVERAGE < ${MINIMUM_COVERAGE}" | bc -l) )); then
                                        echo "Coverage below minimum threshold of ${MINIMUM_COVERAGE}%"
                                        exit 1
                                    else
                                        echo "Coverage meets minimum threshold of ${MINIMUM_COVERAGE}%"
                                    fi
                                    """,
                                    returnStatus: true
                                )
                                
                                // If coverage check fails, fail the build
                                if (coverageResult != 0) {
                                    error "Test coverage for ${service} is below the required threshold of ${MINIMUM_COVERAGE}%"
                                }
                                
                                // Publish coverage report
                                publishHTML([
                                    allowMissing: false,
                                    alwaysLinkToLastBuild: true,
                                    keepAll: true,
                                    reportDir: 'target/site/jacoco',
                                    reportFiles: 'index.html',
                                    reportName: "${service} - JaCoCo Coverage Report"
                                ])
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
                                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
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
        failure {
            echo 'Pipeline failed!'
        }
    }
}
