def helmValues = "/home/jenkins/ptb-agent/workspace/spring-petclinic-project/test-app/values.yaml"
def helmChart = "/home/jenkins/ptb-agent/workspace/spring-petclinic-project/test-app/"

pipeline {
    agent none
    stages {
        stage('Checkout branch') {
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    BRANCH_NAME = params.BRANCH_NAME ?: env.GIT_BRANCH
                    SERVICE = BRANCH_NAME.replaceAll("^origin/", "")
                    checkout([$class: 'GitSCM', branches: [[name: "${BRANCH_NAME}"]], userRemoteConfigs: [[url: 'https://github.com/22127025/spring-petclinic-microservices.git']]])
                }
            }
        }

        stage('Get Latest Commit ID') {
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    LATEST_COMMIT_ID = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                }
            }
        }

        stage('Build service') {
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    echo "Building ${SERVICE}....."
                    sh "./mvnw clean package -DskipTests=true -f spring-petclinic-${SERVICE}"
                }
            }
        }

        stage('Test service') {
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    echo "Testing ${SERVICE}........"
                    sh "./mvnw test -f spring-petclinic-${SERVICE}"
                    if (SERVICE != 'genai-service') {
                        junit "spring-petclinic-${SERVICE}/target/surefire-reports/*.xml"
                        jacoco execPattern: "spring-petclinic-${SERVICE}/target/jacoco.exec", classPattern: "spring-petclinic-${SERVICE}/target/classes", sourcePattern: "spring-petclinic-${SERVICE}/src/main/java"
                    }
                }
            }
        }

        stage('Build and push image to Docker Hub') {
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    echo "Building image for ${SERVICE}....."
                    sh "./mvnw clean install -P buildDocker -f spring-petclinic-${SERVICE}"

                    echo "Retag image for ${SERVICE}....."
                    sh "docker tag 22127025/devops-project2/spring-petclinic-${SERVICE}:latest 22127025/devops-project2:${LATEST_COMMIT_ID}"

                    echo "Pushing image to DockerHub for ${SERVICE}....."
                    withDockerRegistry(credentialsId: 'dockerhub-token', url: 'https://index.docker.io/v1/') {
                        sh "docker push 22127025/devops-project2:${LATEST_COMMIT_ID}"
                    }
                }
            }
        }

        stage('Apply k8s') {
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    echo "Deploying ${SERVICE} to k8s cluster....."
                    sh "helm upgrade --install --namespace=test-${LATEST_COMMIT_ID} --create-namespace ${SERVICE}-${LATEST_COMMIT_ID} -f $helmValues $helmChart --set image.repository=22127025/devops-project2 --set image.tag=${LATEST_COMMIT_ID}"
                }
            }
        }
    }
}
