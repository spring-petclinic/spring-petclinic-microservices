pipeline {
    agent any

    environment {
        // Ensure Windows system commands can be found
        PATH = "C:\\Windows\\System32;${env.PATH}"
    }

    tools {
        maven "M3" // Ensure this matches your Maven installation configured in Jenkins
    }

    stages {
        stage('Test') {
            steps {
                // Checkout your source code from SCM
                checkout scm
                
                // Run tests and generate JaCoCo reports for all modules.
                // Running from the root POM, Maven will process all modules.
                bat 'mvn -Dmaven.test.failure.ignore=true clean package'
            }
            post {
                always {
                    // Collect test reports from all modules using a recursive wildcard
                    junit '**/target/surefire-reports/TEST-*.xml'
                    
                    // Collect JaCoCo coverage XML reports from each module.
                    // This pattern will find all jacoco.xml files in submodule directories.
                }
            }
        }
        stage('Build') {
            steps {
                // Build the project (this will package all modules)
                bat 'mvn -B package'
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
