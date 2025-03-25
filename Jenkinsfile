def SERVICES_CHANGED = ""

pipeline {
    agent any

    stages {
        stage('Detect Changes') {
            // agent { label 'any' }
            steps {
                script {

                    echo "🔍 Checking if the repository is shallow..."
                    def isShallow = sh(script: "git rev-parse --is-shallow-repository", returnStdout: true).trim()
                    echo "⏳ Is repository shallow? ${isShallow}"

                    // Ensure the full git history is available for accurate change detection
                    if (isShallow == "true") {
                        echo "📂 Repository is shallow. Fetching full history..."
                        sh 'git fetch origin main --prune --unshallow'
                    } else {
                        echo "✅ Repository is already complete. Skipping --unshallow."
                        sh 'git fetch origin main --prune'
                    }

                    // Fetch all branches to ensure we have the latest
                    echo "📂 Fetching all branches..."
                    sh 'git fetch --all --prune'

                    // Ensure main branch exists
                    echo "🔍 Checking if origin/main exists..."
                    def mainExists = sh(script: "git branch -r | grep 'origin/main' || echo ''", returnStdout: true).trim()

                    if (!mainExists) {
                        echo "❌ origin/main does not exist in remote. Fetching all branches..."
                        sh 'git remote set-branches --add origin main'
                        sh 'git fetch --all'

                        mainExists = sh(script: "git branch -r | grep 'origin/main' || echo ''", returnStdout: true).trim()

                        if (!mainExists) {
                            error("❌ origin/main still does not exist! Ensure the branch is available in remote.")
                        }
                    }


                    // Determine the base commit to compare against
                    def baseCommit = sh(script: "git merge-base origin/main HEAD", returnStdout: true).trim()
                    echo "🔍 Base commit: ${baseCommit}"

                    // Ensure base commit is valid
                    if (!baseCommit) {
                        error("❌ Base commit not found! Ensure 'git merge-base origin/main HEAD' returns a valid commit.")
                    }

                    // Get the list of changed files relative to the base commit
                    def changes = sh(script: "git diff --name-only ${baseCommit} HEAD", returnStdout: true).trim()

                    echo "📜 Raw changed files:\n${changes}"

                    // Ensure changes are not empty
                    if (!changes) {
                        echo "ℹ️ No changes detected. Skipping tests & build."
                        SERVICES_CHANGED = ""
                        return
                    }

                    // Convert the list into an array
                    def changedFiles = changes.split("\n")

                    // Normalize paths to ensure they match expected service directories
                    def normalizedChanges = changedFiles.collect { file ->
                        file.replaceFirst("^.*?/spring-petclinic-microservices/", "")
                    }

                    echo "✅ Normalized changed files: ${normalizedChanges.join(', ')}"

                    def services = [
                        "spring-petclinic-admin-server",
                        "spring-petclinic-api-gateway",
                        "spring-petclinic-config-server",
                        "spring-petclinic-customers-service",
                        "spring-petclinic-discovery-server",
                        "spring-petclinic-genai-service",
                        "spring-petclinic-vets-service",
                        "spring-petclinic-visits-service",
                    ]

                    // Identify which services have changes
                    def changedServices = services.findAll { service ->
                        normalizedChanges.any { file ->
                            file.startsWith("${service}/") || file.contains("${service}/")
                        }
                    }

                    echo "📢 Final changed services list: ${changedServices.join(', ')}"

                    // Ensure we have at least one changed service
                    if (changedServices.isEmpty()) {
                        echo "ℹ️ No relevant services changed. Skipping tests & build."
                        SERVICES_CHANGED = ""
                        return
                    }

                    // Use properties() to persist the value
                    properties([
                        parameters([
                            string(name: 'SERVICES_CHANGED', defaultValue: changedServices.join(','), description: 'Services that changed in this build')
                        ])
                    ])

                    SERVICES_CHANGED = changedServices.join(',')
                    echo "🚀 Services changed (Global ENV): ${SERVICES_CHANGED}"
                }
            }
        }

        // stage('Test & Coverage Check') {
        //     agent { label 'maven-node' }
        //     when {
        //         expression { SERVICES_CHANGED?.trim() != "" }
        //     }
        //     steps {
        //         script {
        //             def servicesList = SERVICES_CHANGED.tokenize(',')

        //             if (servicesList.isEmpty()) {
        //                 echo "ℹ️ No changed services found. Skipping tests."
        //                 return
        //             }

        //             // Run tests sequentially instead of in parallel
        //             for (service in servicesList) {
        //                 echo "🔬 Running tests for ${service}..."
        //                 withEnv(["MAVEN_USER_HOME=${env.WORKSPACE}/m2-wrapper-${service}"]) {
        //                     dir(service) {
        //                         sh '../mvnw clean verify -PbuildDocker jacoco:report'

        //                         def jacocoFile = sh(script: "find target -name jacoco.xml", returnStdout: true).trim()
        //                         if (!jacocoFile) {
        //                             echo "⚠️ No JaCoCo report found for ${service}."
        //                         } else {
        //                             def missed = sh(script: """
        //                                 awk -F 'missed="' '/<counter type="LINE"/ {gsub(/".*/, "", \$2); sum += \$2} END {print sum}' ${jacocoFile}
        //                             """, returnStdout: true).trim()

        //                             def covered = sh(script: """
        //                                 awk -F 'covered="' '/<counter type="LINE"/ {gsub(/".*/, "", \$2); sum += \$2} END {print sum}' ${jacocoFile}
        //                             """, returnStdout: true).trim()

        //                             def total = missed.toInteger() + covered.toInteger()
        //                             def coveragePercent = (total > 0) ? (covered.toInteger() * 100 / total) : 0

        //                             echo "🚀 Test coverage for ${service}: ${coveragePercent}%"

        //                             if (coveragePercent < 70) {
        //                                 error("❌ Test coverage below 70% for ${service}.")
        //                             }
        //                         }
        //                     }
        //                 }
        //             }
        //         }
        //     }
        // }

        // stage('Publish JaCoCo Coverage') {
        //     agent { label 'maven-node' }
        //     when {
        //         expression { SERVICES_CHANGED?.trim() != "" }
        //     }
        //     steps {
        //         script {
        //             def servicesList = SERVICES_CHANGED.tokenize(',')

        //             if (servicesList.isEmpty()) {
        //                 echo "ℹ️ No changed services found. Skipping coverage upload."
        //                 return
        //             }

        //             for (service in servicesList) {
        //                 echo "📊 Uploading JaCoCo coverage for ${service}..."
        //                 dir(service) {
        //                     jacoco(
        //                         execPattern: 'target/jacoco.exec',
        //                         classPattern: 'target/classes',
        //                         sourcePattern: 'src/main/java',
        //                         exclusionPattern: '**/test/**'
        //                     )
        //                 }
        //             }
        //         }
        //     }
        // }


        stage('Build (Maven)') {
            // agent { label 'maven-node' }
            when {
                expression { SERVICES_CHANGED?.trim() != "" }
            }
            steps {
                script {
                    def servicesList = SERVICES_CHANGED.tokenize(',')

                    if (servicesList.isEmpty()) {
                        echo "ℹ️ No changed services found. Skipping build."
                        return
                    }

                    for (service in servicesList) {
                        echo "🏗️ Building ${service}..."
                        dir(service) {
                            sh '../mvnw package -DskipTests -T 1C'
                        }
                    }
                }
            }
        }


        stage('Docker Build & Push') {
            when {
                expression { SERVICES_CHANGED?.trim() != "" }
            }
            // agent {
            //     label 'docker-node' // Agent with Docker installed
            // }
            steps {
                script {
                    def servicesList = SERVICES_CHANGED.tokenize(',')

                    if (servicesList.isEmpty()) {
                        error("❌ No changed services found. Verify 'Detect Changes' stage.")
                    }

                    // Login to DockerHub once before the loop
                    withCredentials([usernamePassword(
                        credentialsId: 'hzeroxium-dockerhub',
                        usernameVariable: 'DOCKERHUB_USER',
                        passwordVariable: 'DOCKERHUB_PASSWORD'
                    )]) {
                        sh "docker login -u \${DOCKERHUB_USER} -p \${DOCKERHUB_PASSWORD}"
                    }

                    // Sequential Docker builds and pushes
                    for (service in servicesList) {
                        echo "🐳 Building & pushing Docker image for ${service}..."

                        def commitHash = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                        def imageTag = "hzeroxium/${service}:${commitHash}"

                        sh """
                        docker build \
                            --build-arg SERVICE_NAME=${service} \
                            -f Dockerfile \
                            -t ${imageTag} \
                            -t hzeroxium/${service}:latest \
                            .
                        docker push ${imageTag}
                        docker push hzeroxium/${service}:latest
                        docker rmi ${imageTag} || true
                        docker rmi hzeroxium/${service}:latest || true
                        """
                    }
                }
            }
        }

        // stage('Deploy to Kubernetes') {
        //     when {
        //         expression { SERVICES_CHANGED?.trim() != "" }
        //         beforeAgent true
        //     }
        //     agent {
        //         label 'k8s-node' // Agent with kubectl configured
        //     }
        //     environment {
        //         DEPLOY_ENV = "${params.ENVIRONMENT ?: 'dev'}" // Default to 'dev' if not specified
        //     }
        //     steps {
        //         script {
        //             def servicesList = SERVICES_CHANGED.tokenize(',')
        //             if (servicesList.isEmpty()) {
        //                 echo "ℹ️ No changed services found. Skipping deployment."
        //                 return
        //             }

        //             // Configure kubectl with credentials
        //             withKubeConfig([
        //                 credentialsId: 'k8s-credentials',
        //                 serverUrl: 'https://kubernetes.example.com',
        //                 namespace: "${DEPLOY_ENV}"
        //             ]) {
        //                 // Verify connection to cluster
        //                 sh "kubectl config current-context"
        //                 sh "kubectl get nodes -o wide"

        //                 // Deploy each service sequentially
        //                 for (service in servicesList) {
        //                     echo "🚀 Deploying ${service} to Kubernetes environment: ${DEPLOY_ENV}..."

        //                     def commitHash = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        //                     def imageTag = "hzeroxium/${service}:${commitHash}"

        //                     try {
        //                         // Generate deployment files with correct image tag
        //                         dir('k8s-templates') {
        //                             // Replace the image tag in the template
        //                             sh """
        //                             sed 's#__IMAGE_TAG__#${imageTag}#g' ${service}-template.yaml > ../k8s/${DEPLOY_ENV}/${service}.yaml
        //                             """

        //                             // Apply ConfigMaps first if they exist
        //                             sh """
        //                             if [ -f "../k8s/${DEPLOY_ENV}/${service}-configmap.yaml" ]; then
        //                                 kubectl apply -f ../k8s/${DEPLOY_ENV}/${service}-configmap.yaml
        //                             fi
        //                             """

        //                             // Apply main deployment
        //                             sh "kubectl apply -f ../k8s/${DEPLOY_ENV}/${service}.yaml"

        //                             // Wait for deployment to complete with timeout
        //                             sh "kubectl rollout status deployment/${service} --timeout=180s"
        //                         }

        //                         // Verify deployment health
        //                         sh """
        //                         # Check if pods are running
        //                         READY_PODS=\$(kubectl get pods -l app=${service} -o jsonpath='{.items[*].status.containerStatuses[0].ready}' | tr ' ' '\\n' | grep -c true)
        //                         TOTAL_PODS=\$(kubectl get pods -l app=${service} --no-headers | wc -l)

        //                         if [ "\$READY_PODS" -lt "\$TOTAL_PODS" ]; then
        //                             echo "❌ Not all pods are ready for ${service}!"
        //                             kubectl get pods -l app=${service}
        //                             exit 1
        //                         fi
        //                         """

        //                         echo "✅ Deployment successful for ${service}"
        //                     } catch (Exception e) {
        //                         echo "❌ Deployment failed for ${service}: ${e.message}"

        //                         // Optionally rollback on failure
        //                         if (params.AUTO_ROLLBACK) {
        //                             echo "🔄 Rolling back ${service} deployment..."
        //                             sh "kubectl rollout undo deployment/${service}"
        //                         }

        //                         // Fail the build or continue based on parameter
        //                         if (params.FAIL_FAST) {
        //                             error("Deployment failed for ${service}")
        //                         }
        //                     }
        //                 }

        //                 // Print summary
        //                 echo "📊 Deployment Summary (${DEPLOY_ENV}):"
        //                 sh "kubectl get deployments -l project=spring-petclinic"
        //             }
        //         }
        //     }
        //     post {
        //         success {
        //             echo "🎉 All deployments completed successfully in ${DEPLOY_ENV} environment!"
        //         }
        //     }
        // }
    }

    post {
        failure {
            script {
                   echo "❌ CI/CD Pipeline failed!"
            }
        }
        success {
            script {
                   echo "✅ CI/CD Pipeline succeeded!"
            }
        }
        always {
            echo "✅ Pipeline execution completed for services: ${SERVICES_CHANGED}"
        }
    }
}
