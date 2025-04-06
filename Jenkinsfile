pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                sh 'mvn test' // Chạy unit test
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml' // Upload test result
                    recordIssues enabledForFailure: true, tool: checkStyle(pattern: '**/target/checkstyle-result.xml') // Báo cáo độ phủ
                }
            }
        }
        stage('Build') {
            steps {
                sh 'mvn package -DskipTests' // Build artifact
            }
        }
    }
}