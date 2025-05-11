pipeline {
    agent any

    environment {
        SERVICES = "spring-petclinic-vets-service,spring-petclinic-customers-service,spring-petclinic-visits-service,spring-petclinic-admin-server,spring-petclinic-api-gateway,spring-petclinic-config-server,spring-petclinic-genai-service,spring-petclinic-discovery-server"
    }

    stages {
        stage('Detect Changed Services') {
            steps {
                script {
                    def changedFiles = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim().split("\n")
                    def affectedServices = []

                    SERVICES.split(',').each { service ->
                        if (changedFiles.find { it.startsWith(service + "/") }) {
                            affectedServices.add(service)
                        }
                    }

                    if (affectedServices.isEmpty()) {
                        echo "No services changed. Skipping build."
                        currentBuild.result = 'SUCCESS'
                        return
                    }

                    env.BUILD_SERVICES = affectedServices.join(',')
                }
            }
        }

        stage('Docker Build & Push') {
            when {
                expression { return env.BUILD_SERVICES }
            }
            steps {
                script {
                    def commitId = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    env.COMMIT_ID = commitId

                    env.BUILD_SERVICES.split(',').each { service ->
                        echo "Building Docker image for ${service} with tag ${commitId}..."
                        sh """
                            cd ${service}
                            docker build -t trgtamthanh/${service}:${commitId} .
                            docker push trgtamthanh/${service}:${commitId}
                        """
                    }
                }
            }
        }
    }
}
