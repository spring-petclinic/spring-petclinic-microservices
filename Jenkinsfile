pipeline {
    agent any
    environment {
        
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm  // Checkout mã nguồn từ repository
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def services = [
                        'spring-petclinic-customers-service',
                        'spring-petclinic-vets-service',
                        'spring-petclinic-visits-service'
                    ]

                    // Lấy commit chung giữa HEAD và origin/main
                    def lastCommit = sh(script: '''
                        git fetch origin main
                        git merge-base origin/main HEAD
                    ''', returnStdout: true).trim()

                    // Lấy danh sách file đã thay đổi kể từ commit chung
                    def diff = sh(script: "git diff --name-only ${lastCommit} HEAD", returnStdout: true).trim()

                    // Kiểm tra thay đổi thuộc service nào và thêm vào AFFECTED_SERVICES
                    def affectedServicesList = []
                    for (svc in services) {
                        if (diff.contains("${svc}/")) {
                            affectedServicesList.add(svc)
                        }
                    }

                    // Cập nhật AFFECTED_SERVICES với danh sách các service bị thay đổi
                    if (affectedServicesList) {
                        env.AFFECTED_SERVICES = affectedServicesList.join(',')
                        echo "Affected services: ${env.AFFECTED_SERVICES}"
                    } else {
                        echo "No relevant service changes detected."
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    // Kiểm tra nếu có dịch vụ bị thay đổi
                    def servicesToTest = env.AFFECTED_SERVICES.split(',')
                    for (svc in servicesToTest) {
                        dir("${svc}") {
                            sh 'mvn clean test'
                        }
                    }
                }
            }
            post {
                always {
                    // Quay lại mỗi service và thực hiện kiểm tra coverage
                    def servicesToTest = env.AFFECTED_SERVICES.split(',')
                    for (svc in servicesToTest) {
                        dir("${svc}") {
                            junit 'target/surefire-reports/*.xml'
                            recordCoverage tools: [jacoco()]
                        }
                    }
                }
            }
        }

        stage('Check Coverage') {
            steps {
                script {
                    def servicesToTest = env.AFFECTED_SERVICES.split(',')
                    for (svc in servicesToTest) {
                        def coverage = sh(script: "grep -A 1 'INSTRUCTION' ${svc}/target/site/jacoco/index.html | grep -o '[0-9]*%' | head -n1", returnStdout: true).trim().replace('%','').toInteger()
                        if (coverage < 70) {
                            error("Test coverage is below 70% for ${svc}: ${coverage}%")
                        } else {
                            echo "Test coverage OK for ${svc}: ${coverage}%"
                        }
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    def servicesToBuild = env.AFFECTED_SERVICES.split(',')
                    for (svc in servicesToBuild) {
                        dir("${svc}") {
                            sh 'mvn clean package -DskipTests'
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
