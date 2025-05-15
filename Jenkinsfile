pipeline {
  agent any

  environment {
    DOCKER_REGISTRY = "devopshcmus"
    DOCKERFILE_PATH = "docker/Dockerfile"  // đường dẫn tới Dockerfile chung
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Push Docker Images') {
      steps {
        script {
          def commitId = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
          env.COMMIT_ID = commitId

          def branch = env.BRANCH_NAME ?: "unknown"
          def isMain = branch == 'main'

          echo "Building images for branch: ${branch} with commit: ${commitId}"

          // List các service với port tương ứng (port lấy theo docker-compose)
          def services = [
            [name: "spring-petclinic-config-server", port: 8888],
            [name: "spring-petclinic-discovery-server", port: 8761],
            [name: "spring-petclinic-customers-service", port: 8081],
            [name: "spring-petclinic-visits-service", port: 8082],
            [name: "spring-petclinic-vets-service", port: 8083],
            [name: "spring-petclinic-genai-service", port: 8084],
            [name: "spring-petclinic-api-gateway", port: 8080],
            [name: "spring-petclinic-admin-server", port: 9090]
          ]

          withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
            sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'

            services.each { svc ->
              echo "Building ${svc.name}..."

              // Build image
              sh """
                docker build \\
                  --file ${env.DOCKERFILE_PATH} \\
                  --build-arg ARTIFACT_NAME=${svc.name} \\
                  --build-arg EXPOSED_PORT=${svc.port} \\
                  --tag ${env.DOCKER_REGISTRY}/${svc.name}:${commitId} \\
                  .
              """

              // Push image
              sh "docker push ${env.DOCKER_REGISTRY}/${svc.name}:${commitId}"

              if (isMain) {
                sh """
                  docker tag ${env.DOCKER_REGISTRY}/${svc.name}:${commitId} ${env.DOCKER_REGISTRY}/${svc.name}:latest
                  docker push ${env.DOCKER_REGISTRY}/${svc.name}:latest
                """
              }
            }

            sh "docker logout"
          }
        }
      }
    }
  }

  post {
    always {
      echo "Build finished with status: ${currentBuild.currentResult}"
    }
  }
}
