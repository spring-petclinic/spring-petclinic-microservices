pipeline {
    agent none
    environment {
        DOCKER_REGISTRY = "devops22clc"
        REPO_URL = "https://github.com/devops22clc/spring-petclinic-microservices.git"
        REPO_NAME = "spring-petclinic-microservices"
        SERVICE_AS = "spring-petclinic"
        BRANCH_NAME = "main"
    }
    stages {
        stage('Initialize Variables') {
            agent { label 'controller-node' }
            steps {
                script {
                    def SERVICES = [
                        "spring-petclinic-config-server",
                        "spring-petclinic-discovery-server",
                        "spring-petclinic-api-gateway",
                        "spring-petclinic-customers-service",
                        "spring-petclinic-vets-service",
                        "spring-petclinic-visits-service",
                        "spring-petclinic-admin-server",
                        "spring-petclinic-tracing-server"
                    ]
                    env.SERVICES = SERVICES.join(",")

                    env.GIT_COMMIT_SHA = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    env.STAGE = env.BRANCH_NAME == "main" ? "prod" : "dev"
                }
            }
        }

        stage("Pull code") {
            agent { label 'maven-node' }
            steps {
                git branch: env.BRANCH_NAME, url: env.REPO_URL
            }
        }
        stage("Detect changes") {
            agent { label 'maven-node' }
            steps {
                script {
                    def changedFiles = sh(script: "git fetch origin && git diff --name-only HEAD origin/${env.BRANCH_NAME}", returnStdout: true).trim().split("\n")
                    def changedServices = [] as Set
                    def rootChanged = false

                    for (file in changedFiles) {
                        if (!file.startsWith("${SERVICE_AS}/")) {
                            rootChanged = true
                        } else {
                            def service = file.split("/")[0]
                            changedServices.add(service)
                        }
                    }

                    env.CHANGED_SERVICES = changedServices.join(',')
                    env.IS_CHANGED_ROOT = rootChanged.toString()
                    env.IS_CHANGED_ROOT = "true"
                    echo "Changed Services: ${env.CHANGED_SERVICES}"
                }
            }
        }
        stage("Build & TEST") {
            when {
                expression { return env.CHANGED_SERVICES?.trim()}
            }
            parallel {
                stage("Build") {
                    agent { label 'maven-node' }
                    steps {
                        script {
                            def changedServices = env.CHANGED_SERVICES.split(',')
                            for (service in changedServices) {
                                echo "Building service: ${service}"
                                sh """
                                    cd ${service}
                                    echo "run build for ${service}"
                                    mvn clean package -DskipTests
                                    cd ..
                                    docker build --build-arg SERVICE=${service} --build-arg STAGE=${STAGE} -f docker/Dockerfile-${service} -t ${DOCKER_REGISTRY}/${}-${service}:${env.GIT_COMMIT_SHA} .
                                    docker push ${DOCKER_REGISTRY}/${service}-${STAGE}:${GIT_COMMIT_SHA}
                                """
                            }
                        }
                    }
                }
                stage("TEST") {
                    agent { label 'maven-node' }
                    steps {
                        script {
                            def changedServices = env.CHANGED_SERVICES.split(',')
                            for (service in changedServices) {
                                echo "Running tests for service: ${service}"
                                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                                    sh """
                                        cd ${service}
                                        mvn test
                                    """
                                }
                            }
                        }
                    }
                }
            }
        }
        stage("Build & TEST ALL SERVICES") {
            when {
                expression { return env.IS_CHANGED_ROOT == "true"}
            }
            parallel {
                stage("Build") {
                    agent { label 'maven-node' }
                    steps {
                        sh "echo run build for all services"
                        script {
                            sh "mvn clean package -DskipTests"
                            def services = env.SERVICES.split(',')
                            for (service in services) {
                                sh """
                                    docker build --build-arg SERVICE=${service} --build-arg STAGE=${env.STAGE} -f docker/Dockerfile.${service} -t ${DOCKER_REGISTRY}/${service}:${env.GIT_COMMIT_SHA} .
                                    docker push ${DOCKER_REGISTRY}/${service}:${env.GIT_COMMIT_SHA}
                                `"""
                            }
                        }
                    }
                }
                stage("TEST") {
                    agent { label 'maven-node' }
                    steps {
                        sh "echo run test for all services"
                        catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                            sh "mvn test"
                        }
                    }
                }
            }
        }
    }
}
