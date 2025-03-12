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
                            // Run tests and generate coverage reports
                            sh "mvn test surefire-report:report jacoco:report"
        
                            // Publish JUnit test results
                            junit '**/target/surefire-reports/*.xml'
        
                            // Record test coverage using the Coverage plugin
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: '**/target/site/jacoco/jacoco.xml']],
                            )
                            // Extract coverage percentage
                            def coverageXml = readFile('target/site/jacoco/jacoco.xml')
                            def matcher = coverageXml =~ /<counter type="LINE" missed="(\d+)" covered="(\d+)"/
                            
                            if (matcher.find()) {
                                def missed = matcher[0][1].toInteger()
                                def covered = matcher[0][2].toInteger()
                                def coverage = (covered * 100) / (missed + covered)
                                
                                echo "Line Coverage: ${coverage}%"
                                
                                // Fail build if coverage is below threshold
                                if (coverage < 90) {
                                    error "Test coverage is below 90% (${coverage}%), failing the build!"
                                }
                            } else {
                                error "Could not parse coverage report!"
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
