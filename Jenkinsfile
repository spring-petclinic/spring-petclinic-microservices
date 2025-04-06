pipeline {
    agent any

    stages {
        // stage('Test') {
        //     steps {
        //         echo 'Testing ...'
        //         sh '''
        //             cd spring-petclinic-visits-service
        //             mvn clean test -Djacoco.destFile=target/jacoco.exec
        //         '''
        //     }
        // }

        // stage('Building') {
        //     steps {
        //         echo 'Building ...'
        //         sh '''
        //             cd spring-petclinic-visits-service
        //             mvn clean install
        //         '''
        //     }
        // }
        stage('Test') {
            steps {
                echo 'Testing ...'
                // sh "mvn clean test"
                sh '''
                    ./mvnw clean test -f spring-petclinic-vets-service/pom.xml  
                '''
            }
        }
    }

    post {
        always {
            // junit '**/target/surefire-reports/*.xml'
            junit 'spring-petclinic-vets-service/target/surefire-reports/*.xml'

            // jacoco (
            //     execPattern: '**/target/jacoco.exec',
            //     classPattern: '**/target/classes',
            //     sourcePattern: '**/src/main/java',
            //     exclusionPattern: '**/target/test-classes'
            // )

            jacoco (
                execPattern: 'spring-petclinic-vets-service/target/jacoco.exec',
                classPattern: 'spring-petclinic-vets-service/target/classes',
                sourcePattern: 'spring-petclinic-vets-service/src/main/java',
                exclusionPattern: 'spring-petclinic-vets-service/target/test-classes'
            )
        }
    }
}