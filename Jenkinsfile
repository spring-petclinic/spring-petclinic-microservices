pipeline {
  agent any

  environment {
    DOCKER_REGISTRY = "devopshcmus"
    COMPOSE_FILE = "docker-compose.yml"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build JARs') {
      steps {
        script {
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
          // Build từng service và copy jar về root
          services.each { svc ->
            echo "Building JAR for ${svc}"
            sh "./mvnw -pl ${svc} clean package -DskipTests"
            sh "cp ${svc}/target/${svc}.jar ./"
          }
        }
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

            services.each { service ->
              def localImage = "springcommunity/${service}"
              def targetImage = "${env.DOCKER_REGISTRY}/${service}"

              echo "Building docker image for ${service}..."
              sh """
                docker build --file docker/Dockerfile \\
                  --build-arg ARTIFACT_NAME=${service} \\
                  --build-arg EXPOSED_PORT=8080 \\
                  --tag ${targetImage}:${commitId} \\
                  .
              """

              echo "Pushing image ${targetImage}:${commitId}"
              sh "docker push ${targetImage}:${commitId}"

              if (isMain) {
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
