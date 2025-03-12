pipeline {
    agent any
    environment {
        MAVEN_HOME = tool 'Maven'
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
                    def changes = []
                    if (env.CHANGE_TARGET) {
                        changes = sh(script: "git diff --name-only origin/${env.CHANGE_TARGET}...", returnStdout: true).trim().split('\n')
                    } else {
                        changes = sh(script: "git diff --name-only HEAD^", returnStdout: true).trim().split('\n')
                    }

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

                    boolean rootPomChanged = changes.any { it == 'pom.xml' }
                    boolean sharedResourcesChanged = changes.any { change ->
                        change.startsWith('docker/') || 
                        change.startsWith('scripts/') || 
                        change.startsWith('.mvn/') ||
                        change == 'docker-compose.yml'
                    }

                    if (rootPomChanged || sharedResourcesChanged) {
                        echo "Shared resources changed. Building all services."
                        services.each { serviceKey, servicePath -> servicesToBuild[serviceKey] = true }
                    } else {
                        services.each { serviceKey, servicePath ->
                            if (changes.any { it.startsWith("${servicePath}/") }) {
                                servicesToBuild[serviceKey] = true
                                echo "Will build ${serviceKey} due to changes in ${servicePath}"
                            }
                        }
                    }

                    env.NO_SERVICES_TO_BUILD = servicesToBuild.isEmpty() ? 'true' : 'false'
                    env.SERVICES_TO_BUILD = servicesToBuild.keySet().join(',')

                    if (env.NO_SERVICES_TO_BUILD == 'true') {
                        echo "No service changes detected. Skipping build and test."
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
                                sh "../mvnw clean test verify -Pcoverage"
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
                                sh "../mvnw clean package -DskipTests"
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
