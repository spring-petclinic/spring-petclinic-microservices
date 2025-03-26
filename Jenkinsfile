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
                    cd spring-petclinic-customers-service
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
            junit 'spring-petclinic-customers-service/target/surefire-reports/*.xml'
            jacoco(
                execPattern: 'spring-petclinic-customers-service/target/jacoco.exec',
                classPattern: 'spring-petclinic-customers-service/target/classes',
                sourcePattern: 'spring-petclinic-customers-service/src/main/java',
                inclusionPattern: 'spring-petclinic-customers-service/*.class',
                exclusionPattern: ''
            )
            
        }
    }
}