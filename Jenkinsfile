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
                sh 'make test'  // Lệnh build hoặc test của bạn
            }
        }
    }
    post {
        success {
            setGithubStatus('success', 'Build Passed!')
        }
        failure {
            setGithubStatus('failure', 'Build Failed!')
        }
    }
}

def setGithubStatus(state, message) {
    step([
        $class: 'GitHubCommitStatusSetter',
        context: 'ci/build',
        statusBackref: '',
        statusResultSource: [
            $class: 'ConditionalStatusResultSource',
            results: [[status: state, message: message]]
        ]
    ])
}
