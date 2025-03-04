pipeline {
    agent any  // Chạy trên bất kỳ agent nào có sẵn
    stages {
        stage('Checkout') {
            steps {
                echo "Cloning repository..."
                checkout scm
            }
        }
        stage('Test') {
            steps {
                echo "Running tests..."
                sh "./mvnw test"
                junit 'target/surefire-reports/*.xml'  // Upload test results
            }
        }
        stage('Build') {
            steps {
                echo "Building application..."
                sh "./mvnw package -DskipTests"
            }
        }
    }
}
