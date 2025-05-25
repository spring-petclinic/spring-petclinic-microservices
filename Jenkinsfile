pipeline {
    agent any

    environment {
        SERVICES = "spring-petclinic-vets-service,spring-petclinic-customers-service,spring-petclinic-visits-service,spring-petclinic-admin-server,spring-petclinic-api-gateway,spring-petclinic-config-server,spring-petclinic-genai-service,spring-petclinic-discovery-server"
        DOCKER_REGISTRY = "devopshcmus"
    }

    stages {
        stage('Detect Changed Services') {
            steps {
                script {
                    def changedFiles = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim().split("\n")
                    def affectedServices = []

                    SERVICES.split(',').each { service ->
                        if (changedFiles.find { it.startsWith(service + "/") }) {
                            affectedServices.add(service)
                        }
                    }

                    if (affectedServices.isEmpty()) {
                        echo "No services changed. Skipping build."
                        currentBuild.result = 'SUCCESS'
                        return
                    }

                    env.BUILD_SERVICES = affectedServices.join(',')
                }
            }
        }

        stage('Docker Login') {
            when {
                expression { return env.BUILD_SERVICES }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                }
            }
        }

//         stage('Build and Push Docker Images') {
//             when {
//                 expression { return env.BUILD_SERVICES }
//             }
//             steps {
//                 script {
//                     def commitId = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
//                     def branch = env.BRANCH_NAME ?: sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
//                     def isMain = (branch == 'main')
//
//                     env.BUILD_SERVICES.split(',').each { service ->
//                         echo "Building and pushing Docker image for ${service} with tag ${commitId}..."
//
//                         sh "./mvnw clean install -pl ${service} -Dmaven.test.skip=true -P buildDocker -Ddocker.image.prefix=${env.DOCKER_REGISTRY} -Ddocker.image.tag=${commitId} -Dcontainer.build.extraarg=--push"
//
//                         if (isMain) {
//                             echo "Tagging and pushing ${service} as latest"
//                             def imageExists = sh(script: "docker images -q ${env.DOCKER_REGISTRY}/${service}:${commitId}", returnStdout: true).trim()
//                             if (imageExists) {
//                                 sh """
//                                     docker tag ${env.DOCKER_REGISTRY}/${service}:${commitId} ${env.DOCKER_REGISTRY}/${service}:latest
//                                     docker push ${env.DOCKER_REGISTRY}/${service}:latest
//                                 """
//                             } else {
//                                 echo "Image ${env.DOCKER_REGISTRY}/${service}:${commitId} not found locally. Skipping tag latest."
//                             }
//                         }
//                     }
//                 }
//             }
//         }

       stage('Build and Push Docker Images') {
           when {
               expression { return env.BUILD_SERVICES }
           }
           steps {
               script {
                   def commitId = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                   def branch = env.BRANCH_NAME ?: sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
                   def isMain = (branch == 'main')
                   def gitopsRepo = "https://github.com/nguyenvinhluong242004/petclinic-argocd.git"
                   def gitopsDir = "petclinic-argocd"
                   def chartPath = "charts/petclinic"


                   // Lấy tag release nếu có, từ commit hiện tại
                   def releaseTag = ''
                   if (isMain) {
                       sh 'git fetch --tags'
                       releaseTag = sh(
                           script: "git tag --points-at HEAD | grep -E '^v[0-9]+\\.[0-9]+\\.[0-9]+' || true",
                           returnStdout: true
                       ).trim()
                       echo "Tags on HEAD: '${releaseTag}'"
                   }
                   echo "RELEASE TAG: ${releaseTag}"
                   // Nếu không có tag release thì lấy commitId làm tag
                   def imageTag = releaseTag ?: commitId
                   echo "Using image tag: ${imageTag}"

                   env.BUILD_SERVICES.split(',').each { service ->
                       echo "Building and pushing Docker image for ${service} with tag ${imageTag}..."

                       sh "./mvnw clean install -pl ${service} -Dmaven.test.skip=true -P buildDocker -Ddocker.image.prefix=${env.DOCKER_REGISTRY} -Ddocker.image.tag=${imageTag} -Dcontainer.build.extraarg=--push"
                   }
                   if (isMain) {

                        // Clone repo GitOps
                        sh """
                            rm -rf ${gitopsDir}
                            git clone ${gitopsRepo}
                        """

                        env.BUILD_SERVICES.split(',').each { service ->
                            def devValuesPath = "${chartPath}/values-dev.yaml"
                            def stagingValuesPath = "${chartPath}/values-staging.yaml"

                            def serviceKeyMap = [
                                "api-gateway": "apiGateway",
                                "discovery-server": "discoveryServer",
                                "customers-service": "customersService",
                                "vets-service": "vetsService",
                                "visits-service": "visitsService",
                                "genai-service": "genaiService",
                                "admin-server": "adminServer",
                                "config-server": "configServer"
                            ]
                            def yamlKey = serviceKeyMap[service] ?: service

                            if (releaseTag) {
                                echo "Updating ${yamlKey}.tag in values-staging.yaml for ${service}..."
                                sh """
                                    sed -i 's|\\(${yamlKey}:\\s*\\n\\s*  repository:.*\\n\\s*  tag:\\s*\\).*|\\1${commitId}|' ${stagingValuesPath}
                                """
                            }
                            else {
                                echo "Updating ${yamlKey}.tag in values-dev.yaml for ${service}..."
                                sh """
                                    sed -i 's|\\(${yamlKey}:\\s*\\n\\s*  repository:.*\\n\\s*  tag:\\s*\\).*|\\1${commitId}|' ${devValuesPath}
                                """
                            }
                        }

                        withCredentials([usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'username', passwordVariable: 'pass')]) {
                            dir("${gitopsDir}") {
                                sh """
                                    git config user.email "\${username}@users.noreply.github.com"
                                    git config user.name "\${username}"
                                    git remote set-url origin https://\${username}:\${pass}@github.com/nguyenvinhluong242004/petclinic-argocd.git
                                    git add charts/petclinic/values-*.yaml
                                    git commit -m "Update image tags to \${commitId} for changed services [ci skip]" || echo "No changes to commit"
                                    git push origin \${branch}
                                """
                            }
                        }
                   }
               }
           }
       }

        stage('Docker Logout') {
            when {
                expression { return env.BUILD_SERVICES }
            }
            steps {
                sh 'docker logout'
            }
        }
    }
}
