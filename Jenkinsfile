pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                echo "Building.."
                sh '''
                    echo "doing build stuff.."
                '''
            }
        }
        stage('Test') {
            steps {
                echo "Testing.."
                sh '''
                    echo "doing test stuff.."
                '''
            }
        }
        stage('Deliver') {
            steps {
                echo 'Deliver....'
                sh '''
                    echo "doing delivery stuff.."
                '''
            }
        }
        stage('for the fix branch') {
          when {
            branch "fix-*"
          }
          steps {
            sh '''
              This only runs for the fix-* branches
            '''
          }
        }
        stage('for the PR') {
          when {
            branch "PR-*"
          }
          steps {
            sh '''
              This only runs for the PRs
            '''
          }
        }
    }
}
