pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changed Services') {
            steps {
                script {
                    def services = [
                        'spring-petclinic-customers-service',
                        'spring-petclinic-vets-service',
                        'spring-petclinic-visits-service'
                    ]

                    sh 'git fetch origin main'

                    def list_files = sh(
                        script: "git diff --name-only origin/main HEAD",
                        returnStdout: true
                    ).trim().split('\n')

                    echo "List files: ${list_files}"

                    def affectedServicesList = []

                    for (svc in services) {
                        if (list_files.any { it.startsWith("${svc}/") }) {
                            echo "service: ${svc}"
                            affectedServicesList.add(svc)
                            echo "List services: ${affectedServicesList}"
                        }
                    }

                    if (affectedServicesList) {
                        echo "String services: ${affectedServicesList.join(' ')}"
                        env.SERVICES_TO_BUILD = affectedServicesList.join(' ')
                        echo "Affected services: ${env.SERVICES_TO_BUILD}"
                    } else {
                        echo "No relevant service changes detected."
                    }
                }
            }
        }

        stage('Test Affected Services') {
            when {
                expression { return env.SERVICES_TO_BUILD?.trim() }
            }
            steps {
                script {
                    def servicesToTest = env.SERVICES_TO_BUILD.split(' ')
                    for (svc in servicesToTest) {
                        dir("${svc}") {
                            echo "Running tests for ${svc}"
                            sh 'mvn clean test'
                            junit 'target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }

        stage('Build Affected Services') {
            when {
                expression { return env.AFFECTED_SERVICES?.trim() }
            }
            steps {
                script {
                    def servicesToBuild = env.AFFECTED_SERVICES.split(' ')
                    for (svc in servicesToBuild) {
                        dir("${svc}") {
                            echo "Building ${svc}"
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline complete"
        }
    }
}
