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
        stage ('Deploy') {
            steps {
                echo 'Deploying...'
            }
        }
        stage ('Cleanup') {
            steps {
                echo 'Cleaning up...'
            }
        }
        stage ('Post') {
            steps {
                echo 'Post build actions...'
            }
        }
        stage ('Notify') {
            steps {
                echo 'Notifying...'
            }
        }
        stage ('Archive') {
            steps {
                echo 'Archiving...'
            }
        }
        stage ('Report') {
            steps {
                echo 'Reporting...'
            }
        }
        stage ('Approval') {
            steps {
                echo 'Approval...'
            }
        }
        post {
            success {
                githubNotify context: 'jenkins-ci', status: 'SUCCESS', description: 'CI Passed'
            }
            failure {
                githubNotify context: 'jenkins-ci', status: 'FAILURE', description: 'CI Failed'
            }
        }
    }
}
