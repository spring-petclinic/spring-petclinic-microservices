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

        stage('Login to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker_hub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
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
                        def serviceName = service.replaceFirst('spring-petclinic-', '')
                        sh """
                            docker-compose build ${serviceName}
                        """

                        sh """
                            docker tag springcommunity/${service} phuong273/${serviceName}:${commitId} 
                        """
                        
                        sh """
                            docker push phuong273/${serviceName}:${commitId}
                        """
                    }
                }
            }
        }
    }
}
