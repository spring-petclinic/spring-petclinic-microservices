pipeline {
    agent any

    tools {
        maven 'maven388'
    }

    stages {
        stage ('Build pet clinic') {
            steps {
                sh "mvn clean install"
            }
        }
    }
}
