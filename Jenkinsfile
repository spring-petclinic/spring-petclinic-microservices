pipeline {
    agent any
    environment {
        MAVEN_OPTS = "-Dmaven.repo.local=.m2/repository"
    }
    options {
        skipDefaultCheckout()
    }
    stages {
        stage('Checkout') {
            steps {
                script {
                    def previousCommit = sh(script: "git rev-parse HEAD~1", returnStdout: true).trim()
                    def changedFiles = sh(script: "git diff --name-only ${previousCommit}", returnStdout: true).trim().split('\n')

                    def services = ['customers-service', 'vets-service', 'visits-service', 'api-gateway', 'config-server', 'discovery-server']
                    def affectedServices = services.findAll { service ->
                        changedFiles.any { it.startsWith("${service}/") }
                    }

                    if (affectedServices.isEmpty()) {
                        echo "No services affected, skipping pipeline."
                        currentBuild.result = 'SUCCESS'
                        return
                    }

                    env.AFFECTED_SERVICES = affectedServices.join(' ')
                    echo "Affected services: ${env.AFFECTED_SERVICES}"
                }
            }
        }

        stage('Test') {
            when {
                expression { env.AFFECTED_SERVICES != null && env.AFFECTED_SERVICES.trim() != '' }
            }
            steps {
                script {
                    env.AFFECTED_SERVICES.split(' ').each { service ->
                        dir(service) {
                            echo "Running tests for ${service}"
                            sh 'mvn test'

                            // Debug: Kiểm tra thư mục test result
                            sh 'ls -l target/surefire-reports/ || echo "No test reports found"'

                            // Thu thập kết quả test
                            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'

                            // Thu thập độ phủ code
                            jacoco execPattern: '**/target/jacoco.exec'
                        }
                    }
                }
            }
        }

        stage('Build') {
            when {
                expression { env.AFFECTED_SERVICES != null && env.AFFECTED_SERVICES.trim() != '' }
            }
            steps {
                script {
                    env.AFFECTED_SERVICES.split(' ').each { service ->
                        dir(service) {
                            echo "Building ${service}"
                            sh 'mvn package -DskipTests'
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            // Archive test result để Jenkins UI có thể hiển thị
            archiveArtifacts artifacts: '**/target/surefire-reports/*.xml', allowEmptyArchive: true
            // Đảm bảo JUnit luôn post test result lên Jenkins
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
    }
}