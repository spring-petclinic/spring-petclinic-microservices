pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                echo "Đang lấy code từ GitHub..."
                git 'https://github.com/DinhVuHuan/test_devopps.git'  // URL phải đúng
            }
        }
        stage('Hello Jenkins') {
            steps {
                echo "Jenkins đang hoạt động!"
            }
        }
    }
}
