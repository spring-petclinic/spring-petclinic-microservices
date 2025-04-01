pipeline {
    agent {
        label 'development-server'
    }

    environment {
        CHANGED_SERVICES = getChangedServices()
    }

    stages {
        stage('Checkout source') {
            steps {
                checkout scm

                script {
                    try {
                        
                        def gitTag = sh(script: "git describe --tags --always", returnStdout: true).trim()
                        env.GIT_TAG = gitTag

                        echo "Git Tag or Commit: ${env.GIT_TAG}"
                    } catch (Exception e) {
                        echo "Failed to retrieve Git tag: ${e.getMessage()}"
                        env.GIT_TAG = "1.0"
                    }
                }
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    env.CHANGED_SERVICES = getChangedServices()
                    if (env.CHANGED_SERVICES == "NONE") {
                        echo "No relevant changes detected. Skipping build."
                        currentBuild.result = 'ABORTED'
                        error("No relevant changes detected")
                    } else {
                        echo "Detected changes in services: ${env.CHANGED_SERVICES}"
                    }
                }
            }
        }

        stage('Run Unit Test') {
            when {
                expression { env.CHANGED_SERVICES && env.CHANGED_SERVICES.trim() }
            }
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(',')
                    def coverageResults = []
                    def servicesToBuild = []

                    for (service in services) {
                        def testCommand = "mvn test -pl ${service} jacoco:report"
                        echo "Running tests for: ${service}"
                        sh "${testCommand}"

                        def reportPath = "${service}/target/site/jacoco/index.html"
                        archiveArtifacts artifacts: reportPath, fingerprint: true

                        def coverage = sh(
                                script: """
                                grep -oP '(?<=<td class="ctr2">)\\\\d+%' ${reportPath} | head -1 | sed 's/%//'
                                """,
                                returnStdout: true
                            ).trim().toInteger()

                        coverageResults << "${service}:${coverage}%"
                        echo "Code Coverage for ${service}: ${coverage}%"

                        if (coverage > 70) {
                            servicesToBuild << service
                        }
                    }

                    env.CODE_COVERAGES = coverageResults.join(', ')
                    env.SERVICES_TO_BUILD = servicesToBuild.join(',')
                    echo "Final Code Coverages: ${env.CODE_COVERAGES}"
                    echo "Services to Build: ${env.SERVICES_TO_BUILD}"
                }
            }
        }

        stage('Build Services') {
            when {
                expression { env.SERVICES_TO_BUILD && env.SERVICES_TO_BUILD.trim() }
            }
            steps {
                script {
                    def services = env.SERVICES_TO_BUILD.split(',')

                    for (service in services) {
                        echo "Building: ${service}"
                        sh "mvn clean package -pl ${service} -DfinalName=app -DskipTests"
                    }
                }
            }
        }

        stage('Build Docker Image') {
            when {
                expression { env.SERVICES_TO_BUILD && env.SERVICES_TO_BUILD.trim() }
            }
            steps {
                script {
                    def services = env.SERVICES_TO_BUILD.split(',')

                    for (service in services) {
                        echo "Building Docker Image for: ${service}"
                        sh "docker build --build-arg ARTIFACT_NAME=${service}/target/app -t ${service}-image -f docker/Dockerfile ."
                    }
                }
            }
        }
    }
}

def getChangedServices() {
    def branch = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
    
    if (branch != "main") {
        echo "Not on main branch. Skipping change detection."
        return "NONE"
    }


    def changedFiles = sh(script: 'git diff --name-only origin/main~1 origin/main', returnStdout: true).trim().split("\n")
    def services = [
        'spring-petclinic-admin-server', 
        'spring-petclinic-config-server',
        'spring-petclinic-customers-service', 
        'spring-petclinic-discovery-server',
        'spring-petclinic-genai-service',
        'spring-petclinic-vets-service',
        'spring-petclinic-visits-service'
    ]

    def affectedServices = services.findAll { service ->
        changedFiles.any { file -> file.startsWith(service + "/") }
    }

    if (affectedServices.isEmpty()) {
        return "NONE"
    }
    echo "Changed services: ${affectedServices.join(', ')}"
    return affectedServices.join(',')
}