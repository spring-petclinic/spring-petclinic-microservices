pipeline {
    agent any

    environment {
        CHANGED_SERVICES = ""
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    def changedServices = []
                    def changedFiles = sh(script: 'git diff --name-only HEAD~1 HEAD', returnStdout: true).trim()

                    if (changedFiles.contains('spring-petclinic-customers-service')) {
                        changedServices.add('customers')
                    }
                    if (changedFiles.contains('spring-petclinic-vets-service')) {
                        changedServices.add('vets')
                    }
                    if (changedFiles.contains('spring-petclinic-visits-service')) {
                        changedServices.add('visits')
                    }
                    if (changedFiles.contains('spring-petclinic-api-gateway')) {
                        changedServices.add('api-gateway')
                    }
                    if (changedFiles.contains('spring-petclinic-discovery-server')) {
                        changedServices.add('discovery')
                    }
                    if (changedFiles.contains('spring-petclinic-config-server')) {
                        changedServices.add('config')
                    }
                    if (changedFiles.contains('spring-petclinic-admin-server')) {
                        changedServices.add('admin')
                    }

                    if (changedServices.isEmpty()) {
                        changedServices = ['all']
                    }

                    // Convert to string and assign to environment variable
                    env.CHANGED_SERVICES = changedServices.join(',')
                    
                    echo "Changed services: ${env.CHANGED_SERVICES}"
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if (env.CHANGED_SERVICES == 'all') {
                        sh './mvnw clean test'
                    } else {
                        def modules = env.CHANGED_SERVICES.split(',').collect { "spring-petclinic-${it}-service" }.join(',')
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
                    if (env.CHANGED_SERVICES == 'all') {
                        sh './mvnw clean package -DskipTests'
                    } else {
                        def modules = env.CHANGED_SERVICES.split(',').collect { "spring-petclinic-${it}-service" }.join(',')
                        sh "./mvnw clean package -DskipTests -pl ${modules}"
                    }
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                }
            }
        }
    }
}