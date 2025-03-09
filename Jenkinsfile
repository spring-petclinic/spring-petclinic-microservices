pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                echo "Đang lấy code từ GitHub..."
                git branch: 'main', url: 'https://github.com/DinhVuHuan/test_devopps.git'
            }
        }
        stage('Test') {
            steps {
                echo "Chạy unit test..."
                sh './mvnw test'
            }
        }
        stage('Build') {
            steps {
                echo "Build ứng dụng..."
                sh './mvnw package'
            }
        }
    }
}
