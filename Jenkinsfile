pipeline {
    agent any

    environment {
        CHANGED_SERVICES = []
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = sh(script: 'git diff --name-only HEAD~1 HEAD', returnStdout: true).trim()

                    if (changedFiles.contains('spring-petclinic-customers-service')) {
                        CHANGED_SERVICES.add('customers')
                    }
                    if (changedFiles.contains('spring-petclinic-vets-service')) {
                        CHANGED_SERVICES.add('vets')
                    }
                    if (changedFiles.contains('spring-petclinic-visits-service')) {
                        CHANGED_SERVICES.add('visits')
                    }
                    if (changedFiles.contains('spring-petclinic-api-gateway')) {
                        CHANGED_SERVICES.add('api-gateway')
                    }
                    if (changedFiles.contains('spring-petclinic-discovery-server')) {
                        CHANGED_SERVICES.add('discovery')
                    }
                    if (changedFiles.contains('spring-petclinic-config-server')) {
                        CHANGED_SERVICES.add('config')
                    }
                    if (changedFiles.contains('spring-petclinic-admin-server')) {
                        CHANGED_SERVICES.add('admin')
                    }

                    if (
                        CHANGED_SERVICES.isEmpty()
                        ) {
                        CHANGED_SERVICES = ['all']
                        }

                    echo "Changed services: ${CHANGED_SERVICES}"
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if (CHANGED_SERVICES.contains('all')) {
                        sh './mvnw clean test'
                    } else {
                        def modules = CHANGED_SERVICES.collect { "spring-petclinic-${it}-service" }.join(',')
                        sh "./mvnw clean test -pl ${modules}"
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java'
                    )
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    if (CHANGED_SERVICES.contains('all')) {
                        sh './mvnw clean package -DskipTests'
                    } else {
                        def modules = CHANGED_SERVICES.collect { "spring-petclinic-${it}-service" }.join(',')
                        sh "./mvnw clean package -DskipTests -pl ${modules}"
                    }
                }
                archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
            }
        }
    }
}
