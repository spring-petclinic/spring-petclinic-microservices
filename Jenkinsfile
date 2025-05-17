// Jenkinsfile
pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = "docker.io"
        DOCKER_REPOSITORY = "vuhoabinhthachhoa" // Change to your Docker Hub username or private registry
        GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        HELM_REPO_URL = "https://github.com/OpsInUs/DA02-HelmRepo.git" // Your Helm repo URL
        HELM_REPO_BRANCH = "main"
        CHANGED_SERVICES = ""
    }

    stages {
        stage('Determine Changed Services') {
            steps {
                script {
                    // Determine the base commit to compare with
                    def baseCommit = ""
                    if (env.BRANCH_NAME == 'main') {
                        // For main branch, compare with the previous commit
                        baseCommit = sh(script: "git rev-parse HEAD~1", returnStdout: true).trim()
                    } else {
                        // For feature branches, compare with the main branch
                        baseCommit = sh(script: "git merge-base origin/main HEAD", returnStdout: true).trim()
                    }

                    // Get list of changed files
                    def changedFiles = sh(script: "git diff --name-only ${baseCommit} HEAD", returnStdout: true).trim()
                    
                    // Check which services have changed
                    def services = ['spring-petclinic-admin-server', 
                                   'spring-petclinic-api-gateway', 
                                   'spring-petclinic-config-server',
                                   'spring-petclinic-customers-service',
                                   'spring-petclinic-discovery-server',
                                   'spring-petclinic-genai-service',
                                   'spring-petclinic-vets-service',
                                   'spring-petclinic-visits-service']
                    
                    def changedServices = []
                    services.each { service ->
                        if (changedFiles.contains(service)) {
                            changedServices.add(service)
                        }
                    }
                    
                    // If no services have changed, build all (could be a shared dependency)
                    if (changedServices.size() == 0) {
                        changedServices = services
                    }
                    
                    CHANGED_SERVICES = changedServices.join(',')
                    echo "Changed services: ${CHANGED_SERVICES}"
                }
            }
        }

        stage('Build and Push Docker Images') {
    steps {
        script {
            def services = CHANGED_SERVICES.split(',')
            
            withCredentials([string(credentialsId: 'docker-hub-token', variable: 'DOCKER_HUB_TOKEN')]) {
                sh "docker login -u ${DOCKER_REPOSITORY} -p ${DOCKER_HUB_TOKEN} ${DOCKER_REGISTRY}"
            }
            
            services.each { service ->
                echo "Building ${service}"
                
                // Show current directory and list files for debugging
                sh "pwd && ls -la"
                
                // Check if service directory exists
                sh "ls -la ${service} || echo 'Service directory not found'"
                
                // Build with Maven
                dir(service) {
                    // Check for Maven wrapper
                    sh "ls -la ./mvnw || ls -la ../mvnw || echo 'Maven wrapper not found'"
                    
                    // Try to run Maven build, with fallback options
                    sh """
                    if [ -f "./mvnw" ]; then
                        chmod +x ./mvnw
                        ./mvnw clean package -DskipTests
                    elif [ -f "../mvnw" ]; then
                        chmod +x ../mvnw
                        ../mvnw clean package -DskipTests
                    else
                        mvn clean package -DskipTests
                    fi
                    """
                    
                    // Check if target JAR exists
                    sh "ls -la target/ || echo 'Target directory not found'"
                    
                    // Add sleep for debugging if needed
                    // sh "sleep 10"
                    
                    // Docker build with better error handling
                    // Docker build with better error handling
                    sh """
                    docker build \
                        -t ${DOCKER_REPOSITORY}/${service}:latest \
                        -f ../docker/Dockerfile \
                        --build-arg ARTIFACT_NAME=target/${service}-3.4.1 \
                        --build-arg EXPOSED_PORT=8080 . || exit 1

                    docker push ${DOCKER_REPOSITORY}/${service}:latest || exit 1

                    # Tag with commit SHA
                    docker tag ${DOCKER_REPOSITORY}/${service}:latest ${DOCKER_REPOSITORY}/${service}:${GIT_COMMIT_SHORT} || exit 1
                    docker push ${DOCKER_REPOSITORY}/${service}:${GIT_COMMIT_SHORT} || exit 1
                    """
                }
            }
        }
    }
}

        stage('Update Helm Values for Dev') {
            when {
                expression { env.BRANCH_NAME == 'main' }
            }
            steps {
                script {
                    withCredentials([sshUserPrivateKey(credentialsId: 'github-ssh-key', keyFileVariable: 'SSH_KEY')]) {
                        sh """
                            git config --global user.email "htkt004@gmail.com"
                            git config --global user.name "Tuyen572004"
                            mkdir -p ~/.ssh
                            ssh-keyscan github.com >> ~/.ssh/known_hosts
                            GIT_SSH_COMMAND="ssh -i ${SSH_KEY}" git clone ${HELM_REPO_URL} helm-repo
                            cd helm-repo
                            
                            # Set image registry annotation for ArgoCD Image Updater to detect changes
                            for service in \$(echo ${CHANGED_SERVICES} | tr ',' ' '); do
                                echo "Setting latest tag for \${service} in dev values"
                                yq e -i '.services.'\${service}'.image.tag = "latest"' env/dev/values.yaml
                                # We don't need to change anything in the values file since we're using the same latest tag
                                # This commit is primarily to trigger ArgoCD sync
                            done
                            
                            git add env/dev/values.yaml
                            git commit -m "Update dev image annotation to trigger ArgoCD sync for ${CHANGED_SERVICES}" || echo "No changes to commit"
                            GIT_SSH_COMMAND="ssh -i ${SSH_KEY}" git push origin ${HELM_REPO_BRANCH}
                        """
                    }
                }
            }
        }

        stage('Update Helm Values for Staging') {
            when {
                expression { env.TAG_NAME != null }
            }
            steps {
                script {
                    withCredentials([sshUserPrivateKey(credentialsId: 'github-ssh-key', keyFileVariable: 'SSH_KEY')]) {
                        sh """
                            git config --global user.email "jenkins@example.com"
                            git config --global user.name "Jenkins"
                            mkdir -p ~/.ssh
                            ssh-keyscan github.com >> ~/.ssh/known_hosts
                            GIT_SSH_COMMAND="ssh -i ${SSH_KEY}" git clone ${HELM_REPO_URL} helm-repo
                            cd helm-repo
                            
                            # Update image tags in staging values.yaml to match git tag
                            for service in \$(echo ${CHANGED_SERVICES} | tr ',' ' '); do
                                echo "Setting ${TAG_NAME} tag for \${service} in staging values"
                                yq e -i '.services.'\${service}'.image.tag = "${TAG_NAME}"' env/staging/values.yaml
                            done
                            
                            git add env/staging/values.yaml
                            git commit -m "Update staging image tags to ${TAG_NAME} for ${CHANGED_SERVICES}" || echo "No changes to commit"
                            GIT_SSH_COMMAND="ssh -i ${SSH_KEY}" git push origin ${HELM_REPO_BRANCH}
                        """
                    }
                }
            }
        }

        stage('Update Helm Values for Dev-Review') {
            when {
                expression { env.BRANCH_NAME != 'main' && env.TAG_NAME == null }
            }
            steps {
                script {
                    withCredentials([sshUserPrivateKey(credentialsId: 'github-ssh-key', keyFileVariable: 'SSH_KEY')]) {
                        sh """
                            git config --global user.email "jenkins@example.com"
                            git config --global user.name "Jenkins"
                            mkdir -p ~/.ssh
                            ssh-keyscan github.com >> ~/.ssh/known_hosts
                            GIT_SSH_COMMAND="ssh -i ${SSH_KEY}" git clone ${HELM_REPO_URL} helm-repo
                            cd helm-repo
                            
                            # Update image tags in dev-review values.yaml with commit hash
                            for service in \$(echo ${CHANGED_SERVICES} | tr ',' ' '); do
                                echo "Setting ${GIT_COMMIT_SHORT} tag for \${service} in dev-review values"
                                yq e -i '.services.'\${service}'.image.tag = "${GIT_COMMIT_SHORT}"' env/dev-review/values.yaml
                            done
                            
                            git add env/dev-review/values.yaml
                            git commit -m "Update dev-review image tags to ${GIT_COMMIT_SHORT} for ${CHANGED_SERVICES}" || echo "No changes to commit"
                            GIT_SSH_COMMAND="ssh -i ${SSH_KEY}" git push origin ${HELM_REPO_BRANCH}
                        """
                    }
                    
                    echo "To deploy to dev-review environment, manually trigger the 'developer_build' job with parameters: GIT_COMMIT_SHORT=${GIT_COMMIT_SHORT}"
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
    }
}

// trigger jenkins