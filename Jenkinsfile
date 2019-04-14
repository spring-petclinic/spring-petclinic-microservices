properties([buildDiscarder(logRotator(numToKeepStr: '8'))])

def label = "petclinic-${UUID.randomUUID().toString()}"

def revision = "2.1.3-SNAPSHOT"

def credentials = [usernamePassword(credentialsId: 'jcsirot.docker.devoxxfr.chelonix.org', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]

podTemplate(label: label, yaml: """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: docker
    image: docker:19.03-rc
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
        sh "docker build -t docker.devoxxfr.chelonix.org/jcsirot/spring-petclinic-admin-server:${revision} -f spring-petclinic-admin-server/Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=9090 ."
        sh "docker build -t docker.devoxxfr.chelonix.org/jcsirot/spring-petclinic-customers-service:${revision} -f spring-petclinic-customers-service/Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8081 ."
        sh "docker build -t docker.devoxxfr.chelonix.org/jcsirot/spring-petclinic-vets-service:${revision} -f spring-petclinic-vets-service/Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8081 ."
        sh "docker build -t docker.devoxxfr.chelonix.org/jcsirot/spring-petclinic-visits-service:${revision} -f spring-petclinic-visits-service/Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8081 ."
        sh "docker build -t docker.devoxxfr.chelonix.org/jcsirot/spring-petclinic-config-server:${revision} -f spring-petclinic-config-server/Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8888 ."
        sh "docker build -t docker.devoxxfr.chelonix.org/jcsirot/spring-petclinic-discovery-server:${revision} -f spring-petclinic-discovery-server/Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8761 ."
        sh "docker build -t docker.devoxxfr.chelonix.org/jcsirot/spring-petclinic-api-gateway:${revision} -f spring-petclinic-api-gateway/Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8081 ."
        sh "docker build -t docker.devoxxfr.chelonix.org/jcsirot/spring-petclinic-hystrix-dashboard:${revision} -f spring-petclinic-hystrix-dashboard/Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=7979 ."
      }
      stage("Sonar Analysis") {
        withSonarQubeEnv('sonarqube') {
          sh "docker build -f sonar.Dockerfile --build-arg BASE_ID=${BUILD_TAG} --build-arg REVISION=${revision} --build-arg SONAR_MAVEN_GOAL=${SONAR_MAVEN_GOAL} --build-arg SONAR_HOST_URL=${SONAR_HOST_URL} --build-arg SONAR_AUTH_TOKEN=${SONAR_AUTH_TOKEN} ."
        }
      }
      stage("Push images") {
        withCredentials(credentials) {
          sh "docker login -u ${DOCKER_USER} -p ${DOCKER_PASSWORD} docker.devoxxfr.chelonix.org"
          sh "docker push docker.devoxxfr.chelonix.org/jcsirot/spring-petclinic-admin-server:${revision}"
          sh "docker push docker.devoxxfr.chelonix.org/jcsirot/spring-petclinic-customers-service:${revision}"
          sh "docker push docker.devoxxfr.chelonix.org/jcsirot/spring-petclinic-vets-service:${revision}"
          sh "docker push docker.devoxxfr.chelonix.org/jcsirot/spring-petclinic-visits-service:${revision}"
          sh "docker push docker.devoxxfr.chelonix.org/jcsirot/spring-petclinic-config-server:${revision}"
          sh "docker push docker.devoxxfr.chelonix.org/jcsirot/spring-petclinic-discovery-server:${revision}"
          sh "docker push docker.devoxxfr.chelonix.org/jcsirot/spring-petclinic-api-gateway:${revision}"
          sh "docker push docker.devoxxfr.chelonix.org/jcsirot/spring-petclinic-hystrix-dashboard:${revision}"
        }
      }
    }
  }
}
