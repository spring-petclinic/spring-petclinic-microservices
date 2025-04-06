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
                    ./mvnw clean test -f spring-petclinic-customers-service/pom.xml  
                '''
            }
        }
    }

    post {
        always {
            // junit '**/target/surefire-reports/*.xml'
            junit 'spring-petclinic-customers-service/target/surefire-reports/*.xml'

            // jacoco (
            //     execPattern: '**/target/jacoco.exec',
            //     classPattern: '**/target/classes',
            //     sourcePattern: '**/src/main/java',
            //     exclusionPattern: '**/target/test-classes'
            // )

            jacoco (
                execPattern: 'spring-petclinic-customers-service/target/jacoco.exec',
                classPattern: 'spring-petclinic-customers-service/target/classes',
                sourcePattern: 'spring-petclinic-customers-service/src/main/java',
                exclusionPattern: 'spring-petclinic-customers-service/target/test-classes'
            )
        }
    }
}