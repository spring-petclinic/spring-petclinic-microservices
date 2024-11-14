pipeline {
    agent any

    tools {
        maven 'maven3.9.9'
    }

    stages {
        stage ('Build pet clinic') {
            steps {
                sh "mvn clean install"
            }
        }
    }
}
