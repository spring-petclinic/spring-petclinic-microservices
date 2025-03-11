pipeline {
    agent none
    tools {
        maven "Maven 3"
    }

    stages {
        stage('Customers Service') {
            when { changeset "**/spring-petclinic-customers-service/**/*" }
            steps {
                checkout scm
                sh 'mvn -pl spring-petclinic-customers-service -am clean package'
            }
            post {
                always {
                    junit '**/spring-petclinic-customers-service/target/surefire-reports/*.xml'
                }
            }
        }

        stage('GenAI Service') {
            when { changeset "**/spring-petclinic-genai-service/**/*" }
            steps {
                checkout scm
                sh 'mvn -Dmaven.test.failure.ignore=true -pl spring-petclinic-genai-service -am clean package'
            }
            post {
                success {
                    junit '**/spring-petclinic-genai-service/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Vets Service') {
            when { changeset "**/spring-petclinic-vets-service/**" }
            steps {
                checkout scm
                sh 'mvn -pl spring-petclinic-vets-service -am clean test jacoco:report'
            }
            post {
                always {
                    junit '**/spring-petclinic-vets-service/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Visits Service') {
            when { changeset "**/spring-petclinic-visits-service/**" }
            steps {
                checkout scm
                sh 'mvn -pl spring-petclinic-visits-service -am clean test jacoco:report'
            }
            post {
                always {
                    junit '**/spring-petclinic-visits-service/target/surefire-reports/*.xml'
                }
            }
        }
    }

    post {
        success {
            echo 'Success'
        }
        failure {
            echo 'Failed'
        }
    }
}
