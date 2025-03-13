pipeline {
    agent any
    environment {
        MAVEN_OPTS = "-Dmaven.repo.local=.m2/repository"
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Detect Changes') {
            steps {
                script {
                    // Get all changed files
                    def changes = []
                    if (env.CHANGE_TARGET) {
                        // If this is a PR build
                        changes = sh(script: "git diff --name-only origin/${env.CHANGE_TARGET}...", returnStdout: true).trim().split('\n')
                    } else {
                        // If this is a branch build
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
                    env.SERVICES_TO_BUILD.split(',').each { service ->
                        dir("spring-petclinic-${service}") {
                            echo "Testing ${service}..."
                            try {
                                // Run tests with JaCoCo coverage for specific service
                                sh """
                                    echo "Running tests for ${service}"
                                    ../mvnw clean test verify -Pcoverage
                                """
                            } catch (Exception e) {
                                echo "Tests failed for ${service}"
                                throw e
                            }
                        }
                    }
                }
            }
            post {
                always {
                    script {
                        // Publish test results and coverage for changed services
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
    }
    post {
        always {
            cleanWs()
        }
    }
}