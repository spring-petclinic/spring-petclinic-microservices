pipeline {
    agent any

    tools {
        maven 'maven3.9.9'
    }

    options {
        skipDefaultCheckout()
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    checkout scm
                }
            }
        }
        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()
                    echo "Changed files:\n${changedFiles}"

                    env.BUILD_VETS = changedFiles.contains("vets-service/")
                    env.BUILD_VISITS = changedFiles.contains("visits-service/")
                    env.BUILD_CUSTOMERS = changedFiles.contains("customers-service/")
                    echo "BUILD_VETS: ${env.BUILD_VETS}"
                    echo "BUILD_VISITS: ${env.BUILD_VISITS}"
                    echo "BUILD_CUSTOMERS: ${env.BUILD_CUSTOMERS}"
                }

            }
        }

        stage('Build & Test Vets Service') {
            when {
                expression { env.BUILD_VETS == "true" }
            }
            steps {
                dir('spring-petclinic-vets-service') {
                    sh "mvn clean package -DskipTests"
                    sh "mvn test"
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build & Test Visits Service') {
            when {
                expression {
                        return (SERVICE == 'spring-petclinic-vets-service' && env.BUILD_VETS == "true") ||
                               (SERVICE == 'spring-petclinic-visits-service' && env.BUILD_VISITS == "true") ||
                               (SERVICE == 'spring-petclinic-customers-service' && env.BUILD_CUSTOMERS == "true")
                }
            }
            steps {
                dir('spring-petclinic-visits-service') {
                    sh "mvn clean package -DskipTests"
                    sh "mvn test"
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build & Test Customers Service') {
            when {
                expression { env.BUILD_CUSTOMERS == "true" }
            }
            steps {
                dir('spring-petclinic-customers-service') {
                    sh "mvn clean package -DskipTests"
                    sh "mvn test verify"
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        stage('Publish Coverage') {
            steps {
                jacoco(
                    execPattern: '**/target/jacoco.exec',
                    classPattern: '**/target/classes',
                    sourcePattern: '**/src/main/java',
                    inclusionPattern: '**/*.class',
                    exclusionPattern: '**/*Test.class',
                    minimumInstructionCoverage: '70',
                    minimumBranchCoverage: '70'                )
            }
        }
    }

    post {
        always {
            echo "Pipeline completed!"
        }
    }
}