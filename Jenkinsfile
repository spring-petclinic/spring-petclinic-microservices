pipeline {
    agent any
    
    environment {
        // Đặt các biến môi trường cần thiết
        MAVEN_HOME = '/usr/local/maven' // Đảm bảo rằng Maven đã được cài đặt trên Jenkins
    }
    
    stages {
        stage('Checkout') {
            steps {
                // Clone repository từ GitHub
                git 'https://github.com/ndmanh3003/spring-petclinic-microservices.git'
            }
        }
        
        stage('Build') {
            steps {
                // Xây dựng dự án với Maven
                script {
                    echo 'Building the project...'
                    sh "'${MAVEN_HOME}/bin/mvn' clean install"
                }
            }
        }
        
        stage('Test') {
            steps {
                // Chạy unit tests và thu thập kết quả
                script {
                    echo 'Running unit tests...'
                    sh "'${MAVEN_HOME}/bin/mvn' test"
                }
            }
            post {
                always {
                    // Upload test results
                    junit '**/target/test-*.xml'  // Đảm bảo rằng bạn đã cấu hình Maven để tạo ra các file test-*.xml
                }
                success {
                    // Upload code coverage report (Giả sử bạn sử dụng Jacoco)
                    jacoco execPattern: '**/target/jacoco-*.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java', exclusionPattern: ''
                }
            }
        }
        
        stage('Deploy') {
            steps {
                // Thực hiện deploy nếu cần
                echo 'Deploying the application...'
                // Đây là nơi bạn có thể thêm các bước deploy của mình (ví dụ: deploy lên Docker, Kubernetes, v.v.)
            }
        }
    }
    
    post {
        always {
            // Bất kể build thành công hay thất bại, bạn có thể dọn dẹp
            cleanWs()
        }
    }
}
