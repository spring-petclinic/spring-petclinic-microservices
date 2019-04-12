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
        
        sh "docker build -t jcsirot/spring-petclinic-admin-server:${revision} --build-arg SKIP_TESTS=true --target spring-petclinic-admin-server --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=9090 ."
        sh "docker build -t jcsirot/spring-petclinic-customers-service:${revision} --build-arg SKIP_TESTS=true --target spring-petclinic-customers-service --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8081 ."
        sh "docker build -t jcsirot/spring-petclinic-vets-service:${revision} --build-arg SKIP_TESTS=true --target spring-petclinic-vets-service --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8081 ."
        sh "docker build -t jcsirot/spring-petclinic-visits-service:${revision} --build-arg SKIP_TESTS=true --target spring-petclinic-visits-service --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8081 ."
        sh "docker build -t jcsirot/spring-petclinic-config-server:${revision} --build-arg SKIP_TESTS=true --target spring-petclinic-config-server --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8888 ."
        sh "docker build -t jcsirot/spring-petclinic-discovery-server:${revision} --build-arg SKIP_TESTS=true --target spring-petclinic-discovery-server --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8761 ."
        sh "docker build -t jcsirot/spring-petclinic-api-gateway:${revision} --build-arg SKIP_TESTS=true --target spring-petclinic-api-gateway --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=8081 ."
        sh "docker build -t jcsirot/spring-petclinic-hystrix-dashboard:${revision} --build-arg SKIP_TESTS=true --target spring-petclinic-hystrix-dashboard --build-arg REVISION=${revision} --build-arg EXPOSED_PORT=7979 ."
      }
      stage("Sonar Analysis") {
        withSonarQubeEnv('sonarqube') {
          sh "docker build --target sonar --build-arg REVISION=${revision} --build-arg SONAR_MAVEN_GOAL=${SONAR_MAVEN_GOAL} --build-arg SONAR_HOST_URL=${SONAR_HOST_URL} --build-arg SONAR_AUTH_TOKEN=${SONAR_AUTH_TOKEN} ."
        }
      }
    }
  }
}
