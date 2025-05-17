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

//         stage('Build and Push Docker Images') {
//             when {
//                 expression { return env.BUILD_SERVICES }
//             }
//             steps {
//                 script {
//                     def commitId = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
//                     def branch = env.BRANCH_NAME ?: sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
//                     def isMain = (branch == 'main')
//
//                     env.BUILD_SERVICES.split(',').each { service ->
//                         echo "Building Docker image for ${service} with tag ${commitId}..."
//
//                         // Build jar + docker image via Maven exec plugin profile
//                         sh "./mvnw clean install -pl ${service} -Dmaven.test.skip=true -P buildDocker -Ddocker.image.prefix=${env.DOCKER_REGISTRY} -Ddocker.image.tag=${commitId} -Dcontainer.build.extraarg=--push"
//
//                         // Nếu bạn dùng profile buildDocker với --push thì docker image đã được push rồi
//                         // Nhưng để chắc, bạn có thể tag lại latest và push nếu trên main branch
//                         if (isMain) {
//                             echo "Tagging and pushing ${service} as latest"
//                             sh """
//                                 docker tag ${env.DOCKER_REGISTRY}/${service}:${commitId} ${env.DOCKER_REGISTRY}/${service}:latest
//                                 docker push ${env.DOCKER_REGISTRY}/${service}:latest
//                             """
//                         }
//                     }
//                 }
//             }
//         }

        stage('Build and Push Docker Images') {
            when {
                expression { return env.BUILD_SERVICES }
            }
            steps {
                script {
                    def commitId = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    def branch = env.BRANCH_NAME ?: sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
                    def isMain = (branch == 'main')

                    env.BUILD_SERVICES.split(',').each { service ->
                        echo "Building and pushing Docker image for ${service} with tag ${commitId}..."

                        // Build + push Docker image bằng Maven plugin, giả sử profile buildDocker có --push
                        sh "./mvnw clean install -pl ${service} -Dmaven.test.skip=true -P buildDocker -Ddocker.image.prefix=${env.DOCKER_REGISTRY} -Ddocker.image.tag=${commitId} -Dcontainer.build.extraarg=--push"

                        if (isMain) {
                            echo "Tagging and pushing ${service} as latest"
                            // Kiểm tra image tồn tại trước khi tag và push
                            def imageExists = sh(script: "docker images -q ${env.DOCKER_REGISTRY}/${service}:${commitId}", returnStdout: true).trim()
//                             if (imageExists) {
//                                 sh """
//                                     docker tag ${env.DOCKER_REGISTRY}/${service}:${commitId} ${env.DOCKER_REGISTRY}/${service}:latest
//                                     docker push ${env.DOCKER_REGISTRY}/${service}:latest
//                                 """
//                             } else {
//                                 echo "Image ${env.DOCKER_REGISTRY}/${service}:${commitId} not found locally. Skipping tag latest."
//                             }
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
