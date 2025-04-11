import io.jenkins.plugins.checks.api.ChecksStatus
def globalServiceChanged = []

pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                script {
                    def changes = sh(script: 'git diff --name-only HEAD~1 HEAD', returnStdout: true).trim()
                    echo "Changed files:\n${changes}"

                    def serviceList = [
                        "spring-petclinic-admin-server",
                        "spring-petclinic-api-gateway",
                        "spring-petclinic-config-server",
                        "spring-petclinic-customers-service",
                        "spring-petclinic-discovery-server",
                        "spring-petclinic-genai-service",
                        "spring-petclinic-vets-service",
                        "spring-petclinic-visits-service"
                    ]

                    for (svc in serviceList) {
                        if (changes.contains("${svc}/")) {
                            globalServiceChanged << svc
                        }
                    }

                    echo "Changed services: ${globalServiceChanged}"
                }
            }
        }

        stage('Test') {
            when {
                expression { true }
            }
            steps {
                script {
                    if (globalServiceChanged && globalServiceChanged.size() > 0) {
                        // Nếu globalServiceChanged không rỗng, chỉ build các service đã thay đổi
                        globalServiceChanged.each { svc ->
                            dir("${svc}") {
                                sh '../mvnw clean test jacoco:report'
                            }
                        }
                    } else {
                        // Nếu globalServiceChanged rỗng, build lại toàn hệ thống
                        echo "No specific services changed. Building entire system."
                        sh './mvnw clean test jacoco:report'
                    }
                }
            }
            post {
                always {
                    script {
                        if (globalServiceChanged && globalServiceChanged.size() > 0) {
                            // Xử lý báo cáo cho các service đã thay đổi
                            globalServiceChanged.each { svc ->
                                def reportPath = "${svc}/target/surefire-reports"
                                if (fileExists(reportPath)) {
                                    junit "${svc}/target/surefire-reports/*.xml"
                                } else {
                                    echo "No test reports found for ${svc}."
                                }
                            }
                        } else {
                            // Xử lý báo cáo cho toàn hệ thống
                            def reportPath = "target/surefire-reports"
                            if (fileExists(reportPath)) {
                                junit "target/surefire-reports/*.xml"
                            } else {
                                echo "No test reports found for full system build."
                            }
                        }
                    }
                }
            }
        }
        stage('Build') {
            steps {
                script {
                    if (globalServiceChanged && globalServiceChanged.size() > 0) {
                        echo "Building changed services: ${globalServiceChanged}"
                        globalServiceChanged.each { svc ->
                            dir("${svc}") {
                                sh '../mvnw package'
                            }
                        }
                    } else {
                        echo "No specific service changed — building the whole project"
                        sh './mvnw clean package'
                    }
                }
            }
        }
        stage('Coverage Check') {
            when {
                expression { globalServiceChanged && globalServiceChanged.size() > 0 }
            }
            steps {
                script {
                    globalServiceChanged.each { svc ->
                        def coverageFile = "${svc}/target/site/jacoco/index.html"
                        if (fileExists(coverageFile)) {
                            // Dùng grep để lấy phần trăm đầu tiên có dạng "##%"
                            def coverage = sh(script: "grep -oE '[0-9]+%' ${coverageFile} | head -1 | tr -d '%'", returnStdout: true).trim()
                            if (coverage.isInteger() && coverage.toInteger() < 70) {
                                error "${svc}: Coverage is ${coverage}%, below required threshold (70%)"
                            } else {
                                echo "${svc}: Coverage is ${coverage}% - OK!"
                            }
                        } else {
                            echo "${svc}: Coverage report not found at ${coverageFile}"
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            // Upload kết quả test
            junit '**/target/surefire-reports/*.xml'
            
            // Upload coverage (nếu đã cài JaCoCo plugin)
            jacoco execPattern: '**/target/jacoco.exec',
                   classPattern: '**/target/classes',
                   sourcePattern: '**/src/main/java',
                   inclusionPattern: '**/*.class'
        }
    }
}