pipeline {
    agent any

    environment {
        // GITHUB_OWNER = "your-github-username-or-org"
        // GITHUB_REPO  = "your-repo-name"
        // BRANCH_NAME  = "main"
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
                sh 'echo All tests passed!' // Replace with actual test command
            }
        }

        stage('Publish Check') {
            steps {
                script {
                    withChecks('Jenkins Build and Test') {
                        githubNotify context: 'Jenkins', status: 'SUCCESS', description: 'Build & Tests Passed'
                    }
                }
            }
        }
    }
}
