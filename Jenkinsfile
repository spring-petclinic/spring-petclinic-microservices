pipeline {
    agent any
    environment {
        SERVICE_CHANGED = ""
    }
    stages {
         stage('Checkout') {
            steps {
                checkout scm  // Checkout mã nguồn từ repository
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def services = [
                        'spring-petclinic-customers-service',
                        'spring-petclinic-vets-service',
                        'spring-petclinic-visits-service'
                    ]

                    // Lấy commit chung giữa HEAD và origin/main
                    def lastCommit = sh(script: '''
                        git fetch origin main
                        git merge-base origin/main HEAD
                    ''', returnStdout: true).trim()

                    // Lấy danh sách file đã thay đổi kể từ commit chung
                    def diff = sh(script: "git diff --name-only ${lastCommit} HEAD", returnStdout: true).trim()

                    // Kiểm tra thay đổi thuộc service nào
                    env.SERVICE_CHANGED = ''
                    for (svc in services) {
                        if (diff.contains("${svc}/")) {
                            env.SERVICE_CHANGED = svc
                            break
                        }
                    }

                    if (env.SERVICE_CHANGED) {
                        echo "Changed service: ${env.SERVICE_CHANGED}"
                    } else {
                        echo "No relevant service changes detected."
                    }
                }
            }
        }

        stage('Test') {
            steps {
                dir("${SERVICE_CHANGED}") {
                    sh 'mvn clean test'
                }
            }
            post {
                always {
                    dir("${SERVICE_CHANGED}") {
                        junit 'target/surefire-reports/*.xml'
                        recordCoverage tools: [jacoco()]
                    }
                }
            }
        }

        stage('Check Coverage') {
            steps {
                script {
                    def coverage = sh(script: "grep -A 1 'INSTRUCTION' ${SERVICE_CHANGED}/target/site/jacoco/index.html | grep -o '[0-9]*%' | head -n1", returnStdout: true).trim().replace('%','').toInteger()
                    if (coverage < 70) {
                        error("Test coverage is below 70%: ${coverage}%")
                    } else {
                        echo "Test coverage OK: ${coverage}%"
                    }
                }
            }
        }

        stage('Build') {
            steps {
                dir("${SERVICE_CHANGED}") {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
    }
}
