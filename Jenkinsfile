pipeline {
    agent any

    stages {
        stage('Pre-check') {
            steps {
                script {
                    def commitMessage = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
                    echo "Commit Message: ${commitMessage}"

                    if (env.BRANCH_NAME != 'main' && commitMessage.contains("Merge pull request")) {
                        echo "Skipping pipeline for PR merge on branch: ${env.BRANCH_NAME}"
                        currentBuild.result = 'SUCCESS'
                        error("Skip build for non-main branch after merge")
                    }
                }
            }
        }

        stage('Checkout Code') {
            steps {
                script {
                    def branchToCheckout = env.BRANCH_NAME ?: 'main'
                    echo "Checking out branch: ${branchToCheckout}"
                    git branch: branchToCheckout, url: 'https://github.com/phucvu0210/spring-petclinic-microservices.git'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def commitId = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()

                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh "docker login -u ${DOCKER_USERNAME} -p ${DOCKER_PASSWORD}"

                        def previousCommit = sh(script: 'git rev-parse HEAD~1 || echo ""', returnStdout: true).trim()
                        def changedFiles = previousCommit ? 
                            sh(script: "git diff --name-only ${previousCommit} HEAD", returnStdout: true).trim().split('\n') :
                            sh(script: "git diff --name-only HEAD", returnStdout: true).trim().split('\n')

                        def serviceDirs = [
                            'admin-server': 'spring-petclinic-admin-server',
                            'api-gateway': 'spring-petclinic-api-gateway',
                            'config-server': 'spring-petclinic-config-server',
                            'customers-service': 'spring-petclinic-customers-service',
                            'discovery-server': 'spring-petclinic-discovery-server',
                            'vets-service': 'spring-petclinic-vets-service',
                            'visits-service': 'spring-petclinic-visits-service',
                            'genai-service': 'spring-petclinic-genai-service'
                        ]

                        def servicePorts = [
                            "spring-petclinic-admin-server": 9090,
                            "spring-petclinic-api-gateway": 8080,
                            "spring-petclinic-config-server": 8888,
                            "spring-petclinic-customers-service": 8081,
                            "spring-petclinic-discovery-server": 8761,
                            "spring-petclinic-genai-service": 8084,
                            "spring-petclinic-vets-service": 8083,
                            "spring-petclinic-visits-service": 8082
                        ]

                        def servicesToBuild = []
                        for (def file in changedFiles) {
                            for (def entry in serviceDirs) {
                                if (file.startsWith(entry.value + '/')) {
                                    if (!servicesToBuild.contains(entry.key)) {
                                        servicesToBuild.add(entry.key)
                                    }
                                }
                            }
                        }

                        if (servicesToBuild.isEmpty()) {
                            echo "No changes detected for specific services. Skipping Docker build."
                            echo "Changed files: ${changedFiles.join(', ')}"
                            return
                        }

                        for (def service in servicesToBuild) {
                            def serviceDir = serviceDirs[service]
                            def fullServiceName = serviceDir
                            def exposedPort = servicePorts[fullServiceName] ?: 8080
                            def imageName = "${DOCKER_USERNAME}/spring-petclinic-${service}"

                            echo "Building JAR for ${service}..."
                            sh "./mvnw -pl ${serviceDir} -am clean package -DskipTests"

                            def jarFile = sh(script: "ls ${serviceDir}/target/*.jar | head -1", returnStdout: true).trim()
                            echo "Found JAR: ${jarFile}"

                            sh """
                                cp ${jarFile} docker/${service}.jar
                                cd docker
                                docker build --build-arg ARTIFACT_NAME=${service} --build-arg EXPOSED_PORT=${exposedPort} -t ${imageName}:${commitId} .
                                docker push ${imageName}:${commitId}
                                rm ${service}.jar
                                cd ..
                            """

                            if (env.BRANCH_NAME == 'main') {
                                sh """
                                    docker tag ${imageName}:${commitId} ${imageName}:latest
                                    docker push ${imageName}:latest
                                """
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                def commitId = env.GIT_COMMIT
                echo "Sending success status to GitHub for commit: ${commitId}"
                def response = httpRequest(
                    url: "https://api.github.com/repos/phucvu0210/spring-petclinic-microservices/statuses/${commitId}",
                    httpMode: 'POST',
                    contentType: 'APPLICATION_JSON',
                    requestBody: """{
                        "state": "success",
                        "description": "Build passed",
                        "context": "ci/jenkins-pipeline",
                        "target_url": "${env.BUILD_URL}"
                    }""",
                    authentication: 'github-token'
                )
                echo "GitHub Response: ${response.status}"
            }
        }

        failure {
            script {
                def commitId = env.GIT_COMMIT
                echo "Sending failure status to GitHub for commit: ${commitId}"
                def response = httpRequest(
                    url: "https://api.github.com/repos/phucvu0210/spring-petclinic-microservices/statuses/${commitId}",
                    httpMode: 'POST',
                    contentType: 'APPLICATION_JSON',
                    requestBody: """{
                        "state": "failure",
                        "description": "Build failed",
                        "context": "ci/jenkins-pipeline",
                        "target_url": "${env.BUILD_URL}"
                    }""",
                    authentication: 'github-token'
                )
                echo "GitHub Response: ${response.status}"
            }
        }

        always {
            echo "Pipeline finished."
        }
    }
}
