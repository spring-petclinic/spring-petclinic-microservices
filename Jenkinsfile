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
    stage('Sensitive Data Detection') {
            steps {
                script {
                    def buildUrl = env.BUILD_URL
                    sh "gitleaks detect -v --no-git --source . --report-format json --report-path secrets.json || exit 0"
                }
            }
    }