pipeline {
    agent any
    
    tools {
        maven 'Maven 3' // Use Jenkins' built-in Maven
    }
    
    stages {
        stage('Clean Workspace') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def services = [
                        "spring-petclinic-customers-service",
                        "spring-petclinic-vets-service",
                        "spring-petclinic-visits-service",
                        "spring-petclinic-genai-service"
                    ]

                    // **Step 1: Make Sure We Have Full Git History**
                    sh 'git fetch --all --prune'  // Fetch all branches properly

                    // **Step 2: Get the Current Branch**
                    def currentBranch = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    echo "Current branch: ${currentBranch}"

                    // **Step 3: Find the Base Branch (Main or PR Target)**
                    def baseBranch = env.CHANGE_TARGET ?: 'main'  // If it's a PR, use target branch; else default to main
                    echo "Comparing against base branch: origin/${baseBranch}"

                    // **Step 4: Ensure Base Branch Exists Locally (Fixes the Fetch Error)**
                    sh "git checkout ${baseBranch} || git fetch origin ${baseBranch}:${baseBranch} || true"

                    // **Step 5: Find the Common Ancestor Commit (Ensures Correct Comparison)**
                    def baseCommit = sh(script: "git merge-base origin/${baseBranch} HEAD || git rev-list --max-parents=0 HEAD", returnStdout: true).trim()
                    echo "Base commit: ${baseCommit}"

                    // **Step 6: Detect Changed Files**
                    def changedFilesOutput = sh(script: "git diff --name-only ${baseCommit} HEAD || true", returnStdout: true).trim()

                    if (changedFilesOutput) {
                        def changedFiles = changedFilesOutput.split("\n").collect { it.trim() }
                        echo "Changed files: ${changedFiles.join(', ')}"

                        // **Step 7: Detect Changed Services**
                        def changedServices = []
                        for (service in services) {
                            if (changedFiles.any { it.startsWith(service) }) {
                                changedServices.add(service)
                            }
                        }

                        echo "Services with changes: ${changedServices.join(', ')}"
                        env.CHANGED_SERVICES = changedServices.join(', ')
                    } else {
                        echo "No changed files detected."
                        env.CHANGED_SERVICES = ""
                    }

                    env.CURRENT_BRANCH = currentBranch
                }
            }
        }


        stage('Test Services') {
            when {
                expression { env.CHANGED_SERVICES != '' }
            }
            steps {
                script {
                    def changedServices = env.CHANGED_SERVICES.split(',')
                    changedServices.each{ service -> 
                        echo "Testing service: ${service}"
                        dir("${service}") {
                            // Run tests and generate coverage reports
                            sh "mvn test surefire-report:report jacoco:report"
        
                            // Publish JUnit test results
                            junit '**/target/surefire-reports/*.xml'
        
                            // Record test coverage using the Coverage plugin
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: '**/target/site/jacoco/jacoco.xml']],
                                qualityGates: [
                                    [threshold: 70.0, metric: 'LINE', baseline: 'PROJECT', unstable: false]
                                ]
                            )

                            // Check if build is unstable and force failure
                            if (currentBuild.result == 'UNSTABLE') {
                                error "Test coverage is below 70%, failing the build!"
                            }
                        }
                    }
                }
            }
        }

        stage('Build Services') {
            when {
                expression { env.CHANGED_SERVICES != '' }
            }
            steps {
                script {
                    def changedServices = env.CHANGED_SERVICES.split(',')
                    changedServices.each{ service -> 
                        echo "Building service: ${service}"
                        dir("${service}") {
                            sh "mvn clean package -DskipTests"
                        }
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo "Pipeline execution completed!"
        }
    }
}
