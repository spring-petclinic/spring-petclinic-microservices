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
                    node { // Đảm bảo sh chạy trong một node hợp lệ
                        def changedFiles = sh(script: "git diff --name-only HEAD^ HEAD", returnStdout: true).trim()
                        echo "Changed files: ${changedFiles}"

                        def servicePath = [
                            "customers-service": "customers-service/",
                            "vets-service": "vets-service/",
                            "visit-service": "visit-service/"
                        ]

                        // Kiểm tra các file thay đổi và chạy lệnh sh trong mỗi service thay đổi
                        if (changedFiles) {
                            servicePath.each { service, path ->
                                if (changedFiles.contains(path)) {
                                    echo "Changes detected in ${service}, running tests..."

                                    dir(path) {
                                        sh "mvn clean test"
                                        sh "mvn package"
                                    }
                                }
                            }
                        } else {
                            echo "No changes detected, skipping tests."
                        }
                    }
                }
            }
        }
    }
}
