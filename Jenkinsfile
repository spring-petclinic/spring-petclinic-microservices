pipeline {
    agent any
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }
    environment {
        WORKSPACE = "${env.WORKSPACE}"
        // List of services without test folders
        SERVICES_WITHOUT_TESTS = "spring-petclinic-admin-server spring-petclinic-genai-service"
    }
    stages {
        stage('Detect Changes') {
            steps {
                script {
                    // print branch name
                    echo "Running pipeline for Branch : ${env.BRANCH_NAME}"

                    // Get changed files between current and previous commit
                    def changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim()
                    // Define service directories to monitor
                    def services = [
                        'spring-petclinic-admin-server',
                        'spring-petclinic-api-gateway',
                        'spring-petclinic-config-server',
                        'spring-petclinic-customers-service',
                        'spring-petclinic-discovery-server',
                        'spring-petclinic-genai-service',
                        'spring-petclinic-vets-service',
                        'spring-petclinic-visits-service'
                    ]
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
                            env.CHANGED_SERVICES = services.join(" ")
                        } else {
                            echo "No relevant changes detected"
                        }
                    }
                    
                    echo "Services to build: ${env.CHANGED_SERVICES}"
                }
            }
        }
        stage('Test Services') {
            when {
                expression { return env.CHANGED_SERVICES != "" }
            }
            steps {
                script {
                    def serviceList = env.CHANGED_SERVICES.trim().split(" ")
                    
                    // Create a GitHub check for the test stage
                    checkout([$class: 'GitSCM', branches: [[name: env.GIT_COMMIT]]])
                    def checkRunName = "Test Services"
                    def checkRun = githubChecks name: checkRunName,
                                              status: 'in_progress',
                                              summary: "Running tests for services: ${env.CHANGED_SERVICES}"
                    
                    try {
                        for (service in serviceList) {
                            echo "Testing service: ${service}"
                            dir(service) {
                                // Check if the service has tests
                                if (!env.SERVICES_WITHOUT_TESTS.contains(service)) {
                                    try {
                                        sh 'mvn clean test'
                                        
                                        // Publish test results
                                        junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                                        
                                        // Publish coverage reports
                                        jacoco(
                                            execPattern: '**/target/jacoco.exec',
                                            classPattern: '**/target/classes',
                                            sourcePattern: '**/src/main/java',
                                            exclusionPattern: '**/src/test*'
                                        )
                                    } catch (Exception e) {
                                        echo "Warning: Tests failed for ${service}, but continuing pipeline"
                                        currentBuild.result = 'UNSTABLE'
                                    }
                                } else {
                                    echo "Skipping tests for ${service} as it does not have test folders"
                                }
                            }
                        }
                        
                        checkRun.conclusion = 'success'
                        checkRun.summaryText = "All tests completed successfully"
                    } catch (Exception e) {
                        checkRun.conclusion = 'failure'
                        checkRun.summaryText = "Tests failed: ${e.message}"
                        throw e
                    } finally {
                        checkRun.status = 'completed'
                        checkRun.text = "Detailed test results for the following services: ${env.CHANGED_SERVICES}"
                        checkRun.publish()
                    }
                }
            }
        }
        
        stage('Build Services') {
            when {
                expression { return env.CHANGED_SERVICES != "" }
            }
            steps {
                script {
                    def serviceList = env.CHANGED_SERVICES.trim().split(" ")
                    
                    // Create a GitHub check for the build stage
                    def checkRunName = "Build Services"
                    def checkRun = githubChecks name: checkRunName,
                                              status: 'in_progress',
                                              summary: "Building services: ${env.CHANGED_SERVICES}"
                    
                    try {
                        for (service in serviceList) {
                            echo "Building service: ${service}"
                            dir(service) {
                                sh 'mvn package -DskipTests'
                                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                            }
                        }
                        
                        checkRun.conclusion = 'success'
                        checkRun.summaryText = "All services built successfully"
                    } catch (Exception e) {
                        checkRun.conclusion = 'failure'
                        checkRun.summaryText = "Build failed: ${e.message}"
                        throw e
                    } finally {
                        checkRun.status = 'completed'
                        checkRun.text = "Build results for services: ${env.CHANGED_SERVICES}"
                        checkRun.publish()
                    }
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}