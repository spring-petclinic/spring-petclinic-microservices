pipeline {
    agent any

    environment {
        GIT_REPO = 'https://github.com/nghiaz160904/spring-petclinic-microservices.git'
    }

    stages {
        stage('Checkout Code') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'GITHUB_CREDENTIALS', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
                    git branch: 'main', credentialsId: 'GITHUB_CREDENTIALS', url: "https://${GIT_USER}:${GIT_PASS}@github.com/nghiaz160904/spring-petclinic-microservices.git"
                }
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim().split("\n")

                    env.SHOULD_BUILD_CUSTOMERS = changedFiles.any { it.startsWith("customers-service/") } ? "true" : "false"
                    env.SHOULD_BUILD_VETS = changedFiles.any { it.startsWith("vets-service/") } ? "true" : "false"
                    env.SHOULD_BUILD_VISIT = changedFiles.any { it.startsWith("visit-service/") } ? "true" : "false"
                }
            }
        }

        stage('Test Customers Service') {
            when { expression { env.SHOULD_BUILD_CUSTOMERS == "true" } }
            steps {
                sh './mvnw test -pl customers-service'
            }
        }

        stage('Test Vets Service') {
            when { expression { env.SHOULD_BUILD_VETS == "true" } }
            steps {
                sh './mvnw test -pl vets-service'
            }
        }

        stage('Test Visit Service') {
            when { expression { env.SHOULD_BUILD_VISIT == "true" } }
            steps {
                sh './mvnw test -pl visit-service'
            }
        }

        stage('Build Customers Service') {
            when { expression { env.SHOULD_BUILD_CUSTOMERS == "true" } }
            steps {
                sh './mvnw clean package -pl customers-service -DskipTests'
            }
        }

        stage('Build Vets Service') {
            when { expression { env.SHOULD_BUILD_VETS == "true" } }
            steps {
                sh './mvnw clean package -pl vets-service -DskipTests'
            }
        }

        stage('Build Visit Service') {
            when { expression { env.SHOULD_BUILD_VISIT == "true" } }
            steps {
                sh './mvnw clean package -pl visit-service -DskipTests'
            }
        }
    }
}
