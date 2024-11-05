pipeline {
    agent any

    tools {
        maven 'maven388'
    }

    stages {
        stage ('checkout'){
            steps{
                git url: "https://github.com/ngengecharity/spring-petclinic-microservices.git", branches: "*"
            }
        }
    }
}
