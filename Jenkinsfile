pipeline {
    agent any

    tools {
        maven 'Maven 3.6.3'  // Đảm bảo Maven đã được cấu hình trong Jenkins Global Tool Configuration
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    echo "🚀 Running Checkout phase on branch: ${env.BRANCH_NAME}"
                    git branch: "${env.BRANCH_NAME}", url: 'https://github.com/ndmanh3003/spring-petclinic-microservices'
                    echo "✅ Checked out branch: ${env.BRANCH_NAME} successfully!"
                }
            }
        }

        stage('Build') {
            steps {
                echo "🛠️ Running Build phase..."
                script {
                    sh 'mvn clean install'
                }
                echo "✅ Build completed successfully!"
            }
        }

        stage('Test') {
            steps {
                echo "🔬 Running Test phase..."
                script {
                    sh 'mvn test'

                    // Thu thập kết quả kiểm thử và độ phủ testcase (Jacoco)
                    junit '**/target/test-*.xml'
                    jacoco execPattern: '**/target/jacoco-*.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java', exclusionPattern: ''
                }
                echo "✅ Test completed and results uploaded!"
            }
        }

        stage('Deploy') {
            steps {
                echo "🚀 Running Deploy phase..."
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
