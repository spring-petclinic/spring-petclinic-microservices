pipeline {
    agent any

    environment {
        SERVICES = "spring-petclinic-customers-service,spring-petclinic-vets-service,spring-petclinic-visits-service,spring-petclinic-genai-service"
        TEST_RESULTS = 'target/surefire-reports'
        COVERAGE_REPORT = 'target/site/jacoco'
    }

    triggers {
           GenericTrigger(
                genericVariables: [
                    [key: 'GITHUB_EVENT', value: '$.github_event'] // Hoặc '$.event', tùy thuộc vào payload
                ],
                causeString: 'Triggered by GitHub event: $GITHUB_EVENT',
                regexpFilterText: '$GITHUB_EVENT',
                regexpFilterExpression: 'push|pull_request',
                printContributedVariables: true, // Thêm dòng này để debug
                printPostContent: true          // Thêm dòng này để debug
            )
    }

    stages {
        stage('Check Changes') {
            steps {
                script {
                    def servicesList = env.SERVICES.split(',')
                    def changedFiles = sh(script: "git diff --name-only origin/main", returnStdout: true).trim()

                    def servicesToBuild = servicesList.findAll { service ->
                        changedFiles.split('\n').any { it.startsWith("${service}/") }
                    }

                    if (servicesToBuild.isEmpty()) {
                        echo "No changes in any services. Skipping build."
                        currentBuild.result = 'SUCCESS'
                        error("No services changed, skipping build.")
                    }

                    env.SERVICES_TO_BUILD = servicesToBuild.join(',')
                    echo "Services to build: ${env.SERVICES_TO_BUILD}"
                }
            }
        }

        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                script {
                    publishChecks name: 'Test', status: 'IN_PROGRESS'

                    def servicesToBuild = env.SERVICES_TO_BUILD ? env.SERVICES_TO_BUILD.split(',') : []
                    for (service in servicesToBuild) {
                        dir(service) {
                            sh '../mvnw test'
                        }
                    }
                }
            }
            post {
                always {
                    script {
                        def servicesToBuild = env.SERVICES_TO_BUILD ? env.SERVICES_TO_BUILD.split(',') : []
                        for (service in servicesToBuild) {
                            def reportPath = "${service}/target/surefire-reports/*.xml"
                            def reportExists = sh(script: "ls ${reportPath} 2>/dev/null || echo 'notfound'", returnStdout: true).trim()
                            if (reportExists == 'notfound') {
                                echo "No test report found for ${service}"
                            } else {
                                junit reportPath
                            }

                            jacoco execPattern: "${service}/target/jacoco.exec", 
                                   classPattern: "${service}/target/classes", 
                                   sourcePattern: "${service}/src/main/java", 
                                   htmlReport: true
                        }
                    }
                }
                success {
                    script {
                        publishChecks name: 'Test', status: 'COMPLETED', conclusion: 'SUCCESS'
                    }
                }
                failure {
                    script {
                        publishChecks name: 'Test', status: 'COMPLETED', conclusion: 'FAILURE'
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    publishChecks name: 'Build', status: 'IN_PROGRESS'

                    def servicesToBuild = env.SERVICES_TO_BUILD ? env.SERVICES_TO_BUILD.split(',') : []
                    for (service in servicesToBuild) {
                        dir(service) {
                            sh '../mvnw clean package -DskipTests'
                        }
                    }
                }
            }
            post {
                success {
                    script {
                        publishChecks name: 'Build', status: 'COMPLETED', conclusion: 'SUCCESS'
                    }
                }
                failure {
                    script {
                        publishChecks name: 'Build', status: 'COMPLETED', conclusion: 'FAILURE'
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                def servicesToBuild = env.SERVICES_TO_BUILD ? env.SERVICES_TO_BUILD.split(',') : []
                for (service in servicesToBuild) {
                    archiveArtifacts artifacts: "${service}/target/*.jar", fingerprint: true
                }
            }
        }
        success {
            script {
                publishChecks name: 'Pipeline', status: 'COMPLETED', conclusion: 'SUCCESS'
                echo 'Build and test completed successfully for changed services!'
            }
        }
        failure {
            script {
                publishChecks name: 'Pipeline', status: 'COMPLETED', conclusion: 'FAILURE'
                echo 'Build or test failed for some services!'
            }
        }
    }
}
