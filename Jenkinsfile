pipeline {
    agent any
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }
    environment {
        // Uncomment and set these values
        GITHUB_OWNER = "@Akerman0509"
        GITHUB_REPO  = "spring-petclinic-microservices"
        BRANCH_NAME  = "${env.GIT_BRANCH.replaceFirst(/^origin\//, '')}"
    }

    stages {
        stage('Build') {
            steps {
                echo 'Building project...'
                sh 'echo Build success!' // Replace with actual build command
            }
        }

        stage('Test') {
            steps {
                echo 'Running tests...'
                withChecks(name: 'Tests', includeStage: true) {
                    sh 'echo All tests passed!' // Replace with actual test command
                }
            }
        }

        stage('Publish Check') {
            steps {
                script {
                    publishChecks name: 'Jenkins Build', 
                                 title: 'Jenkins Build', 
                                 summary: 'Build and tests completed successfully',
                                 conclusion: 'SUCCESS', 
                                 detailsURL: "${env.BUILD_URL}"
                }
            }
        }
    }
}