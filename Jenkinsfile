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
                    cd spring-petclinic-visits-service
                    mvn clean test -Djacoco.destFile=target/jacoco.exec
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
            junit 'spring-petclinic-visits-service/target/surefire-reports/*.xml'
            jacoco(
                execPattern: 'spring-petclinic-visits-service/target/jacoco.exec',
                classPattern: 'spring-petclinic-visits-service/target/classes',
                sourcePattern: 'spring-petclinic-visits-service/src/main/java',
                inclusionPattern: 'spring-petclinic-visits-service/*.class',
                exclusionPattern: ''
            )
            
        }
    }
}