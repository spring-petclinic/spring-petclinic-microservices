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
                    def changedFiles = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim().split("\n")
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
                    changedServices.each{ service -> 
                        echo "Testing service: ${service}"
                        dir("${service}") {
                            // Run tests and generate JaCoCo report dynamically
                            sh '''
                                mvn clean test \
                                org.jacoco:jacoco-maven-plugin:0.8.10:prepare-agent \
                                org.jacoco:jacoco-maven-plugin:0.8.10:report
                            '''
        
                            // Publish JUnit test results
                            junit '**/target/surefire-reports/*.xml'
        
                            // Archive JaCoCo coverage report for later use
                            archiveArtifacts artifacts: '**/target/site/jacoco/*', fingerprint: true
        
                            // Manually register the coverage report
                            script {
                                def coverageReport = "**/target/site/jacoco/jacoco.xml"
                                if (fileExists(coverageReport)) {
                                    recordCoverage(
                                        tools: [[$class: 'JacocoReportAdapter', path: coverageReport]],
                                        sourceFileResolver: sourceFiles('NEVER_STORE')
                                    )
                                } else {
                                    echo "Coverage report not found, skipping upload."
                                }
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
