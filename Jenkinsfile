pipeline {
    agent any
    
    triggers {
        cron('H/1 * * * *')
    }

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
                echo 'Delivering....'
                sh '''
                echo "doing delivery stuff.."
                '''
            }
        }
    }
}
