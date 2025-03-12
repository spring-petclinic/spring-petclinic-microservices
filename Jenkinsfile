pipeline {
    agent any
    environment {
        MAVEN_HOME = tool 'Maven'
        CHANGED_FILES = ''
        RUN_VETS_SERVICE = 'false'
        RUN_CUSTOMERS_SERVICE = 'false'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    CHANGED_FILES = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim()
                    
                    if (CHANGED_FILES.contains("vets-service/")) {
                        env.RUN_VETS_SERVICE = "true"
                    }
                    if (CHANGED_FILES.contains("customers-service/")) {
                        env.RUN_CUSTOMERS_SERVICE = "true"
                    }
                }
            }
        }

        stage('Test & Build Vets Service') {
            when {
                expression { env.RUN_VETS_SERVICE == 'true' }
            }
            steps {
                dir('vets-service') {
                    sh './mvnw test'
                    sh './mvnw package'
                }
            }
            post {
                always {
                    junit 'vets-service/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Test & Build Customers Service') {
            when {
                expression { env.RUN_CUSTOMERS_SERVICE == 'true' }
            }
            steps {
                dir('customers-service') {
                    sh './mvnw test'
                    sh './mvnw package'
                }
            }
            post {
                always {
                    junit 'customers-service/target/surefire-reports/*.xml'
                }
            }
        }
    }
}
