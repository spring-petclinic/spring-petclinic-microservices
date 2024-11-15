pipeline {
    agent any
    tools {
        maven 'maven38'
    }

    stages {
        
        stage('Credential Scanner') {
            steps {
                script {
                    def buildUrl = env.BUILD_URL
                    sh '/path/to/gitleaks detect --source .'
                }
            }
        }    
        stage('Build pet clinic') {
            steps {
                sh "mvn clean install"
            }
        }
    }
}