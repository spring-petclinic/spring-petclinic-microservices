pipeline {
    agent any
    options {
        skipDefaultCheckout()
    }
    environment {
        MAVEN_OPTS = "-Dmaven.repo.local=.m2/repository"
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Determine Changed Services') {
            steps {
                script {
                    def changedServices = sh(script: '''
                        git fetch origin main
                        git diff --name-only $(git merge-base origin/main HEAD) HEAD | awk -F/ '{print $1}' | sort -u
                    ''', returnStdout: true).trim().split('\n')

                    def allServices = ['spring-petclinic-vets-service', 'spring-petclinic-visits-service', 'spring-petclinic-customers-service', 'spring-petclinic-genai-service']
                    
                    // Chuyển changedServices thành List<String> và sử dụng intersect
                    def changedServicesList = changedServices as List
                    env.SERVICES_TO_BUILD = allServices.findAll { it in changedServicesList }.join(',')
                }
            }
        }

        stage('Build and Test') {
            when {
                expression { return env.SERVICES_TO_BUILD?.trim() }
            }
            steps {
                script {
                    def services = env.SERVICES_TO_BUILD.split(',')
                    for (s in services) {
                        dir("${s}") {
                            sh "mvn clean test"
                            junit '**/target/surefire-reports/*.xml'
                            jacoco execPattern: '**/target/jacoco.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java'
                            sh "mvn clean package"
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            echo "Pipeline complete"
        }
    }
}
