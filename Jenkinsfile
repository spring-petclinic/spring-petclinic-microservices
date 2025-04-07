pipeline {
    agent any

    environment {
        MIN_COVERAGE = 70
    }

    tools {
        maven '3.9.9'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changed Services') {
            steps {
                script {
                    echo "Current user: $USER"
                    def services = [
                        'spring-petclinic-customers-service',
                        'spring-petclinic-vets-service',
                        'spring-petclinic-visits-service'
                    ]

                    sh 'git fetch origin refs/heads/main:refs/remotes/origin/main'

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
                            sh 'mvn test'
                            junit 'target/surefire-reports/*.xml'

                            jacoco classPattern: "target/classes", 
                                   execPattern: "target/coverage-reports/jacoco.exec",
                                   runAlways: true, 
                                   sourcePattern: "src/main/java"

                            // Get Code Coverage
                            def codeCoverages = []
                            def coverageReport = readFile(file: "target/site/jacoco/index.html")
                            def matcher = coverageReport =~ /<tfoot>(.*?)<\/tfoot>/
                            if (matcher.find()) {
                                def coverageMatch = matcher[0]
                                def instructionMatcher = coverageMatch =~ /<td class="ctr2">(.*?)%<\/td>/
                                if (instructionMatcher.find()) {
                                    def coverage = instructionMatcher[0][1]
                                    echo "Overall code coverage of ${svc}: ${coverage}%"

                                    // Kiểm tra coverage có đạt yêu cầu không
                                    if (coverage.toFloat() < env.MIN_COVERAGE.toInteger()) {
                                        error("Coverage for ${svc} is too low (${coverage}%), must be >= ${env.MIN_COVERAGE}%")
                                    } else {
                                        echo "Coverage OK (${coverage}%)"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Build Affected Services') {
            when {
                expression { return env.SERVICES_TO_BUILD?.trim() }
            }
            steps {
                script {
                    def servicesToBuild = env.SERVICES_TO_BUILD.split(' ')
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
