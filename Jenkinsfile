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
                    mvn clean test -Djacoco.destFile=target/jacoco.exec
                '''
                jacoco(
                    execPattern: 'spring-petclinic-vets-service/target/jacoco.exec',
                    classPattern: 'spring-petclinic-vets-service/target/classes',
                    sourcePattern: 'spring-petclinic-vets-service/src/main/java',
                    inclusionPattern: 'spring-petclinic-vets-service/*.class',
                    exclusionPattern: ''
                )
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
           jacoco()
        }
    }
}