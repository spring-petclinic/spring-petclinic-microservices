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

                    echo '🔨 Đang build tất cả services'
                    
                    // Build tất cả services không cần kiểm tra thay đổi
                    for (service in services) {
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

                    // Lưu danh sách tất cả services để push
                    writeFile file: 'all-services.txt', text: services*.name.join('\n')
                }
            }
        }

        stage('Docker Push') {
            steps {
                script {
                    echo "🔐 Đăng nhập Docker Hub với tài khoản: ${DOCKERHUB_CREDENTIALS_USR}"
                    // Cải thiện bảo mật bằng cách sử dụng --password-stdin
                    withCredentials([string(credentialsId: 'dockerhub-cred', variable: 'DOCKER_PWD')]) {
                        sh 'echo $DOCKER_PWD | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin https://index.docker.io/v1/'
                    }
                    
                    sh """
                        docker info
                        docker images
                    """

                    // Đọc danh sách tất cả services và push lên Docker Hub
                    def allServices = readFile('all-services.txt').split('\n').findAll { it }
                    echo "🚀 Đang push ${allServices.size()} services lên Docker Hub"
                    
                    for (service in allServices) {
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
            sh 'rm -f all-services.txt || true'
        }
    }
}