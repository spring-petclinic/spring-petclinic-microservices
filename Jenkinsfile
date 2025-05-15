pipeline {
    agent any

    environment {
        SERVICES = "spring-petclinic-vets-service,spring-petclinic-customers-service,spring-petclinic-visits-service,spring-petclinic-admin-server,spring-petclinic-api-gateway,spring-petclinic-config-server,spring-petclinic-genai-service,spring-petclinic-discovery-server"
        DOCKER_REGISTRY = "devopshcmus"
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

        stage('Docker Login') {
            when {
                expression { return env.BUILD_SERVICES }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
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

                    def branch = env.BRANCH_NAME ?: sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
                    def isMain = (branch == 'main')

                    env.BUILD_SERVICES.split(',').each { service ->
                        echo "Building Docker image for ${service} with tag ${commitId}..."

                        sh """
                            docker compose build ${service}
                            docker tag springcommunity/${service} ${env.DOCKER_REGISTRY}/${service}:${commitId}
                            docker push ${env.DOCKER_REGISTRY}/${service}:${commitId}
                        """

                        if (isMain) {
                            echo "Tagging and pushing ${service} as latest"
                            sh """
                                docker tag ${env.DOCKER_REGISTRY}/${service}:${commitId} ${env.DOCKER_REGISTRY}/${service}:latest
                                docker push ${env.DOCKER_REGISTRY}/${service}:latest
                            """
                        }
                    }
                }
            }
        }

        stage('Docker Logout') {
            when {
                expression { return env.BUILD_SERVICES }
            }
            steps {
                sh 'docker logout'
            }
        }
    }
}
