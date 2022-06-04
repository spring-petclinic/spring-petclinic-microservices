pipeline{
    agent any
    tools{
        maven 'Maven 3.8.5'
        jdk 'jdk8'
    }
    stages{
        stage("Initialize"){
            steps{
                sh 'echo "PATH = ${PATH}"'
            }
        }
        stage('Build'){
            steps{
                echo 'Building ...'
                sh 'mvn compile'
            }

        }
        stage('Test'){
            steps{
                echo 'Testing ...'
                sh 'mvn test'
            }
        }
    }
}
