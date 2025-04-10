pipeline {
    agent any

    tools {
        maven 'Maven 3.8.7'
    }

    environment {
        MIN_COVERAGE = 70
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

       stage('Detect Changed Service') {
            steps {
                script {
                    def changedFiles = sh(
                        script: "git diff-tree --no-commit-id --name-only -r HEAD",
                        returnStdout: true
                    ).trim().split('\n')
        
                    def services = ['vets-service', 'visit-service', 'customers-service']
                    def touchedService = services.find { s -> changedFiles.any { it.startsWith(s + '/') } }
        
                    if (touchedService == null) {
                        error "No changes detected in services. Aborting pipeline."
                    }
        
                    env.SERVICE = touchedService
                    echo "📦 Changed service: ${env.SERVICE}"
                }
            }
        }



        stage('Test') {
            steps {
                dir("${env.SERVICE}") {
                    sh './mvnw verify'
                }
            }
        }

        stage('Check Coverage') {
            steps {
                script {
                    def coverageFile = "${env.WORKSPACE}/${env.SERVICE}/target/site/jacoco/jacoco.xml"
                    def coverage = 0

                    if (fileExists(coverageFile)) {
                        def jacoco = new XmlSlurper().parse(new File(coverageFile))
                        def missed = jacoco.counter.find { it.@type == 'INSTRUCTION' }.@missed.toInteger()
                        def covered = jacoco.counter.find { it.@type == 'INSTRUCTION' }.@covered.toInteger()
                        coverage = (covered * 100) / (missed + covered)
                        echo "Test coverage: ${coverage}%"
                    } else {
                        error "Coverage file not found"
                    }

                    if (coverage < env.MIN_COVERAGE.toInteger()) {
                        error "Coverage below ${env.MIN_COVERAGE}%. Failing build."
                    }
                }
            }
        }

        stage('Build') {
            steps {
                dir("${env.SERVICE}") {
                    sh './mvnw package -DskipTests'
                }
            }
        }
    }

    post {
        success {
            echo '✅ Build, test, and coverage passed.'
        }
        failure {
            echo '❌ Pipeline failed.'
        }
    }
}
