pipeline {
    agent any
    environment {
        SERVICE_DIR = ''
    }

    stages {
        stage('Check Changes') {
            steps {
                script {
                    def changedFiles = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim().split('\n')
                    def serviceDirs = ['customers-service', 'vets-service', 'visits-service']
                    
                    for (dir in serviceDirs) {
                        if (changedFiles.any { it.startsWith(dir) }) {
                            SERVICE_DIR = dir
                            break
                        }
                    }
                    
                    if (SERVICE_DIR == '') {
                        echo "No service changes detected. Skipping pipeline."
                        currentBuild.result = 'ABORTED'
                        error("No relevant changes")
                    }
                }
            }
        }

        stage('Test') {
            when { environment name: 'SERVICE_DIR', not: '' }
            steps {
                dir("${SERVICE_DIR}") {
                    sh 'mvn clean test'
                }
            }
        }

        stage('Build') {
            when { environment name: 'SERVICE_DIR', not: '' }
            steps {
                dir("${SERVICE_DIR}") {
                    sh 'mvn clean package'
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: "${SERVICE_DIR}/target/*.jar", fingerprint: true
        }
    }
}
