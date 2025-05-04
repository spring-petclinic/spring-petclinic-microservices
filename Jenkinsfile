pipeline {
    agent any
    
    environment {
        DOCKER_HUB_CREDS = credentials('dockerhub-credentials')
        DOCKER_IMAGE_NAME = 'thainhat/petclinic'
        COMMIT_ID = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
        BRANCH_NAME = "${env.BRANCH_NAME}"
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
                    
                    for (service in services) {
                        def serviceName = service.name
                        def servicePort = service.port
                        
                        def jarFile = findFiles(glob: "spring-petclinic-${serviceName}/target/*.jar")[0].path
                        sh "cp ${jarFile} docker/${serviceName}.jar"
                        
                        // Build image với context là thư mục docker/
                        sh """
                        docker build -t ${DOCKER_IMAGE_NAME}-${serviceName}:${COMMIT_ID} \
                            --build-arg ARTIFACT_NAME=${serviceName} \
                            --build-arg EXPOSED_PORT=${servicePort} \
                            -f ./docker/Dockerfile ./docker

                        docker tag ${DOCKER_IMAGE_NAME}-${serviceName}:${COMMIT_ID} ${DOCKER_IMAGE_NAME}-${serviceName}:latest
                        """
                        
                        sh "rm docker/${serviceName}.jar"
                    }
                }
            }
        }
        
        stage('Docker Push') {
            steps {
                sh "echo ${DOCKER_HUB_CREDS_PSW} | docker login -u ${DOCKER_HUB_CREDS_USR} --password-stdin"
                
                script {
                    def services = ['admin-server', 'api-gateway', 'customers-service', 'discovery-server', 'genai-service', 'vets-service', 'visits-service']
                    
                    for (service in services) {
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
            sh 'docker logout'
            sh "docker system prune -f"
        }
    }
}
