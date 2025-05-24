pipeline {
    agent any
    
    environment {
        // Docker Hub credentials từ Jenkins credentials store
        DOCKER_HUB_USERNAME = credentials('docker-hub-username')
        DOCKER_HUB_TOKEN = credentials('docker-hub-token')
        
        // Services array
        SERVICES = 'spring-petclinic-admin-server,spring-petclinic-api-gateway,spring-petclinic-config-server,spring-petclinic-customers-service,spring-petclinic-discovery-server,spring-petclinic-genai-service,spring-petclinic-vets-service,spring-petclinic-visits-service'
    }
    
    parameters {
        choice(
            name: 'BUILD_MODE',
            choices: ['AUTO_DETECT', 'ALL_SERVICES', 'SINGLE_SERVICE'],
            description: 'Build mode: AUTO_DETECT chỉ build service có thay đổi'
        )
        choice(
            name: 'SERVICE_NAME',
            choices: [
                'spring-petclinic-admin-server',
                'spring-petclinic-api-gateway', 
                'spring-petclinic-config-server',
                'spring-petclinic-customers-service',
                'spring-petclinic-discovery-server',
                'spring-petclinic-genai-service',
                'spring-petclinic-vets-service',
                'spring-petclinic-visits-service'
            ],
            description: 'Service to build (chỉ dùng khi BUILD_MODE = SINGLE_SERVICE)'
        )
    }
    
    stages {
        stage('📋 Initialize') {
            steps {
                script {
                    echo "🚀 Starting CI Pipeline for PetClinic Microservices"
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "Build Mode: ${params.BUILD_MODE}"
                    
                    // Get commit info
                    env.COMMIT_ID = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    env.COMMIT_MESSAGE = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
                    
                    // Create image tags based on branch
                    if (env.BRANCH_NAME == 'main') {
                        env.PRIMARY_TAG = 'latest'
                        env.SECONDARY_TAG = env.COMMIT_ID
                    } else {
                        env.PRIMARY_TAG = "${env.BRANCH_NAME}-${env.COMMIT_ID}"
                        env.SECONDARY_TAG = env.BRANCH_NAME
                    }
                    
                    echo "=== Build Information ==="
                    echo "📍 Branch: ${env.BRANCH_NAME}"
                    echo "🔖 Commit ID: ${env.COMMIT_ID}"
                    echo "🏷️ Primary Tag: ${env.PRIMARY_TAG}"
                    echo "🏷️ Secondary Tag: ${env.SECONDARY_TAG}"
                    echo "💬 Commit: ${env.COMMIT_MESSAGE}"
                }
            }
        }
        
        stage('🔍 Detect Changes') {
            when {
                expression { params.BUILD_MODE == 'AUTO_DETECT' }
            }
            steps {
                script {
                    def services = env.SERVICES.split(',')
                    def changedServices = []
                    
                    try {
                        // Get changed files since last commit
                        def changedFiles = sh(
                            script: '''
                                if git rev-parse HEAD~1 >/dev/null 2>&1; then
                                    git diff --name-only HEAD~1 HEAD
                                else
                                    git diff --name-only --cached
                                fi
                            ''',
                            returnStdout: true
                        ).trim().split('\n')
                        
                        echo "📝 Changed files:"
                        changedFiles.each { file ->
                            echo "   - ${file}"
                        }
                        
                        // Check which services have changes
                        services.each { service ->
                            def hasChanges = changedFiles.any { file -> 
                                file.startsWith("${service}/") || file == "${service}"
                            }
                            if (hasChanges) {
                                changedServices.add(service)
                            }
                        }
                        
                    } catch (Exception e) {
                        echo "⚠️ Could not detect changes (might be first commit): ${e.getMessage()}"
                        echo "Building all services as fallback"
                        changedServices = services
                    }
                    
                    if (changedServices.isEmpty()) {
                        echo "📦 No service-specific changes detected"
                        echo "🔨 Building all services (common files might have changed)"
                        env.SERVICES_TO_BUILD = env.SERVICES
                    } else {
                        echo "🎯 Services with changes: ${changedServices.join(', ')}"
                        env.SERVICES_TO_BUILD = changedServices.join(',')
                    }
                }
            }
        }
        
        stage('📝 Set Build List') {
            steps {
                script {
                    def allServices = env.SERVICES.split(',')
                    
                    switch(params.BUILD_MODE) {
                        case 'ALL_SERVICES':
                            env.SERVICES_TO_BUILD = env.SERVICES
                            echo "🔨 Mode: Building ALL services"
                            break
                        case 'SINGLE_SERVICE':
                            env.SERVICES_TO_BUILD = params.SERVICE_NAME
                            echo "🎯 Mode: Building single service - ${params.SERVICE_NAME}"
                            break
                        case 'AUTO_DETECT':
                            echo "🔍 Mode: Auto-detected services"
                            break
                    }
                    
                    def buildList = env.SERVICES_TO_BUILD.split(',')
                    echo "📋 Final build list (${buildList.size()} services):"
                    buildList.each { service ->
                        echo "   ✅ ${service.trim()}"
                    }
                }
            }
        }
        
        stage('🐳 Build & Push Images') {
            steps {
                script {
                    def servicesToBuild = env.SERVICES_TO_BUILD.split(',')
                    def buildResults = [:]
                    def successCount = 0
                    
                    // Login to Docker Hub
                    sh '''
                        echo "🔐 Logging into Docker Hub..."
                        echo $DOCKER_HUB_TOKEN | docker login -u $DOCKER_HUB_USERNAME --password-stdin
                    '''
                    
                    // Build each service
                    servicesToBuild.each { service ->
                        service = service.trim()
                        
                        echo ""
                        echo "=" * 60
                        echo "🔨 Building: ${service}"
                        echo "=" * 60
                        
                        try {
                            // Check if service directory exists
                            if (!fileExists(service)) {
                                throw new Exception("Service directory '${service}' not found")
                            }
                            
                            dir(service) {
                                // Check for existing JAR
                                def jarExists = sh(
                                    script: 'ls target/*.jar 2>/dev/null | wc -l',
                                    returnStdout: true
                                ).trim() != '0'
                                
                                if (!jarExists) {
                                    echo "📦 Building Maven project..."
                                    sh '''
                                        mvn clean package -DskipTests -q
                                        if [ ! -f target/*.jar ]; then
                                            echo "❌ Maven build failed - no JAR produced"
                                            exit 1
                                        fi
                                    '''
                                }
                                
                                // Get JAR info
                                def jarFile = sh(
                                    script: 'ls target/*.jar | head -1',
                                    returnStdout: true
                                ).trim()
                                
                                def jarName = sh(
                                    script: 'basename $(ls target/*.jar | head -1)',
                                    returnStdout: true
                                ).trim()
                                
                                echo "✅ Found JAR: ${jarName}"
                                
                                // Build Docker images
                                def primaryImage = "${env.DOCKER_HUB_USERNAME}/${service}:${env.PRIMARY_TAG}"
                                def secondaryImage = "${env.DOCKER_HUB_USERNAME}/${service}:${env.SECONDARY_TAG}"
                                
                                echo "🐳 Building Docker image..."
                                sh "docker build -t ${primaryImage} ."
                                
                                // Tag with secondary tag if different
                                if (env.PRIMARY_TAG != env.SECONDARY_TAG) {
                                    sh "docker tag ${primaryImage} ${secondaryImage}"
                                }
                                
                                // Get image size
                                def imageSize = sh(
                                    script: "docker images ${primaryImage} --format '{{.Size}}'",
                                    returnStdout: true
                                ).trim()
                                
                                echo "✅ Image built successfully - Size: ${imageSize}"
                                
                                // Push images
                                echo "📤 Pushing to Docker Hub..."
                                sh "docker push ${primaryImage}"
                                
                                if (env.PRIMARY_TAG != env.SECONDARY_TAG) {
                                    sh "docker push ${secondaryImage}"
                                }
                                
                                buildResults[service] = 'SUCCESS'
                                successCount++
                                
                                echo "✅ Successfully pushed ${service}"
                                echo "   🏷️ Tags: ${env.PRIMARY_TAG}, ${env.SECONDARY_TAG}"
                            }
                            
                        } catch (Exception e) {
                            buildResults[service] = "FAILED: ${e.getMessage()}"
                            echo "❌ Build failed for ${service}: ${e.getMessage()}"
                        }
                    }
                    
                    // Store results for summary
                    env.BUILD_RESULTS = buildResults.collect { k, v -> "${k}:${v}" }.join('|')
                    env.SUCCESS_COUNT = successCount.toString()
                    env.TOTAL_COUNT = servicesToBuild.size().toString()
                }
            }
        }
        
        stage('📊 Build Summary') {
            steps {
                script {
                    echo ""
                    echo "=" * 60
                    echo "🎉 BUILD SUMMARY"
                    echo "=" * 60
                    echo "📍 Branch: ${env.BRANCH_NAME}"
                    echo "🔖 Commit: ${env.COMMIT_ID}"
                    echo "🏷️ Image Tags: ${env.PRIMARY_TAG}, ${env.SECONDARY_TAG}"
                    echo "📊 Success Rate: ${env.SUCCESS_COUNT}/${env.TOTAL_COUNT} services"
                    echo ""
                    
                    def results = env.BUILD_RESULTS.split('\\|')
                    def successfulServices = []
                    def failedServices = []
                    
                    results.each { result ->
                        def parts = result.split(':')
                        def service = parts[0]
                        def status = parts[1]
                        
                        if (status == 'SUCCESS') {
                            successfulServices.add(service)
                            echo "✅ ${service}"
                            echo "   📦 https://hub.docker.com/r/${env.DOCKER_HUB_USERNAME}/${service}"
                        } else {
                            failedServices.add(service)
                            echo "❌ ${service} - ${status}"
                        }
                    }
                    
                    if (successfulServices.size() > 0) {
                        echo ""
                        echo "🎯 Successfully built images:"
                        successfulServices.each { service ->
                            echo "   docker pull ${env.DOCKER_HUB_USERNAME}/${service}:${env.PRIMARY_TAG}"
                        }
                    }
                    
                    if (failedServices.size() > 0) {
                        echo ""
                        echo "⚠️ Failed builds: ${failedServices.join(', ')}"
                    }
                    
                    // Set build result
                    if (env.SUCCESS_COUNT == '0') {
                        currentBuild.result = 'FAILURE'
                        error("All builds failed!")
                    } else if (failedServices.size() > 0) {
                        currentBuild.result = 'UNSTABLE'
                        echo "⚠️ Some builds failed, marking as UNSTABLE"
                    }
                }
            }
        }
    }
    
    post {
        always {
            script {
                // Cleanup
                echo "🧹 Cleaning up..."
                sh '''
                    docker logout || true
                    docker system prune -f || true
                '''
            }
        }
        
        success {
            echo "🎉 CI Pipeline completed successfully!"
            script {
                if (env.BRANCH_NAME == 'main') {
                    echo "🚀 Main branch build successful - ready for deployment"
                }
            }
        }
        
        failure {
            echo "❌ CI Pipeline failed!"
        }
        
        unstable {
            echo "⚠️ CI Pipeline completed with some failures"
        }
        
        cleanup {
            // Clean workspace if needed
            cleanWs(deleteDirs: true, disableDeferredWipeout: true)
        }
    }
}