pipeline {
    agent any

    environment {
        DOCKER_USER = 'quocviet10011'
        DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'
        SERVICES = "spring-petclinic-customers-service,spring-petclinic-vets-service,spring-petclinic-visits-service,spring-petclinic-genai-service"
    }

    options {
        skipDefaultCheckout(true)
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    checkout scm
                    env.COMMIT_ID = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                }
            }
        }

        stage('Detect changes') {
            when {
                not {
                    branch 'main'
                }
            }
            steps {
                script {
                    sh 'git fetch origin main'

                    def servicesList = env.SERVICES.split(',')
                    def targetBranch = env.CHANGE_TARGET ?: "main"
                    def commonAncestor = sh(returnStdout: true, script: "git merge-base HEAD origin/${targetBranch}").trim()
                    def changedFiles = sh(returnStdout: true, script: "git diff --name-only ${commonAncestor}").trim()

                    def servicesToBuild = servicesList.findAll { service ->
                        changedFiles.split('\n').any { it.startsWith("${service}/") }
                    }

                    if (servicesToBuild.isEmpty()) {
                        echo "No changes in any services. Skipping build."
                        currentBuild.result = 'SUCCESS'
                        return
                    }

                    env.SERVICES_TO_BUILD = servicesToBuild.join(',')
                    echo "Services to build: ${env.SERVICES_TO_BUILD}"
                }
            }
        }

        stage('Build and Push') {
            when {
                allOf {
                    not { branch 'main' }
                }
            }

            steps {
                script {
                    if (!env.SERVICES_TO_BUILD) {
                        echo "No services to build. Skipping Docker build stage."
                        return
                    }

                    def servicesToBuild = env.SERVICES_TO_BUILD.split(',')

                    docker.withRegistry('', DOCKER_CREDENTIALS_ID) {
                        servicesToBuild.each { service ->
                            echo "Building and pushing Docker image for ${service}"
                            dir(service) {
                                sh '../mvnw clean install -P buildDocker'
                            }

                            def oldImage= "springcommunity/${service}:latest"
                            def imageName = "${DOCKER_USER}/${service}:${env.COMMIT_ID}"
                            sh "docker tag ${oldImage} ${imageName}"
                            try {
                                sh "docker push ${imageName}"
                            } catch (e) {
                                echo "Failed to push image ${imageName}: ${e}"
                                throw e
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        failure {
            echo 'Build failed!'
        }
        success {
            echo 'Build and push completed successfully.'
        }
    }
}
