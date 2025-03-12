pipeline {
    agent none

    environment {
        GITHUB_CREDENTIALS_ID = 'github-token'
        MAIN_BRANCH = 'main'
    }

    stages {
        stage('Detect Changes') {
            // agent { label 'jenkins-agent' }
            steps {
                script {
                    def changedFiles = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim().split("\n")
                    def services = ["customers-service", "vets-service", "visit-service"]
                    def affectedServices = services.findAll { service -> 
                        changedFiles.any { it.startsWith(service) }
                    }
                    if (affectedServices.isEmpty()) {
                        echo "No relevant changes detected, skipping pipeline"
                        currentBuild.result = 'ABORTED'
                        return
                    }
                    env.AFFECTED_SERVICES = affectedServices.join(",")
                }
            }
        }

        stage('Run Tests') {
            // agent { label 'jenkins-agent' }
            steps {
                script {
                    def services = env.AFFECTED_SERVICES.split(",")
                    services.each { service ->
                        echo "Running tests for ${service}"
                        dir(service) {
                            sh "./mvnw test"
                        }
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    jacoco execPattern: '**/target/jacoco.exec'
                }
            }
        }

        stage('Validate Coverage') {
            // agent { label 'jenkins-agent' }
            steps {
                script {
                    def coverage = sh(script: "grep -oP 'TOTAL.*\\K\\d+%' target/site/jacoco/index.html | tr -d '%'", returnStdout: true).trim().toInteger()
                    if (coverage < 70) {
                        error "Test coverage below threshold: ${coverage}%"
                    }
                }
            }
        }

        stage('Merge to Main') {
            // agent { label 'jenkins-agent' }
            steps {
                script {
                    withCredentials([string(credentialsId: env.GITHUB_CREDENTIALS_ID, variable: 'GITHUB_TOKEN')]) {
                        sh """
                            git config --global user.email "jenkins@example.com"
                            git config --global user.name "Jenkins CI"
                            git checkout ${env.MAIN_BRANCH}
                            git merge --no-ff ${env.GIT_BRANCH}
                            git push origin ${env.MAIN_BRANCH}
                        """
                    }
                }
            }
        }

        stage('Build & Deploy') {
            // agent { label 'jenkins-agent' }
            steps {
                script {
                    def services = env.AFFECTED_SERVICES.split(",")
                    services.each { service ->
                        echo "Building and deploying ${service}"
                        dir(service) {
                            sh "./mvnw package"
                        }
                    }
                }
            }
        }
    }
}
