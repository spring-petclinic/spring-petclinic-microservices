pipeline {
  agent any

  environment {
    DOCKER_REGISTRY = "devopshcmus"
    COMPOSE_FILE = "docker-compose.yml"
  }

//   tools {
//     jdk 'JDK17'
//   }

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

          withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
            sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'

            // Build all services with docker-compose
            sh "docker-compose -f ${env.COMPOSE_FILE} build"

            // List all service names from docker-compose (hardcoded here or you can parse the file)
            def services = [
              "spring-petclinic-config-server",
              "spring-petclinic-discovery-server",
              "spring-petclinic-customers-service",
              "spring-petclinic-visits-service",
              "spring-petclinic-vets-service",
              "spring-petclinic-genai-service",
              "spring-petclinic-api-gateway",
              "spring-petclinic-admin-server"
            ]

            // Tag & push images with commitId
            services.each { service ->
              def localImage = "springcommunity/${service}"
              def targetImage = "${env.DOCKER_REGISTRY}/${service}"

              echo "Tagging image ${localImage} as ${targetImage}:${commitId}"
              sh "docker tag ${localImage} ${targetImage}:${commitId}"
              sh "docker push ${targetImage}:${commitId}"

              if (isMain) {
                // Tag thêm latest cho main branch
                echo "Tagging image ${targetImage}:${commitId} as latest"
                sh "docker tag ${targetImage}:${commitId} ${targetImage}:latest"
                sh "docker push ${targetImage}:latest"
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
