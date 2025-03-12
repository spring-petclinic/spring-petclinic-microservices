pipeline {
    agent none
    environment {
        DOCKER_REGISTRY = "devops22clc"
        REPO_URL = "https://github.com/devops22clc/spring-petclinic-microservices.git"
        REPO_NAME = "spring-petclinic-microservices"
        SERVICE_AS = "spring-petclinic"
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
                            "spring-petclinic-genai-service"
                    ]
                    env.SERVICES = SERVICES.join(",")

                    env.GIT_COMMIT_SHA = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    env.STAGE = env.BRANCH_NAME == "main" ? "prod" : "dev" //@fixme
                }
            }
        }
        stage("Detect changes") {
            agent { label 'controller-node' }
            steps {
                script {
                    sh "pwd"
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
                    echo "Changed Services: ${env.CHANGED_SERVICES}"
                }
            }
        }
        stage("Pull code") {
            agent { label 'maven-node' }
            steps {
                git branch: env.BRANCH_NAME, url: env.REPO_URL
            }
        }
        stage("Build & TEST") {
            parallel {
                stage("Build") {
                    agent { label 'maven-node' }
                    steps {
                        sh "cat ~/docker-registry-passwd.txt | docker login --username ${DOCKER_REGISTRY} --password-stdin"
                        script {
                            if (env.CHANGED_SERVICES?.trim()) {
                                def changedServices = env.CHANGED_SERVICES.split(',')
                                for (service in changedServices) {
                                sh """
                                    cd ${service}
                                    echo "run build for ${service}"
                                    mvn clean package -DskipTests
                                    cd ..
                                    docker build --build-arg SERVICE=${service} --build-arg STAGE=${env.STAGE} -f docker/Dockerfile.${service} -t ${DOCKER_REGISTRY}/${service}:${env.GIT_COMMIT_SHA} .
                                    docker push ${DOCKER_REGISTRY}/${service}:${env.GIT_COMMIT_SHA}
                                 """
                                }
                            } else if (env.IS_CHANGED_ROOT == "true") {
                                // Build all services
                                sh "echo run build for all services"
                                sh "mvn clean package -DskipTests"
                                def services = env.SERVICES.split(',')
                                for (service in services) {
                                    sh """
                                    docker build --build-arg SERVICE=${service} --build-arg STAGE=${env.STAGE} -f docker/Dockerfile.${service} -t ${DOCKER_REGISTRY}/${service}:${env.GIT_COMMIT_SHA} .
                                    docker push ${DOCKER_REGISTRY}/${service}:${env.GIT_COMMIT_SHA}
                                """
                                }
                            }
                            sh "echo y | docker image prune -a"
                        }
                    }
                }
                stage("TEST") {
                    agent { label 'maven-node' }
                    steps {
                        script {
                            if (env.CHANGED_SERVICES?.trim()) {
                                // Test only changed services
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
                            } else if (env.IS_CHANGED_ROOT == "true") {
                                // Test all services
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
        //stage('Deploy') {
        //    when {
        //        expression { return env.STAGE == "prod" || env.STAGE = "dev" || env.STAGE = "uat" }
        //    }
        //    agent { label 'kubernetes-node' }
        //    steps {
        //                script {
        //            if (env.IS_CHANGED_ROOT == 'true') {
        //                        echo "Deploying all services"
        //                sh """
        //
        //                """
        //            } else if (env.CHANGED_SERVICES?.trim()) {
        //                def changedServices = env.CHANGED_SERVICES.split(',')
        //                for (service in changedServices) {
        //                    echo "Deploying service: ${service}"
        //                }
        //            }
        //        }
        //    }
        //}
        stage('Post') {
            agent { label 'maven-node' }
            steps {
                script {
                    if (currentBuild.result == 'SUCCESS') {
                        sh "echo Pipeline executed successfully"
                    } else {
                        sh "echo Pipeline execution failed"
                    }
                    cleanWs()
                }
            }
        }
    }
}
