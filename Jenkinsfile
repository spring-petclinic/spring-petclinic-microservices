pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                echo "Đang lấy code từ GitHub..."
                git branch: 'main', url: 'https://github.com/DinhVuHuan/test_devopps.git'
            }
        }
        stage('Hello Jenkins') {
            steps {
                echo "Jenkins đang hoạt động!"
            }
        }
    }
}
