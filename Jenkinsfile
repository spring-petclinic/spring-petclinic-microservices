pipeline {
    agent any

    tools {
        maven 'Maven 3.8.6'
        jdk 'Java 17'
    }

    stages {

        stage('Build') {
            steps {
                echo "🔨 Building all services..."
                sh 'mvn clean install -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo "🧪 Running tests..."
                sh 'mvn test -pl spring-petclinic-api-gateway'
            }
        }

        stage('Test Report') {
            steps {
                echo "📊 Publishing test reports..."
                
                // Publish JUnit test results
                junit '**/spring-petclinic-api-gateway/target/surefire-reports/*.xml'
                
                // Nếu có SonarQube thì đo coverage:
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh 'mvn sonar:sonar -pl spring-petclinic-api-gateway'
                }
            }
        }
    }

    post {
        always {
            echo "✅ Pipeline finished."
        }
        failure {
            echo "❌ Pipeline failed."
        }
    }
}
