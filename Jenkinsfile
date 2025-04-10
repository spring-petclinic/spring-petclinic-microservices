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

                    if (globalServiceChanged.isEmpty()) {
                        echo "No relevant changes detected, skipping build."
                        currentBuild.result = 'ABORTED'
                        return
                    }

                    echo "Changed services: ${globalServiceChanged}"
                }
            }
        }

        stage('Test') {
            when {
                expression { globalServiceChanged && globalServiceChanged.size() > 0 }
            }
            steps {
                script {
                    globalServiceChanged.each { svc ->
                        dir("${svc}") {
                            sh '../mvnw clean test jacoco:report'
                        }
                    }
                }
            }
            post {
                always {
                    script {
                        globalServiceChanged.each { svc ->
                            def reportPath = "${svc}/target/surefire-reports"
                            if (fileExists(reportPath)) {
                                junit "${svc}/target/surefire-reports/*.xml"
                                
                                // Xuất báo cáo JaCoCo (tạo báo cáo về độ phủ)
                                def jacocoReportFile = "${svc}/target/site/jacoco/jacoco.xml"
                                if (fileExists(jacocoReportFile)) {
                                    jacoco execPattern: "${svc}/target/jacoco.exec", classPattern: '**/classes', sourcePattern: '**/src/main/java', inclusionPattern: '**/*.java', exclusionPattern: '**/*Test.java'
                                } else {
                                    echo "No JaCoCo report found for ${svc}."
                                }
                            } else {
                                echo "No test reports found for ${svc}."
                            }
                        }
                    }
                }
            }
        }

        stage('Build') {
            when {
                expression { globalServiceChanged && globalServiceChanged.size() > 0 }
            }
            steps {
                script {
                    globalServiceChanged.each { svc ->
                        dir("${svc}") {
                            sh '../mvnw package'
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
