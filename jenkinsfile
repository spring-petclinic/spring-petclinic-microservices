pipeline {
    agent any

    // parameters {
    //     choice(name: 'SERVICE', choices: ['customers-service' , 'genai-service', 'vets-service'], description: 'Select microservice to build')
    // }
    def branchName = env.BRANCH_NAME.replace('feature/', '')

    parameters {
        string(name: 'SERVICE', defaultValue: branchName, description: 'Branch name')
    }

    stages {
        stage('Checkout Code') {
            steps {
                script {
                    echo "Checking out code from branch: ${env.BRANCH_NAME}"
                    checkout scm
                }
            }
        }

        stage('Setup Environment') {
            steps {
                sh 'sudo dnf install -y java-17-openjdk maven'
                sh 'sudo dnf install maven -y'
            }
        }

        stage('Run Unit Tests') {
            steps {
                script {
                    echo "Running tests for ${params.SERVICE}"
                    sh "./mvnw -pl spring-petclinic-${params.SERVICE} test"
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    jacoco execPattern: '**/target/jacoco.exec'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "./mvnw clean install -pl spring-petclinic-${params.SERVICE}"
            }
        }
    }

    post {
        success { echo "Pipeline completed successfully!" }
        failure { echo "Pipeline failed!" }
    }
}
