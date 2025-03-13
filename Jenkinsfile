pipeline {
    agent any
    
    environment {
        SERVICE_CHANGED = ''
    }
    
    stages {
        stage('Check Changes') {
            steps {
                script {
                    def changedFiles = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim().split("\n")
                    def services = ['customers-service', 'vets-service', 'visits-service', 'api-gateway']
                    for (service in services) {
                        if (changedFiles.any { it.startsWith(service + '/') }) {
                            SERVICE_CHANGED = service
                            break
                        }
                    }
                    if (SERVICE_CHANGED == '') {
                        echo "No relevant changes detected. Skipping pipeline."
                        currentBuild.result = 'ABORTED'
                        return
                    }
                    echo "Changes detected in service: ${SERVICE_CHANGED}"
                }
            }
        }
        
        stage('Test') {
            when {
                expression { return env.SERVICE_CHANGED != '' }
            }
            steps {
                dir("${SERVICE_CHANGED}") {
                    sh './mvnw test'
                }
            }
            post {
                always {
                    junit "${SERVICE_CHANGED}/target/surefire-reports/*.xml"
                }
            }
        }
        
        stage('Build') {
            when {
                expression { return env.SERVICE_CHANGED != '' }
            }
            steps {
                dir("${SERVICE_CHANGED}") {
                    sh './mvnw package -DskipTests'
                }
            }
        }
    }
    
    post {
        success {
            echo "Pipeline completed successfully for service: ${SERVICE_CHANGED}"
        }
        failure {
            echo "Pipeline failed for service: ${SERVICE_CHANGED}"
        }
    }
}
