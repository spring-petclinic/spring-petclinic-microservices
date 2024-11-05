pipeline {
    agent any
    tools {
        maven 'maven38'
    }

    stages {
        stage('checkout') {
            steps {
                git url: "https://github.com/tubuochiatoh/spring-petclinic-microservices.git", branch "*"
            }
        }
    }
}