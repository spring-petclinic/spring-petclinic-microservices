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
                    def prevCommit = sh(script: "git rev-parse HEAD~1", returnStdout: true).trim()
                    def currCommit = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
                    def changedFiles = sh(
                        script: "git diff --name-only ${prevCommit} ${currCommit}",
                        returnStdout: true
                    ).trim().split('\n')

                    echo "📄 Changed files: ${changedFiles.join(', ')}"

                    def services = ['vets-service', 'visit-service', 'customers-service']
                    def touchedService = services.find { s -> changedFiles.any { it.startsWith(s + '/') } }

                    if (touchedService == null) {
                        echo "🔍 No service directories modified."
                        echo "No service changes detected. Skipping pipeline stages."
                        env.SERVICE = null
                    } else {
                        env.SERVICE = touchedService
                        echo "📦 Changed service: ${env.SERVICE}"
                    }
                }
            }
        }

        stage('Test') {
            when {
                expression { return env.SERVICE != null }
            }
            steps {
                dir("${env.SERVICE}") {
                    sh './mvnw verify'
                }
            }
        }

        stage('Check Coverage') {
            when {
                expression { return env.SERVICE != null }
            }
            steps {
                script {
                    def coverageFile = "${env.WORKSPACE}/${env.SERVICE}/target/site/jacoco/jacoco.xml"
                    def coverage = 0

                    if (fileExists(coverageFile)) {
                        def jacoco = new XmlSlurper().parse(new File(coverageFile))
                        def missed = jacoco.counter.find { it.@type == 'INSTRUCTION' }.@missed.toInteger()
                        def covered = jacoco.counter.find { it.@type == 'INSTRUCTION' }.@covered.toInteger()
                        coverage = (covered * 100) / (missed + covered)
                        echo "📊 Test coverage: ${coverage}%"
                    } else {
                        error "❌ Coverage file not found"
                    }

                    if (coverage < env.MIN_COVERAGE.toInteger()) {
                        error "❌ Coverage below ${env.MIN_COVERAGE}%. Failing build."
                    }
                }
            }
        }

        stage('Build') {
            when {
                expression { return env.SERVICE != null }
            }
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
