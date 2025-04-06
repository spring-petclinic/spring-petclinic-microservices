pipeline {
    agent any
    environment {
        SERVICE_CHANGED = ""
    }
    stages {
        stage('Detect Changes') {
            steps {
                script {
                    def diff = sh(script: 'git diff --name-only origin/main', returnStdout: true).trim()
                    if (diff.contains('spring-petclinic-customers-service/')) {
                        env.SERVICE_CHANGED = 'spring-petclinic-customers-service'
                    } else if (diff.contains('spring-petclinic-vets-service/')) {
                        env.SERVICE_CHANGED = 'spring-petclinic-vets-service'
                    } else if (diff.contains('spring-petclinic-visits-service/')) {
                        env.SERVICE_CHANGED = 'spring-petclinic-visits-service'
                    } else {
                        error "No relevant service changes detected."
                    }
                    echo "Changed service: ${env.SERVICE_CHANGED}"
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
