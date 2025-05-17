// Jenkinsfile
pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = "docker.io"
        DOCKER_REPOSITORY = "vuhoabinhthachhoa"
        GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        HELM_REPO_URL = "https://github.com/OpsInUs/DA02-HelmRepo.git"
        HELM_REPO_BRANCH = "main"
        CHANGED_SERVICES = ""
        TARGET_ENV = ""
        IMAGE_TAG = ""
    }

    stages {
        stage('Determine Environment and Tag') {
            steps {
                script {
                    // Check if this is a tagged commit
                    def isTagged = sh(script: "git tag --points-at HEAD || echo ''", returnStdout: true).trim()
                    
                    if (isTagged) {
                        // Tagged commit = staging environment
                        env.TARGET_ENV = 'staging'
                        env.IMAGE_TAG = isTagged
                        echo "Tagged commit detected: ${isTagged}. Targeting staging environment."
                    } else if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'origin/main') {
                        // Main branch without tag = dev environment
                        env.TARGET_ENV = 'dev'
                        env.IMAGE_TAG = 'latest'
                        echo "Main branch commit detected. Targeting dev environment."
                    } else {
                        // Other branch = dev-review
                        env.TARGET_ENV = 'dev-review'
                        env.IMAGE_TAG = env.GIT_COMMIT_SHORT
                        echo "Feature branch commit detected. Targeting dev-review environment."
                    }
                    
                    echo "Selected environment: ${env.TARGET_ENV} with image tag: ${env.IMAGE_TAG}"
                }
            }
        }

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
                        baseCommit = sh(script: "git merge-base origin/main HEAD || git rev-parse HEAD~1", returnStdout: true).trim()
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
                            echo "Service detected as changed: ${service}"
                        }
                    }
                    
                    // If no services have changed, build all (could be a shared dependency)
                    if (changedServices.size() == 0) {
                        changedServices = services
                        echo "No specific services changed. Building all services."
                    }
                    
                    env.CHANGED_SERVICES = changedServices.join(',')
                }
            }
        }

        stage('Build and Push Docker Images') {
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(',')
                    
                    withCredentials([string(credentialsId: 'docker-hub-token', variable: 'DOCKER_HUB_TOKEN')]) {
                        // Safer login with password-stdin
                        sh """
                            echo "${DOCKER_HUB_TOKEN}" | docker login -u ${DOCKER_REPOSITORY} --password-stdin ${DOCKER_REGISTRY}
                        """
                    }
                    
                    services.each { service ->
                        echo "Building ${service} for ${env.TARGET_ENV} environment with tag: ${env.IMAGE_TAG}"
                        
                        // Build with Maven
                        dir(service) {
                            // Check for Maven wrapper and make it executable
                            def mvnwExists = sh(script: "test -f ./mvnw && echo 'yes' || echo 'no'", returnStdout: true).trim()
                            def mvnwParentExists = sh(script: "test -f ../mvnw && echo 'yes' || echo 'no'", returnStdout: true).trim()
                            
                            if (mvnwExists == 'yes') {
                                sh "chmod +x ./mvnw"
                                sh "./mvnw clean package -DskipTests"
                            } else if (mvnwParentExists == 'yes') {
                                sh "chmod +x ../mvnw"
                                sh "../mvnw clean package -DskipTests"
                            } else {
                                sh "mvn clean package -DskipTests"
                            }
                            
                            // Verify JAR was created
                            sh "ls -la target/ || echo 'Target directory not found'"
                            
                            // Build Docker image with appropriate tag
                            sh """
                            docker build \
                                -t ${DOCKER_REPOSITORY}/${service}:${env.IMAGE_TAG} \
                                -f ../docker/Dockerfile \
                                --build-arg ARTIFACT_NAME=target/${service}-3.4.1 \
                                --build-arg EXPOSED_PORT=8080 . || exit 1

                            docker push ${DOCKER_REPOSITORY}/${service}:${env.IMAGE_TAG} || exit 1
                            """
                            
                            // For dev and staging, always tag with commit SHA for traceability
                            // but don't push this tag for dev and staging since it's not needed externally
                            if (env.TARGET_ENV == 'dev' || env.TARGET_ENV == 'staging') {
                                sh "docker tag ${DOCKER_REPOSITORY}/${service}:${env.IMAGE_TAG} ${DOCKER_REPOSITORY}/${service}:${GIT_COMMIT_SHORT}"
                            }
                        }
                    }
                }
            }
        }

        stage('Update Helm Values') {
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
                            
                            # Update image tags in the appropriate environment file
                            for service in \$(echo ${CHANGED_SERVICES} | tr ',' ' '); do
                                echo "Setting ${env.IMAGE_TAG} tag for \${service} in ${env.TARGET_ENV} values"
                                yq e -i '.services.'\${service}'.image.tag = "${env.IMAGE_TAG}"' env/${env.TARGET_ENV}/values.yaml
                            done
                            
                            git add env/${env.TARGET_ENV}/values.yaml
                            git commit -m "Update ${env.TARGET_ENV} image tags to ${env.IMAGE_TAG} for ${CHANGED_SERVICES}" || echo "No changes to commit"
                            GIT_SSH_COMMAND="ssh -i ${SSH_KEY}" git push origin ${HELM_REPO_BRANCH}
                        """
                    }
                    
                    // For dev-review, remind to manually trigger deployment
                    if (env.TARGET_ENV == 'dev-review') {
                        echo """
                        ====================================================================
                        Images built with tag: ${env.IMAGE_TAG}
                        To deploy to dev-review environment, manually trigger the 'developer_build' job
                        with parameters: GIT_COMMIT_SHORT=${GIT_COMMIT_SHORT}
                        ====================================================================
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo "Pipeline completed successfully for ${env.TARGET_ENV} environment"
        }
        failure {
            echo "Pipeline failed for ${env.TARGET_ENV} environment"
        }
    }
}