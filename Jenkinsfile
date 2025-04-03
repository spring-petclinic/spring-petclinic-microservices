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
                    // Kiểm tra các file thay đổi và chạy lệnh sh trong mỗi service thay đổi
                    servicePath.each { service, path ->
                        if (changedFiles.contains(path)) {
                            echo "Changes detected in ${service}, running tests..."
                            
                            // Chạy trong môi trường node để đảm bảo các lệnh sh được thực thi chính xác
                            node {
                                sh "cd ${path} && mvn clean test"
                                sh "cd ${path} && mvn package"
                            }
                        }
                    }
                }
            }
        }
    }
}
