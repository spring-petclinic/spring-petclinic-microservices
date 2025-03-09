pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                echo "Đang lấy code từ GitHub..."
                git 'https://github.com/<your-team>/spring-petclinic-microservices.git'
            }
        }
        stage('Hello Jenkins') {
            steps {
                echo "Jenkins đang hoạt động!"
            }
        }
    }
}
