pipeline {
    agent any
    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }
        stage('Build & Test') {
            steps {
                sh './mvnw verify'  // Chạy test Maven
            }
        }
    }
    post {
        success {
            githubCheck('success', 'CI Passed ✅')
        }
        failure {
            githubCheck('failure', 'CI Failed ❌')
        }
    }
}

def githubCheck(state, message) {
    step([
        $class: 'GitHubChecksPublisher',
        name: 'Jenkins CI',  // Tên check
        status: 'COMPLETED',
        conclusion: state,
        output: [
            title: 'CI/CD Pipeline',
            summary: message
        ]
    ])
}
