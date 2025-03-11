pipeline {
    agent none
    environment {
        DOCKER_REGISTRY = "devops22clc"
        REPO_URL = "https://github.com/devops22clc/spring-petclinic-microservices.git"
        REPO_NAME = "spring-petclinic-microservices"
        SERVICE_AS = "spring-petclinic"
    }
    stages {
        stage("Detect changes") {
            agent { label 'controller-node' }
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
                    echo "Changed Services: ${env.CHANGED_SERVICES}"
                }
            }
        }
        stage("Setup Workspace") {
            agent { label 'maven-node' }
            steps {
                script {
                    env.WORK_DIR = "${WORKSPACE}/${env.BRANCH_NAME}"
                    sh "mkdir -p ${env.WORK_DIR}"
                }
            }
        }
        stage("Pull code") {
            agent { label 'maven-node' }
            steps {
                dir("${env.WORK_DIR}") {
                    sh """
                    if [ ! -d "${REPO_NAME}" ]; then
                        git clone -b ${env.BRANCH_NAME} ${REPO_URL}
                    else
                        cd ${REPO_NAME} && git fetch origin ${env.BRANCH_NAME} && git reset --hard origin/${env.BRANCH_NAME}
                    fi
                    """
                }
            }
        }
        stage("Build & TEST") {
            when {
                expression { return env.CHANGED_SERVICES?.trim() }
            }
            parallel {
                stage("Build") {
                    agent { label 'maven-node' }
                    steps {
                        dir("${env.WORK_DIR}/${REPO_NAME}") {
                            script {
                                env.GIT_COMMIT_SHA = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                                env.STAGE = env.BRANCH_NAME == 'main' ? 'prod' : 'dev'
                                def services = env.CHANGED_SERVICES.split(',')
                                for (service in services) {
                                    echo "🚀 Building service: ${service}"
                                    sh """
                                        cd ${service}
                                        mvn clean package
                                        cd ..
                                        docker build --build-arg SERVICE=${service} --build-arg STAGE=${env.STAGE} -f docker/Dockerfile-${service} -t ${DOCKER_REGISTRY}/${service}:${env.GIT_COMMIT_SHA}
                                        docker push ${DOCKER_REGISTRY}/${service}:${env.GIT_COMMIT_SHA}
                                    """
                                }
                            }
                        }
                    }
                }
                stage("TEST") {
                    agent { label 'maven-node' }
                    steps {
                        dir("${env.WORK_DIR}/${REPO_NAME}") {
                            script {
                                def services = env.CHANGED_SERVICES.split(',')
                                for (service in services) {
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
        }
    }
}
