pipeline {
    agent any
    tools {
        maven 'maven38'
    }

    stages {
        
        stage('Credential Scanner for detecting Secrets') {
            steps {
                script {
                    def buildUrl = env.BUILD_URL
                    sh "gitleaks detect -v --no-git --source . --report-format json --report-path secrets.json || exit 0"
                }
            }
        }    
        stage('Build pet clinic') {
            steps {
                sh "mvn clean install"
            }
        }

        stage('Test Petclinic') {
            steps {
                script {
                    //Run Unit Test
                    sh 'mvn test'
                }
            }
            post {
                always {
                    //Archive and publish test results of the spring-petclinic"
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        stage('Package Petclinic App') {
            steps {
                script {
                    // Package the application (For example, create a JAR or WAR file)
                    sh 'mvn package'
                }
            }
            post {
                success {
                    // Archive the package artifact and put in a folder
                    archiveArtifacts artifacts: 'spring-petclinic-*/target/*.jar', allowEmptyArchive: true
                }
            }
        }
    }
}