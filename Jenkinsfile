pipeline {
    agent any

    tools {
        maven 'Maven-3.8.7'
        jdk 'jdk17'
    }

    environment {
        COVERAGE_THRESHOLD = 70
    }

    options {
        timestamps()
        skipDefaultCheckout()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changed Services') {
            steps {
                script {
                    def output = sh(script: "git diff --name-only origin/main...HEAD", returnStdout: true).trim()
                    def changedDirs = output.split("\n").collect { it.tokenize('/')[0] }.unique()
                    def knownServices = ['customers-service', 'vets-service', 'visits-service', 'genai-service']
                    def affected = changedDirs.findAll { knownServices.contains(it) }
                    env.AFFECTED_SERVICES = affected.join(',')
                    echo "Affected services: ${env.AFFECTED_SERVICES}"
                }
            }
        }

        stage('Build & Test Changed Services') {
            when {
                expression { return env.AFFECTED_SERVICES?.trim() }
            }
            steps {
                script {
                    def services = env.AFFECTED_SERVICES.split(',')
                    for (svc in services) {
                        stage("Build & Test ${svc}") {
                            dir("${svc}") {
                                sh "../../mvnw clean test jacoco:report"
                                junit '**/target/surefire-reports/*.xml'

                                def coverage = calculateCoverage("${svc}")
                                echo "Test coverage for ${svc}: ${coverage}%"
                                if (coverage < COVERAGE_THRESHOLD.toInteger()) {
                                    error("Coverage for ${svc} is below ${COVERAGE_THRESHOLD}%")
                                }

                                sh "../../mvnw package -DskipTests"
                            }
                        }
                    }
                }
            }
        }

        stage('Skip - No Changes Detected') {
            when {
                not { expression { return env.AFFECTED_SERVICES?.trim() } }
            }
            steps {
                echo "Don't microservice changed."
            }
        }
    }

    post {
        always {
            echo "Finished pipeline for branch: ${env.BRANCH_NAME}"
        }
        failure {
            echo "Build failed. Check logs and coverage report."
        }
    }
}


def calculateCoverage(serviceName) {
    def reportFile = "${serviceName}/target/site/jacoco/index.html"
    def line = sh(script: "grep -A 1 'Line Coverage' ${reportFile} | grep -oP '[0-9]+(?=%)' | head -n 1", returnStdout: true).trim()
    return line as int
}

