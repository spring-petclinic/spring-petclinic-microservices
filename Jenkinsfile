pipeline {
    agent any

    environment {
        SERVICES = "spring-petclinic-admin-server spring-petclinic-genai-service spring-petclinic-api-gateway spring-petclinic-vets-service spring-petclinic-config-server spring-petclinic-visits-service spring-petclinic-customers-service spring-petclinic-discovery-server"
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    sh "git fetch origin test || true" // Đảm bảo luôn có test branch
                    def changedFiles = sh(script: "git diff --name-only origin/test", returnStdout: true).trim()
                    echo "Changed files: ${changedFiles}"

                    def affectedServices = SERVICES.split(" ").findAll { service ->
                        changedFiles.split("\n").any { file -> file.startsWith(service + "/") }
                    }

                    if (affectedServices.isEmpty()) {
                        echo "No service changes detected. Skipping pipeline."
                        currentBuild.result = 'SUCCESS'
                        error("No changes in relevant services. Skipping build & test.")
                    } else {
                        env.AFFECTED_SERVICES = affectedServices.join(" ")
                        echo "Services to build: ${env.AFFECTED_SERVICES}"
                    }
                }
            }
        }

        stage('Build & Test') {
            when {
                expression { return env.AFFECTED_SERVICES != null && env.AFFECTED_SERVICES != "" }
            }
            steps {
                script {
                    env.AFFECTED_SERVICES.split(" ").each { service ->
                        echo "Building and testing ${service}..."
                        dir(service) {
                            sh './mvnw clean package' // Sử dụng wrapper để tránh cần cài Maven
                            sh './mvnw test'
                        }
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    cobertura coberturaReportFile: '**/target/site/cobertura/coverage.xml'
                }
            }
        }
    }
}
