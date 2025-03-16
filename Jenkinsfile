pipeline {
    agent none  // No default agent; assign dynamically per stage

    environment {
        OWNER = "devops22clc"
        REPO_URL = "https://github.com/devops22clc/spring-petclinic-microservices.git"
        REPO_NAME = "spring-petclinic-microservices"
        SERVICE_AS = "spring-petclinic"
    }

    stages {
        stage("Detect Changes") {
            agent { label 'controller-node' }
            steps {
                script {
                    sh "git fetch --no-tags --force --progress -- ${REPO_URL} refs/heads/${BRANCH_NAME}:refs/remotes/origin/${BRANCH_NAME}"
                    def changedFiles = sh(script: "git diff --name-only origin/${BRANCH_NAME}", returnStdout: true).trim().split("\n")
                    def changedServices = [] as Set
                    def rootChanged = false

                    for (file in changedFiles) {
                        if (!file.startsWith("${SERVICE_AS}")) {
                            rootChanged = true
                            break
                        } else {
                            def service = file.split("/")[0]
                            changedServices.add(service)
                        }
                    }

                    env.CHANGED_SERVICES = changedServices.join(',')
                    env.IS_CHANGED_ROOT = rootChanged.toString()
                    sh "git merge origin/${BRANCH_NAME}"
                }
            }
        }

        stage("Build & TEST") {
            parallel {
                stage("Build") {
                    steps {
                        script {
                            env.GIT_COMMIT_SHA = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
                            if (env.IS_CHANGED_ROOT == "true") env.CHANGED_SERVICES = env.SERVICES

                            def changedServices = env.CHANGED_SERVICES.split(',')
                            def parallelBuilds = [:]

                            for (service in changedServices) {
                                parallelBuilds[service] = {
                                    def agentNode = selectLeastBusyAgent()
                                    node(agentNode) {
                                        stage("Build - ${service}") {
                                            sh """
                                            cd ${service}
                                            mvn clean package -DskipTests
                                            cd ..
                                            docker build --build-arg SERVICE=${service} --build-arg STAGE=${env.STAGE} \
                                                -f docker/Dockerfile.${service} -t ${OWNER}/${env.STAGE}-${service}:${env.GIT_COMMIT_SHA} .
                                            """
                                        }
                                    }
                                }
                            }
                            parallel parallelBuilds
                        }
                    }
                }

                stage("TEST") {
                    steps {
                        script {
                            if (env.IS_CHANGED_ROOT == "true") env.CHANGED_SERVICES = env.SERVICES

                            def changedServices = env.CHANGED_SERVICES.split(',')
                            def parallelTests = [:]

                            for (service in changedServices) {
                                parallelTests[service] = {
                                    def agentNode = selectLeastBusyAgent()
                                    node(agentNode) {
                                        stage("Test - ${service}") {
                                            catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                                                sh """
                                                cd ${service}
                                                mvn clean test jacoco:report && mvn clean verify
                                                """
                                            }
                                        }
                                    }
                                }
                            }
                            parallel parallelTests
                        }
                    }
                }
            }
        }
    }
}

// Function to Select the Least Busy Agent
def selectLeastBusyAgent() {
    def nodes = jenkins.model.Jenkins.instance.nodes.findAll { it.labelString.contains('maven-node') }
    def agentUsage = [:]

    for (node in nodes) {
        def executors = node.toComputer().getExecutors()
        def busyExecutors = executors.count { it.isBusy() }
        agentUsage[node.getNodeName()] = busyExecutors
    }

    def leastBusyAgent = agentUsage.sort { it.value }.keySet().first()
    echo "Selected Agent: ${leastBusyAgent}"
    return leastBusyAgent
}
