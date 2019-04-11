
def label = "petclinic-${UUID.randomUUID().toString()}"

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

  def image = "jenkins/jnlp-slave"
  node(label) {
    stage("Build artifacts") {
      checkout scm
      container('docker') {
        sh "docker version"
        sh "docker build -t builder:${BUILD_TAG} --target builder ."
      }
    }
  }
}
