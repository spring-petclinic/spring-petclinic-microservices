pipeline {
    agent any

    environment{
        MAVEN_OPTS = '-Dmaven.test.failure.ignore=true'
    }

    tools {
        maven 'Maven 3.9.9'
        jdk 'JDK 11'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main', url: 'https://github.com/blue182/spring-petclinic-microservices.git';
            }
        }

        stage('Build'){
            steps {
                echo 'Building the project...'
                sh "mvn clean install -DskipTests=true"
            }
        }

        stage('Test & Coverage') {
            steps {
                echo 'Running tests with code coverage...'
                sh "mvn test jacoco:report"
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    // Optional nếu đã cài plugin JaCoCo Jenkins
                    jacoco execPattern: '**/target/jacoco.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java'
                }
            }
        }

        stage('Code Coverage'){
            steps{
                echo 'Running code coverage...'
                sh 'mvn jacoco:report'
            }
        }
    }

    post{
        success {
            echo "✅ Build and test passed successfully!"
        }

        failure {
            echo "❌ Build or test failed!"
        }
    }
}