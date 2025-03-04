pipeline {
    agent any

    environment {
        // Ensure Windows system commands can be found
        PATH = "C:\\Windows\\System32;${env.PATH}"
    }

    tools {
        maven "M3"
    }

    stages {
        stage('Build') {
            steps {
                // Checkout code from your Git repository
                checkout scm
                // Use bat to run Maven (this now should find cmd.exe)
                bat "mvn -B package --file pom.xml"
            }
        }
    }

    post {
        success {
            echo 'Build succeeded!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}
