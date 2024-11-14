pipeline {
    agent any

    tools {
        maven 'maven38'
    }

    stages {
        stage ('Build pet clinic') {
            steps {
                sh "mvn clean install"
            }
        }
    }
}
