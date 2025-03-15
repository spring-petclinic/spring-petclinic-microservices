pipeline {
    agent any
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        // skipDefaultCheckout(true)
        // skipStagesAfterUnstable()
    }
    environment {
        WORKSPACE = "${env.WORKSPACE}"
        // List of services without test folders
        SERVICES_WITHOUT_TESTS = "spring-petclinic-admin-server spring-petclinic-genai-service"
    }
    stages {
        stage('Detect Changes') {
            steps {
                publishChecks name: 'Detect Changes', status: 'IN_PROGRESS'
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
                publishChecks name: 'Detect Changes', status: 'COMPLETED', conclusion: 'SUCCESS'
            }
            post {
                failure {
                    publishChecks name: 'Detect Changes', status: 'COMPLETED', conclusion: 'FAILURE'
                }
            }
        }
        
        stage('Test Services') {
            when {
                expression { return env.CHANGED_SERVICES != "" }
            }
            steps {
                publishChecks name: 'Test Services', status: 'IN_PROGRESS'
                script {
                    def serviceList = env.CHANGED_SERVICES.trim().split(" ")
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
                }
                publishChecks name: 'Test Services', status: 'COMPLETED', conclusion: currentBuild.result == 'UNSTABLE' ? 'NEUTRAL' : 'SUCCESS'
            }
            post {
                failure {
                    publishChecks name: 'Test Services', status: 'COMPLETED', conclusion: 'FAILURE'
                }
            }
        }
        
        stage('Build Services') {
            when {
                expression { return env.CHANGED_SERVICES != "" }
            }
            steps {
                publishChecks name: 'Build Services', status: 'IN_PROGRESS'
                script {
                    def serviceList = env.CHANGED_SERVICES.trim().split(" ")
                    for (service in serviceList) {
                        echo "Building service: ${service}"
                        dir(service) {
                            sh 'mvn package -DskipTests'
                            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                        }
                    }
                }
                publishChecks name: 'Build Services', status: 'COMPLETED', conclusion: 'SUCCESS'
            }
            post {
                failure {
                    publishChecks name: 'Build Services', status: 'COMPLETED', conclusion: 'FAILURE'
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