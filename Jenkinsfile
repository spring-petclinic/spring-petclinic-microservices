pipeline {
    agent any

    environment {
        PROJECT_NAME = 'spring-petclinic'
        DOCKER_IMAGE_NAME = "thainhat104/${PROJECT_NAME}"
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-cred')
        BRANCH_NAME = "${env.BRANCH_NAME}"
        COMMIT_ID = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    def services = [
                        [name: 'admin-server', port: 9090],
                        [name: 'api-gateway', port: 8080],
                        [name: 'customers-service', port: 8081],
                        [name: 'discovery-server', port: 8761],
                        [name: 'vets-service', port: 8083],
                        [name: 'visits-service', port: 8082],
                        [name: 'genai-service', port: 8084]
                    ]

                    def changedServices = []

                    for (service in services) {
                        def serviceDir = "${PROJECT_NAME}-${service.name}"
                        def isChanged = sh(
                            script: "git diff --name-only origin/main...HEAD | grep -q '^${serviceDir}/'",
                            returnStatus: true
                        ) == 0

                        if (isChanged) {
                            changedServices << service
                        }
                    }

                    if (changedServices.isEmpty()) {
                        echo '✅ Không có service nào thay đổi. Bỏ qua bước Docker build.'
                    } else {
                        for (service in changedServices) {
                            def serviceName = service.name
                            def servicePort = service.port

                            def jarFile = findFiles(glob: "${PROJECT_NAME}-${serviceName}/target/*.jar")[0].path
                            sh "cp ${jarFile} docker/${serviceName}.jar"

                            sh """
                                docker build -t ${DOCKER_IMAGE_NAME}-${serviceName}:${COMMIT_ID} \
                                    --build-arg ARTIFACT_NAME=${serviceName} \
                                    --build-arg EXPOSED_PORT=${servicePort} \
                                    -f ./docker/Dockerfile ./docker

                                docker tag ${DOCKER_IMAGE_NAME}-${serviceName}:${COMMIT_ID} ${DOCKER_IMAGE_NAME}-${serviceName}:latest
                            """

                            sh "rm docker/${serviceName}.jar"
                        }

                        writeFile file: 'changed-services.txt', text: changedServices*.name.join('\n')
                    }
                }
            }
        }

        stage('Docker Push') {
            when {
                expression { fileExists('changed-services.txt') }
            }
            steps {
                script {
                    echo "🔐 Logging in to Docker Hub as user: ${DOCKERHUB_CREDENTIALS_USR}"
                    sh """
                        docker login -u ${DOCKERHUB_CREDENTIALS_USR} -p ${DOCKERHUB_CREDENTIALS_PSW} https://index.docker.io/v1/
                        docker info
                        docker images
                    """

                    def changedServices = readFile('changed-services.txt').split('\n').findAll { it }
                    for (service in changedServices) {
                        sh """
                            docker push ${DOCKER_IMAGE_NAME}-${service}:${COMMIT_ID}
                            docker push ${DOCKER_IMAGE_NAME}-${service}:latest  
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
            sh 'docker system prune -f || true'
        }
    }
}
