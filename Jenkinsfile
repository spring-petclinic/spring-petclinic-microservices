def changedFiles = sh(script: "git diff --name-only HEAD^ HEAD", returnStdout: true).trim()
def servicePath = [
    "customers-service": "customers-service/",
    "vets-service": "vets-service/",
    "visit-service": "visit-service/"
]

pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Test & Build') {
            steps {
                script {
                    servicePath.each { service, path ->
                        if (changedFiles.contains(path)) {
                            echo "Changes detected in ${service}, running tests..."
                            sh "cd ${path} && mvn clean test"
                            sh "cd ${path} && mvn package"
                        }
                    }
                }
            }
        }
    }
}
