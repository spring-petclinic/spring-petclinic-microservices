pipeline {
    agent any
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }
    // environment {
    //     GITHUB_OWNER = "your-github-username-or-org"
    //     GITHUB_REPO  = "your-repo-name"
    //     BRANCH_NAME  = "main"
    // }

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
                sh 'echo All tests passed!' // Replace with actual test command
            }
        }

        stage('Publish Check') {
            steps {
                script {
                    publishChecks name: 'Jenkins Build', conclusion: 'SUCCESS', detailsURL: "${env.BUILD_URL}"
                }
            }
        }
    }
}
