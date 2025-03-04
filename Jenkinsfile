pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Determine Changed Services') {
            steps {
                script {
                    // Lấy danh sách file thay đổi so với commit trước
                    def changedFiles = sh(
                        script: "git diff --name-only HEAD~1 HEAD",
                        returnStdout: true
                    ).trim().split("\\r?\\n")

                    // Tạo biến global (hoặc environment) để sử dụng ở stage sau
                    env.CHANGED_SERVICES = ""
                    if (changedFiles.any { it.startsWith('spring-petclinic-customers-service/') }) {
                        env.CHANGED_SERVICES += "customers "
                    }
                    if (changedFiles.any { it.startsWith('spring-petclinic-vets-service/') }) {
                        env.CHANGED_SERVICES += "vets "
                    }
                    if (changedFiles.any { it.startsWith('spring-petclinic-visits-service/') }) {
                        env.CHANGED_SERVICES += "visits "
                    }
                    // ... Kiểm tra các service khác nếu cần
                }
            }
        }

        stage('Test & Build Customers') {
            when {
                expression { return env.CHANGED_SERVICES.contains("customers") }
            }
            steps {
                dir('spring-petclinic-customers-service') {
                    sh 'mvn clean test'
                    junit '**/target/surefire-reports/*.xml'
                    jacoco execPattern: '**/target/jacoco.exec', 
                           classPattern: '**/target/classes', 
                           sourcePattern: '**/src/main/java', 
                           exclusionPattern: ''
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Test & Build Vets') {
            when {
                expression { return env.CHANGED_SERVICES.contains("vets") }
            }
            steps {
                dir('spring-petclinic-vets-service') {
                    sh 'mvn clean test'
                    junit '**/target/surefire-reports/*.xml'
                    jacoco execPattern: '**/target/jacoco.exec',
                           classPattern: '**/target/classes',
                           sourcePattern: '**/src/main/java',
                           exclusionPattern: ''
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        // Tương tự cho visits-service, genai-service, ...
    }
}
