def SERVICES_CHANGED = ""
def DEPLOY_ENV = "${params.ENVIRONMENT ?: 'dev'}" // Default dev if not specified
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
            steps {
                script {
                    def servicesList = SERVICES_CHANGED.tokenize(',')

                    if (servicesList.isEmpty()) {
                        error("❌ No changed services found. Verify 'Detect Changes' stage.")
                    }

                    def servicePorts = [
                        "spring-petclinic-admin-server": 9090,
                        "spring-petclinic-api-gateway": 8080,
                        "spring-petclinic-config-server": 8888,
                        "spring-petclinic-customers-service": 8081,
                        "spring-petclinic-discovery-server": 8761,
                        "spring-petclinic-genai-service": 8084,
                        "spring-petclinic-vets-service": 8083,
                        "spring-petclinic-visits-service": 8082
                    ]

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

                        // Extract short service name from the full name
                        def shortServiceName = service.replaceFirst("spring-petclinic-", "")
                        
                        // Get the appropriate port for this service
                        def servicePort = servicePorts.get(service, 8080)
                        
                        def commitHash = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                        def imageTag = "hzeroxium/${service}:${commitHash}"

                        sh """
                        docker build \\
                            --build-arg SERVICE_NAME=${shortServiceName} \\
                            --build-arg EXPOSED_PORT=${servicePort} \\
                            -f Dockerfile \\
                            -t ${imageTag} \\
                            -t hzeroxium/${service}:latest \\
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

        stage('Deploy to Kubernetes (Helm)') {
            when {
                expression { SERVICES_CHANGED?.trim() != "" }
                beforeAgent true
            }
            agent {
                label 'k8s-node'
            }
            steps {
                script {
                    def servicesList = SERVICES_CHANGED.tokenize(',')
                    if (servicesList.isEmpty()) {
                        echo "ℹ️ No changed services found. Skipping deployment."
                        return
                    }

                    def servicePorts = [
                        "spring-petclinic-admin-server": 9090,
                        "spring-petclinic-api-gateway": 8080,
                        "spring-petclinic-config-server": 8888,
                        "spring-petclinic-customers-service": 8081,
                        "spring-petclinic-discovery-server": 8761,
                        "spring-petclinic-genai-service": 8084,
                        "spring-petclinic-vets-service": 8083,
                        "spring-petclinic-visits-service": 8082
                    ]

                    withKubeConfig([
                        credentialsId: 'k8s-credentials',
                        serverUrl: 'https://kubernetes.example.com',
                        namespace: "${DEPLOY_ENV}"
                    ]) {
                        sh "kubectl get nodes -o wide"

                        for (service in servicesList) {
                            echo "🚀 Deploying ${service}..."

                            def commitHash = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                            def imageTag = "hzeroxium/${service}:${commitHash}"
                            def releaseName = "${service}-${DEPLOY_ENV}"
                            def servicePort = servicePorts.get(service, 8080)

                            try {
                                sh """
                                helm upgrade --install ${releaseName} chart/ \
                                --namespace "${DEPLOY_ENV}" \
                                --create-namespace \
                                --wait --timeout 180s \
                                --set image.repository="hzeroxium/${service}" \
                                --set image.tag="${commitHash}" \
                                --set service.port=${servicePort} \
                                --set livenessProbe.port=${servicePort} \
                                --set readinessProbe.port=${servicePort}
                                """
                                echo "✅ Helm deployment success for ${service}"
                            } catch (err) {
                                echo "❌ Helm deployment failed for ${service}: ${err.message}"
                                if (params.AUTO_ROLLBACK) {
                                    echo "🔄 Rolling back ${releaseName}..."
                                    sh "helm rollback ${releaseName}"
                                }
                                if (params.FAIL_FAST) {
                                    error("Deployment failed for ${service}")
                                }
                            }
                        }

                        echo "📊 Deployment Summary (${DEPLOY_ENV}):"
                        sh "helm list --namespace ${DEPLOY_ENV}"
                    }
                }
            }
            post {
                success {
                    echo "🎉 All deployments (Helm) completed successfully in ${DEPLOY_ENV} environment!"
                }
            }
        }
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
