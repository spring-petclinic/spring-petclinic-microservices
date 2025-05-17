// Jenkinsfile
pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = "docker.io"
        DOCKER_REPOSITORY = "vuhoabinhthachhoa"
        GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        HELM_REPO_URL = "https://github.com/OpsInUs/DA02-HelmRepo.git"
        HELM_REPO_BRANCH = "main"
    }

    stages {
        stage('Determine Environment and Tag') {
    steps {
        script {
            // Add debug info
            echo "BRANCH_NAME: ${env.BRANCH_NAME}"
            echo "GIT_BRANCH: ${env.GIT_BRANCH}"
            
            // Check current branch another way
            sh "git branch --show-current"
            sh "git rev-parse --abbrev-ref HEAD"
            
            // Check if this is a tagged commit
            def isTagged = sh(script: "git tag --points-at HEAD || echo ''", returnStdout: true).trim()
            
            // Detect branch more reliably
            def currentBranch = sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
            
            if (isTagged) {
                // Tagged commit = staging environment
                env.TARGET_ENV = 'staging'
                env.IMAGE_TAG = isTagged
                echo "Tagged commit detected: ${isTagged}. Targeting staging environment."
            } else if (currentBranch == 'main' || currentBranch == 'master' || env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'origin/main' || env.GIT_BRANCH == 'main' || env.GIT_BRANCH == 'origin/main') {
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
                    try {
                        if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'origin/main') {
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
                        
                        // Store as a comma-separated string and also keep the list
                        env.CHANGED_SERVICES = changedServices.join(',')
                        // Store in a global variable to avoid issues with env variables
                        SERVICES_TO_BUILD = changedServices
                        
                        echo "Services to build: ${env.CHANGED_SERVICES}"
                    } catch (Exception e) {
                        echo "Error detecting changed services: ${e.message}. Building all services."
                        def services = ['spring-petclinic-admin-server', 
                                       'spring-petclinic-api-gateway', 
                                       'spring-petclinic-config-server',
                                       'spring-petclinic-customers-service',
                                       'spring-petclinic-discovery-server',
                                       'spring-petclinic-genai-service',
                                       'spring-petclinic-vets-service',
                                       'spring-petclinic-visits-service']
                        env.CHANGED_SERVICES = services.join(',')
                        SERVICES_TO_BUILD = services
                    }
                }
            }
        }

        stage('Build and Push Docker Images') {
            steps {
                script {
                    // Use the global variable instead of splitting env.CHANGED_SERVICES again
                    def services = SERVICES_TO_BUILD ?: []
                    
                    if (services.isEmpty()) {
                        echo "No services to build. Skipping Docker build stage."
                        return
                    }
                    
                    try {
                        withCredentials([string(credentialsId: 'docker-hub-token', variable: 'DOCKER_HUB_TOKEN')]) {
                            // Use single quotes for better security
                            sh '''
                                echo "${DOCKER_HUB_TOKEN}" | docker login -u ${DOCKER_REPOSITORY} --password-stdin ${DOCKER_REGISTRY}
                            '''
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
                                docker build \\
                                    -t ${DOCKER_REPOSITORY}/${service}:${env.IMAGE_TAG} \\
                                    -f ../docker/Dockerfile \\
                                    --build-arg ARTIFACT_NAME=target/${service}-3.4.1 \\
                                    --build-arg EXPOSED_PORT=8080 . || exit 1

                                docker push ${DOCKER_REPOSITORY}/${service}:${env.IMAGE_TAG} || exit 1
                                """
                            }
                        }
                    } catch (Exception e) {
                        echo "Error in Docker build stage: ${e.message}"
                        throw e
                    }
                }
            }
        }

        // stage('Update Helm Values') {
        //     steps {
        //         script {
        //             // For public repositories, we don't need SSH keys for cloning
        //             sh """
        //                 # Clone the public Helm repository
        //                 git clone ${HELM_REPO_URL} helm-repo
        //                 cd helm-repo
        //             """
                    
        //             // Make sure the directory structure exists
        //             sh "mkdir -p helm-repo/env/${env.TARGET_ENV}"
                    
        //             // Debug - list what we have
        //             sh "ls -la helm-repo/env/"
                    
        //             // Update the appropriate environment's values.yaml file
        //             def services = env.CHANGED_SERVICES.split(',')
        //             services.each { service ->
        //                 // Convert from service dir name to helm service name
        //                 def helmServiceName = service.replace('spring-petclinic-', '')
                        
        //                 // Check if the yq tool is available
        //                 sh "which yq || echo 'YQ not found'"
                        
        //                 // Update image tag in values file using yq
        //                 sh """
        //                     # Update image tag in the target environment's values file
        //                     yq e -i '.services.${helmServiceName}.image.tag = "${env.IMAGE_TAG}"' helm-repo/env/${env.TARGET_ENV}/values.yaml || echo "YQ command failed - ensure yq is installed"
        //                 """
        //             }
                    
        //             // For pushing changes, we still need credentials
        //             withCredentials([usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
        //                 sh """
        //                     cd helm-repo
        //                     git config --global user.email "htkt004@gmail.com"
        //                     git config --global user.name "Tuyen572004"
                            
        //                     # Configure git to use https with credentials
        //                     git config credential.helper store
        //                     echo "https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com" > ~/.git-credentials
                            
        //                     # Add, commit, and push changes
        //                     git add env/${env.TARGET_ENV}/values.yaml
        //                     git commit -m "Update ${env.TARGET_ENV} environment with ${env.IMAGE_TAG} tag for ${env.CHANGED_SERVICES}" || echo "No changes to commit"
        //                     git push origin ${HELM_REPO_BRANCH}
                            
        //                     # Remove credentials file for security
        //                     rm ~/.git-credentials
        //                 """
        //             }
        //         }
        //     }
        // }
    }

    post {
        always {
            script {
                try {
                    // Safe cleanup operations
                    echo "Running cleanup tasks..."
                    sh 'docker system prune -f || true'  // The '|| true' makes it continue even if this command fails
                    cleanWs()  // Clean workspace
                } catch (Exception e) {
                    echo "Warning: Cleanup tasks encountered an issue: ${e.message}"
                    // Continue without failing the build
                }
            }
        }
    }
}