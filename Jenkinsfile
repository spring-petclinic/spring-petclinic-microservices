pipeline {
    agent any

    tools {
        maven 'Maven 3.8.7'  // Đảm bảo Maven được cài sẵn trên Jenkins
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

                    // Tìm service nào xuất hiện trong đường dẫn file
                    def touchedService = services.find { s ->
                        changedFiles.any { it.contains("${s}/") }
                    }

                    if (touchedService == null) {
                        echo "🔍 No service directories modified."
                        echo "No service changes detected. Skipping pipeline stages."
                        env.SERVICE = ''
                    } else {
                        env.SERVICE = touchedService
                        echo "📦 Changed service: ${env.SERVICE}"
                    }
                }
            }
        }

        stage('Test') {
            when {
                expression { return env.SERVICE?.trim() }
            }
            steps {
                dir("${env.SERVICE}") {
                    sh 'mvn verify'  // Thay đổi từ ./mvnw verify thành mvn verify
                }
            }
        }

        stage('Check Coverage') {
            when {
                expression { return env.SERVICE?.trim() }
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

        stage('Publish Coverage Report') {
            when {
                expression { return env.SERVICE?.trim() }
            }
            steps {
                jacoco execPattern: "${env.SERVICE}/target/jacoco.exec",
                       classPattern: "${env.SERVICE}/target/classes",
                       sourcePattern: "${env.SERVICE}/src/main/java",
                       inclusionPattern: '**/*.class',
                       exclusionPattern: '**/*Test*'
            }
        }

        stage('Build') {
            when {
                expression { return env.SERVICE?.trim() }
            }
            steps {
                dir("${env.SERVICE}") {
                    sh 'mvn package -DskipTests'  // Thay đổi từ ./mvnw package -DskipTests thành mvn package -DskipTests
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
