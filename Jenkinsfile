pipeline {
    agent any

    environment {
        REPO_URL = 'https://github.com/htloc0610/spring-petclinic-microservices'
        BRANCH = "main"
        WORKSPACE_DIR = "repo" 
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    echo "Cloning repository ${REPO_URL} - Branch: ${BRANCH}"
                    sh "rm -rf ${WORKSPACE_DIR}" 
                    sh "mkdir -p ${WORKSPACE_DIR}" 
                    dir(WORKSPACE_DIR) {
                        sh "git clone -b ${BRANCH} ${REPO_URL} ."
                    }
                }
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    dir(WORKSPACE_DIR) {
                        def changes = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim()
                        echo "Files changed:\n${changes}"

                        def services = [
                            'spring-petclinic-admin-server',
                            'spring-petclinic-api-gateway',
                            'spring-petclinic-config-server',
                            'spring-petclinic-customers-service',
                            'spring-petclinic-discovery-server',
                            'spring-petclinic-genai-service',
                            'spring-petclinic-visits-service'
                        ]

                        def affectedServices = changes.tokenize("\n")
                            .collect { it =~ /^([^\/]+)\// ? (it =~ /^([^\/]+)\//)[0][1] : null } // Trích xuất tên thư mục gốc
                            .unique()
                            .findAll { it in services } // Lọc những service hợp lệ

                        if (affectedServices.isEmpty()) {
                            echo "No relevant changes, skipping pipeline"
                            currentBuild.result = 'ABORTED'
                            return
                        }

                        env.AFFECTED_SERVICES = affectedServices.join(",")
                        echo "Services to build: ${env.AFFECTED_SERVICES}"
                    }
                }
            }
        }

        stage('Test') {
            when {
                expression { env.AFFECTED_SERVICES && env.AFFECTED_SERVICES.split(",").any { it in [
                    'spring-petclinic-admin-server',
                    'spring-petclinic-api-gateway',
                    'spring-petclinic-config-server',
                    'spring-petclinic-customers-service',
                    'spring-petclinic-discovery-server',
                    'spring-petclinic-genai-service',
                    'spring-petclinic-visits-service'] } }
            }
            steps {
                script {
                    env.AFFECTED_SERVICES.split(",").each { service ->
                        echo "Running tests for ${service}..."
                        dir("${WORKSPACE_DIR}/${service}") {
                            sh 'mvn test'
                        }
                    }
                }
            }
            post {
                always {
                    junit "**/${WORKSPACE_DIR}/**/target/surefire-reports/*.xml"
                }
            }
        }

        stage('Code Coverage') {
            when {
                expression { env.AFFECTED_SERVICES && env.AFFECTED_SERVICES.split(",").any { it in [
                    'spring-petclinic-admin-server',
                    'spring-petclinic-api-gateway',
                    'spring-petclinic-config-server',
                    'spring-petclinic-customers-service',
                    'spring-petclinic-discovery-server',
                    'spring-petclinic-genai-service'] } }
            }
            steps {
                script {
                    env.AFFECTED_SERVICES.split(",").each { service ->
                        echo "Checking test coverage for ${service}..."
                        dir("${WORKSPACE_DIR}/${service}") {
                            sh 'mvn jacoco:report'
                        }
                    }
                }
            }
            post {
                always {
                    script {
                        env.AFFECTED_SERVICES.split(",").each { service ->
                            publishHTML([
                                target: [
                                    reportDir: "${WORKSPACE_DIR}/${service}/target/site/jacoco",
                                    reportFiles: 'index.html',
                                    reportName: "Code Coverage - ${service}"
                                ]
                            ])
                        }
                    }
                }
            }
        }

        stage('Build') {
            when {
                expression { env.AFFECTED_SERVICES && env.AFFECTED_SERVICES.split(",").any { it in [
                    'spring-petclinic-admin-server',
                    'spring-petclinic-api-gateway',
                    'spring-petclinic-config-server',
                    'spring-petclinic-customers-service',
                    'spring-petclinic-discovery-server',
                    'spring-petclinic-genai-service'] } }
            }
            steps {
                script {
                    env.AFFECTED_SERVICES.split(",").each { service ->
                        echo "Building ${service}..."
                        dir("${WORKSPACE_DIR}/${service}") {
                            sh 'mvn clean package'
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully for ${env.AFFECTED_SERVICES}!"
        }
        failure {
            echo "Pipeline failed!"
        }
    }
}
