pipeline {
    agent any
    
    tools {
        maven 'Maven 3' // Use Jenkins' built-in Maven
    }
    
    stages {
        stage('Clean Workspace') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def services = [
                            "spring-petclinic-customers-service",
                            "spring-petclinic-vets-service",
                            "spring-petclinic-visits-service",
                            "spring-petclinic-genai-service"]
                    
                    def baseCommit = sh(script: "git rev-parse HEAD^1", returnStdout: true).trim() // First parent of merge commit
                    def changedFiles = sh(script: "git diff --name-only ${baseCommit} HEAD", returnStdout: true).trim().split("\n")
                    echo "Changed files: ${changedFiles.join(', ')}"

                    def changedServices = []
                    for (service in services) {
                        if (changedFiles.any { it.startsWith(service) }) {
                            changedServices.add(service)
                        }
                    }
                    echo "Code changes in services: ${changedServices.join(', ')}"
                    env.CHANGED_SERVICES = changedServices.join(', ')                             
                }
            }
        }

        stage('Test Services') {
            when {
                expression { env.CHANGED_SERVICES != '' }
            }
            steps {
                script {
                    def changedServices = env.CHANGED_SERVICES.split(',')
                    changedServices.each { service -> 
                        echo "Testing service: ${service}"
                        dir("${env.WORKSPACE}/${service}") {  // Use absolute workspace path
                            sh "ls -l"  // Debugging: Check if pom.xml exists
                            sh "mvn clean test surefire-report:report jacoco:report"
                            
                            // Display test results
                            sh "cat target/surefire-reports/*.txt || true"

                            // Publish JUnit test results
                            junit '**/target/surefire-reports/*.xml'

                            // Record test coverage
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: '**/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', baseline: 'PROJECT', unstable: false]
                                ]
                            )

                            // Ensure tests don't fail due to missing files
                            echo "Current build result: ${currentBuild.result}"

                            if (currentBuild.result == 'UNSTABLE' || currentBuild.result == 'FAILURE') {
                                error "Test stage failed due to test failures!"
                            }
                        }
                    }
                }
            }
        }

        stage('Build Services') {
            when {
                expression { env.CHANGED_SERVICES != '' }
            }
            steps {
                script {
                    def changedServices = env.CHANGED_SERVICES.split(',')
                    changedServices.each{ service -> 
                        echo "Building service: ${service}"
                        dir("${service}") {
                            sh "mvn clean package -DskipTests"
                        }
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo "Pipeline execution completed!"
        }
    }
}
