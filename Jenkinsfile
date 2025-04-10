pipeline {
    agent any

    tools {
        maven 'Maven 3.8.7'  // Đảm bảo Maven đã được cài trong Jenkins
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
                    def changedFiles = sh(script: "git diff --name-only ${prevCommit} ${currCommit}", returnStdout: true).trim().split('\n')
                    def services = ['vets-service', 'visit-service', 'customers-service']
                    env.SERVICE = services.find { s -> changedFiles.any { it.contains("${s}/") } }
                }
            }
        }

        stage('Test') {
            when { expression { return env.SERVICE?.trim() } }
            steps {
                dir("${env.SERVICE}") {
                    sh 'mvn verify'  // Chạy Maven đã cài sẵn trong Jenkins
                }
            }
        }

        stage('Check Coverage') {
            when { expression { return env.SERVICE?.trim() } }
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
            when { expression { return env.SERVICE?.trim() } }
            steps {
                jacoco execPattern: "${env.SERVICE}/target/jacoco.exec",
                       classPattern: "${env.SERVICE}/target/classes",
                       sourcePattern: "${env.SERVICE}/src/main/java",
                       inclusionPattern: '**/*.class',
                       exclusionPattern: '**/*Test*'
            }
        }

        stage('Post comment to PR') {
            when { expression { return env.CHANGE_ID } } // Chỉ khi chạy từ PR
            steps {
                script {
                    def coverageFile = "${env.WORKSPACE}/${env.SERVICE}/target/site/jacoco/jacoco.xml"
                    def coverage = fileExists(coverageFile) ? new XmlSlurper().parse(new File(coverageFile)).counter.find { it.@type == 'INSTRUCTION' }.@covered.toInteger() * 100 / new XmlSlurper().parse(new File(coverageFile)).counter.find { it.@type == 'INSTRUCTION' }.@missed.toInteger() : 0
                    def msg = "### 📊 Code Coverage\n\nTest coverage: ${coverage}%\n\n✔️ Jenkins build successful."

                    // Chỉ cho phép merge khi độ phủ > MIN_COVERAGE
                    if (coverage >= env.MIN_COVERAGE.toInteger()) {
                        githubPrComment comment: msg
                    } else {
                        error "❌ Coverage is below ${env.MIN_COVERAGE}%. Cannot merge."
                    }
                }
            }
        }

        stage('Build') {
            when { expression { return env.SERVICE?.trim() } }
            steps {
                dir("${env.SERVICE}") {
                    sh 'mvn package -DskipTests'
                }
            }
        }
    }

    post {
        success { echo '✅ Build passed, coverage above threshold.' }
        failure { echo '❌ Pipeline failed or coverage below threshold.' }
    }
}
