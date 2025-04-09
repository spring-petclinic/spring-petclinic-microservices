pipeline {
    agent { label 'jenkins-agent' }  // hoặc 'ubuntu' nếu bạn đặt label như vậy

    tools {
        maven 'Maven 3.8.6'  // Đã cấu hình trong Jenkins (Global Tool Configuration)
        jdk 'JDK17'          // Đã cấu hình Java 17
    }

    environment {
        MAVEN_OPTS = '-Dmaven.test.failure.ignore=false'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/<your-group>/spring-petclinic-microservices.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package'
            }
        }
    }

    post {
        always {
            echo 'Build finished'
        }
        success {
            echo 'Build successful 🎉'
        }
        failure {
            echo 'Build failed 💥'
        }
    }
}
