pipeline {
    agent any

    environment {
        GIT_DIFF = ''
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    checkout scm
                    GIT_DIFF = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim()
                    echo "Changed files: ${GIT_DIFF}"
                }
            }
        }

        stage('Determine Changes') {
            steps {
                script {
                    def services = ['customers-service', 'vets-service', 'visit-service']
                    env.BUILD_SERVICES = services.findAll { service -> GIT_DIFF.contains(service) }.join(',')
                    if (env.BUILD_SERVICES == '') {
                        echo "No relevant changes detected. Skipping build."
                        currentBuild.result = 'SUCCESS'
                        error('No changes detected for relevant services.')
                    }
                    echo "Services to build: ${env.BUILD_SERVICES}"
                }
            }
        }

        stage('Run Tests') {
            when {
                expression { env.BUILD_SERVICES != '' }
            }
            steps {
                script {
                    def servicesToBuild = env.BUILD_SERVICES.split(',')
                    servicesToBuild.each { service ->
                        echo "Running tests for ${service}"
                        sh "cd ${service} && ../mvnw test"
                        junit "**/${service}/target/surefire-reports/*.xml"
                        jacoco execPattern: "**/${service}/target/jacoco.exec"
                    }
                }
            }
        }

        stage('Build Services') {
            when {
                expression { env.BUILD_SERVICES != '' }
            }
            steps {
                script {
                    def servicesToBuild = env.BUILD_SERVICES.split(',')
                    servicesToBuild.each { service ->
                        echo "Building ${service}"
                        sh "cd ${service} && ../mvnw package -DskipTests"
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
        }
    }
}
