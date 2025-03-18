pipeline {
    agent any

    environment {
        MAVEN_HOME = '/usr/local/maven'  // Đảm bảo Maven đã được cài đặt trên Jenkins
    }

    stages {
        stage('Checkout') {
            steps {
                echo "🚀 Running Checkout phase..."
                // Clone repository từ GitHub
                git url: 'https://github.com/spring-petclinic/spring-petclinic-microservices.git', branch: 'main'
                echo "✅ Checkout completed successfully!"
            }
        }

        stage('Build') {
            steps {
                echo "🛠️ Running Build phase..."
                script {
                    // Thực hiện build dự án bằng Maven (build thực sự)
                    sh "'${MAVEN_HOME}/bin/mvn' clean install"
                }
                echo "✅ Build completed successfully!"
            }
        }

        stage('Test') {
            steps {
                echo "🔬 Running Test phase..."
                script {
                    // Chạy unit tests và thu thập kết quả kiểm thử
                    sh "'${MAVEN_HOME}/bin/mvn' test"

                    // Lấy kết quả kiểm thử và độ phủ testcase (Jacoco)
                    junit '**/target/test-*.xml'  // Thu thập kết quả kiểm thử từ các tệp XML
                    jacoco execPattern: '**/target/jacoco-*.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java', exclusionPattern: ''
                }
                echo "✅ Test completed and results uploaded!"
            }
        }

        stage('Deploy') {
            steps {
                echo "🚀 Running Deploy phase..."
                // Mô phỏng bước deploy
                echo "✅ Deploy completed successfully!"
            }
        }
    }

    post {
        always {
            echo "✔️ Pipeline finished."
        }
    }
}
