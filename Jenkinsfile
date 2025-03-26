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
                    mvn clean test -Djacoco.destFile=target/jacoco.exec
                '''
                jacoco(
                    execPattern: '**/target/jacoco.exec',
                    classPattern: '**/target/classes',
                    sourcePattern: '**/src/main/java',
                    inclusionPattern: '**/*.class',
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