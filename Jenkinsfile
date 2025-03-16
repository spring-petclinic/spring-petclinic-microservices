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

                    // Fetch all branches, ensuring full history
                    sh 'git fetch --all --prune'

                    // Get the current branch name
                    def currentBranch = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    echo "Current branch: ${currentBranch}"

                    // Determine the base branch (use PR target or default to main)
                    def baseBranch = env.CHANGE_TARGET ?: 'main'
                    echo "Comparing against base branch: origin/${baseBranch}"

                    // Ensure the base branch exists locally
                    sh "git checkout ${baseBranch} || git checkout -b ${baseBranch} origin/${baseBranch} || true"

                    // **Force Fetch to Ensure Base Branch is Up-to-Date**
                    sh "git fetch origin ${baseBranch}:${baseBranch}"

                    // Get the latest common ancestor commit (fixing the issue)
                    def baseCommit = sh(script: "git merge-base ${baseBranch} ${currentBranch} || echo \$(git rev-list --max-parents=0 HEAD)", returnStdout: true).trim()
                    echo "Base commit: ${baseCommit}"

                    // **Get list of changed files in the branch**
                    def changedFilesOutput = sh(script: "git diff --name-only ${baseCommit} ${currentBranch} || true", returnStdout: true).trim()

                    if (changedFilesOutput) {
                        def changedFiles = changedFilesOutput.split("\n").collect { it.trim() }
                        echo "Changed files: ${changedFiles.join(', ')}"

                        // **Identify which services were modified**
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
