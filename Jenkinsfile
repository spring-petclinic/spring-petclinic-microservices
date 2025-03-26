pipeline {
    agent any

    tools {
        maven '3.9.9'
    }

    stages {
        stage('Test') {
            steps {
                echo 'Testing ...'
                sh '''
                    cd spring-petclinic-vets-service
                    mvn clean test
                '''
            }
        }

        // stage('Building') {
        //     steps {
        //         echo 'Building ...'
        //         sh '''
        //             cd spring-petclinic-visits-service
        //             mvn clean install
        //         '''
        //     }
        // }
    }

    post {
        always {
           junit 'spring-petclinic-vets-service/target/surefire-reports/*.xml'
        }
    }
}