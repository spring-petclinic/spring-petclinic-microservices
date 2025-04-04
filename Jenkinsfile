pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    def changedFiles = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()
                    echo "Changed files: ${changedFiles}"
                }
            }
        }
        stage('Determine Service') {
            steps {
                script {
                    def changedFiles = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()
                    
                    if (changedFiles.contains('customers-service/')) {
                        env.SERVICE = 'customers-service'
                    } else if (changedFiles.contains('vets-service/')) {
                        env.SERVICE = 'vets-service'
                    } else if (changedFiles.contains('visit-service/')) {
                        env.SERVICE = 'visit-service'
                    } else {
                        env.SERVICE = 'none'
                    }
                }
            }
        }
        stage('Test') {
            when {
                expression { env.SERVICE != 'none' }
            }
            steps {
                // sh "cd ${env.SERVICE} && mvn test"
                echo "testing... ${env.SERVICE}"
            }
            // post {
            //     // always {
            //     //     junit "${env.SERVICE}/target/surefire-reports/*.xml"
            //     //     jacoco execPattern: "${env.SERVICE}/target/jacoco.exec"
            //     // }
            //     echo "end testing..."
            // }
        }
        stage('Build') {
            when {
                expression { env.SERVICE != 'none' }
            }
            steps {
                // sh "cd ${env.SERVICE} && mvn package"
                echo "building... ${env.SERVICE}"
            }
        }
    }
}
