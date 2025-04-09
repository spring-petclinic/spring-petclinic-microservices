pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK 17'
    }

    stages {
        stage('Environment Check') {
            steps {
                echo "Maven Home: ${tool 'Maven'}"
                echo "Java Home: ${tool 'JDK 17'}"
                sh "mvn -version"
                sh "java -version"
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                echo "Running tests..."
                sh "mvn test"
            }
        }

        stage('Build') {
            steps {
                echo "Building application ..."
                sh "mvn clean package -DskipTests"
            }
        }
    }
}