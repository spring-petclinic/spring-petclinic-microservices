pipeline {
    agent any
    options {
        skipDefaultCheckout()
    }
    environment {
        SERVICES_TO_BUILD = ""  // Đảm bảo biến được khai báo và có giá trị mặc định
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
                        git diff --name-only origin/main...HEAD | awk -F/ '{print $1}' | sort -u
                    ''', returnStdout: true).trim().split('\n')

                    def allServices = ['spring-petclinic-vets-service', 'spring-petclinic-visits-service', 'spring-petclinic-customers-service']
                    
                    // Chuyển changedServices thành List<String> và sử dụng intersect
                    def changedServicesList = changedServices as List
                    env.SERVICES_TO_BUILD = allServices.findAll { it in changedServicesList }.join(',')
                    // Nếu không có service nào được thay đổi, SERVICES_TO_BUILD sẽ là một chuỗi rỗng
                    if (!env.SERVICES_TO_BUILD) {
                        echo "No services changed, skipping build."
                    }
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
