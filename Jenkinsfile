pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                echo "Building.."
                sh '''
                    echo "test.."
                '''
            }
        }
        stage('Test') {
            steps {
                echo "Testing.."
            }
        }
        stage('Deliver') {
            steps {
                echo 'Deliver....'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying...'
            }
        }
        stage('Cleanup') {
            steps {
                echo 'Cleaning up...'
            }
        }
        stage('Post') {
            steps {
                echo 'Post build actions...'
            }
        }
        stage('Notify') {
            steps {
                echo 'Notifying...'
            }
        }
        stage('Archive') {
            steps {
                echo 'Archiving...'
            }
        }
        stage ('Report') {
            steps {
                echo 'Reporting...'
            }
        }
    }
    post {
        success {
            script {
                githubNotify context: 'CI',
                    status: 'SUCCESS',
                    description: 'Build passed',
                    repo: 'test-project1-devops',
                    account: 'vuden2605',
                    sha: env.GIT_COMMIT,
                    credentialsId: 'test'
            }
        }
        failure {
            script {
                githubNotify context: 'CI',
                    status: 'FAILURE',
                    description: 'Build failed',
                    repo: 'test-project1-devops',
                    account: 'vuden2605',
                    sha: env.GIT_COMMIT,
                    credentialsId: 'test'
            }
        }
}

}
