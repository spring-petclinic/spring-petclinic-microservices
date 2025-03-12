def changedFiles = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()
def shouldBuildCustomers = changedFiles.contains("customers-service/")
def shouldBuildVets = changedFiles.contains("vets-service/")
def shouldBuildVisit = changedFiles.contains("visit-service/")

pipeline {
    agent any
    stages {
        stage('Checkout Code') {
            steps {
                git 'https://github.com/<USERNAME>/spring-petclinic-microservices.git'
            }
        }

        stage('Test Customers Service') {
            when {
                expression { shouldBuildCustomers }
            }
            steps {
                sh './mvnw test -pl customers-service'
            }
        }

        stage('Test Vets Service') {
            when {
                expression { shouldBuildVets }
            }
            steps {
                sh './mvnw test -pl vets-service'
            }
        }

        stage('Test Visit Service') {
            when {
                expression { shouldBuildVisit }
            }
            steps {
                sh './mvnw test -pl visit-service'
            }
        }

        stage('Build Customers Service') {
            when {
                expression { shouldBuildCustomers }
            }
            steps {
                sh './mvnw clean package -pl customers-service -DskipTests'
            }
        }

        stage('Build Vets Service') {
            when {
                expression { shouldBuildVets }
            }
            steps {
                sh './mvnw clean package -pl vets-service -DskipTests'
            }
        }

        stage('Build Visit Service') {
            when {
                expression { shouldBuildVisit }
            }
            steps {
                sh './mvnw clean package -pl visit-service -DskipTests'
            }
        }
    }
}
