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
                                    
                                    // Xuất báo cáo JaCoCo
                                    def jacocoReportFile = "${svc}/target/site/jacoco/jacoco.xml"
                                    if (fileExists(jacocoReportFile)) {
                                        jacoco execPattern: "${svc}/target/jacoco.exec", 
                                               classPattern: '**/classes', 
                                               sourcePattern: '**/src/main/java', 
                                               inclusionPattern: '**/*.java', 
                                               exclusionPattern: '**/*Test.java'
                                    } else {
                                        echo "No JaCoCo report found for ${svc}."
                                    }
                                } else {
                                    echo "No test reports found for ${svc}."
                                }
                            }
                        } else {
                            // Xử lý báo cáo cho toàn hệ thống
                            def reportPath = "target/surefire-reports"
                            if (fileExists(reportPath)) {
                                junit "target/surefire-reports/*.xml"
                                
                                def jacocoReportFile = "target/site/jacoco/jacoco.xml"
                                if (fileExists(jacocoReportFile)) {
                                    jacoco execPattern: "target/jacoco.exec", 
                                           classPattern: '**/classes', 
                                           sourcePattern: '**/src/main/java', 
                                           inclusionPattern: '**/*.java', 
                                           exclusionPattern: '**/*Test.java'
                                } else {
                                    echo "No JaCoCo report found for full system build."
                                }
                            } else {
                                echo "No test reports found for full system build."
                            }
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