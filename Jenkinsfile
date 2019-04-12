properties([buildDiscarder(logRotator(numToKeepStr: '8'))])

def label = "petclinic-${UUID.randomUUID().toString()}"

def revision = "2.1.3-SNAPSHOT"

podTemplate(label: label, yaml: """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: docker
    image: docker:18.09
    command: ['cat']
    tty: true
    volumeMounts:
    - name: dockersock
      mountPath: /var/run/docker.sock
  volumes:
  - name: dockersock
    hostPath:
      path: /var/run/docker.sock
"""
  ) {

  node(label) {
    checkout scm
    container('docker') {
      stage("Build images") {
        sh "docker version"
        sh "docker build -t builder:${BUILD_TAG} --target builder --build-arg REVISION=${revision} ."
        sh "docker build -t base:${BUILD_TAG} --target base --build-arg REVISION=${revision} ."
        sh "docker build -t jcsirot/spring-petclinic-admin-server:${revision} -f spring-petclinic-admin-server/Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=9090 ."
        sh "docker build -t jcsirot/spring-petclinic-customers-service:${revision} -f spring-petclinic-customers-service/Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8081 ."
        sh "docker build -t jcsirot/spring-petclinic-vets-service:${revision} -f spring-petclinic-vets-service/Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8081 ."
        sh "docker build -t jcsirot/spring-petclinic-visits-service:${revision} -f spring-petclinic-visits-service/Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8081 ."
        sh "docker build -t jcsirot/spring-petclinic-config-server:${revision} -f spring-petclinic-config-server/Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8888 ."
        sh "docker build -t jcsirot/spring-petclinic-discovery-server:${revision} -f spring-petclinic-discovery-server/Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8761 ."
        sh "docker build -t jcsirot/spring-petclinic-api-gateway:${revision} -f spring-petclinic-api-gateway/Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8081 ."
        sh "docker build -t jcsirot/spring-petclinic-hystrix-dashboard:${revision} -f spring-petclinic-hystrix-dashboard/Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=7979 ."
      }
      stage("Sonar Analysis") {
        withSonarQubeEnv('sonarqube') {
          sh "docker build -f sonar.Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg SONAR_MAVEN_GOAL=${SONAR_MAVEN_GOAL} --build-arg SONAR_HOST_URL=${SONAR_HOST_URL} --build-arg SONAR_AUTH_TOKEN=${SONAR_AUTH_TOKEN} ."
        }
      }
    }
  }
}
