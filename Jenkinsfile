pipeline {
    agent any

    tools {
        maven 'maven'
    }

    stages {
        stage ('Build pet clinic') {
            steps {
                sh "mvn clean install"
            }
        }
    }
}
