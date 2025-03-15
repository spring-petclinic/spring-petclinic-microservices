pipeline {
    agent any
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }
    environment {
        WORKSPACE = "${env.WORKSPACE}"
        SERVICES_WITHOUT_TESTS = "spring-petclinic-admin-server spring-petclinic-genai-service"
    }
    stages {
        stage('Detect Changes') {
            steps {
                script {
                    echo "Running pipeline for Branch : ${env.BRANCH_NAME}"
                    def changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim()
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
                    env.CHANGED_SERVICES = ""
                    for (service in services) {
                        if (changedFiles.contains(service)) {
                            env.CHANGED_SERVICES = env.CHANGED_SERVICES + " " + service
                        }
                    }
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
                    for (service in serviceList) {
                        withChecks(name: "Test: ${service}", includeStage: true) {
                            echo "Testing service: ${service}"
                            dir(service) {
                                if (!env.SERVICES_WITHOUT_TESTS.contains(service)) {
                                    try {
                                        sh 'mvn clean test'
                                        junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
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
                    for (service in serviceList) {
                        withChecks(name: "Build: ${service}", includeStage: true) {
                            echo "Building service: ${service}"
                            dir(service) {
                                sh 'mvn package -DskipTests'
                                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
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
