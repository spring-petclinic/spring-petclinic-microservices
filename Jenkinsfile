pipeline {
    agent any

    environment {
        SERVICES = "spring-petclinic-customers-service,spring-petclinic-vets-service,spring-petclinic-visits-service,spring-petclinic-genai-service"
        TEST_RESULTS = 'target/surefire-reports'
        COVERAGE_REPORT = 'target/site/jacoco'
    }

    triggers {
        pollSCM('* * * * *') // Kiểm tra thay đổi mỗi phút
    }

    stages {
        stage('Check Changes') {
            steps {
                script {
                    def servicesList = env.SERVICES.split(',')
                    // def changedFiles = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()

                    // def servicesToBuild = servicesList.findAll { service ->
                    //     changedFiles.split('\n').any { it.startsWith("${service}/") }
                    // }

                    // if (servicesToBuild.isEmpty()) {
                    //     echo "No changes in any services. Skipping build."
                    //     currentBuild.result = 'SUCCESS'
                    //     error("Build failed")
                    // }

                    env.SERVICES_TO_BUILD = env.SERVICES
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
                            junit "${service}/${TEST_RESULTS}/*.xml"
                            jacoco execPattern: "${service}/target/jacoco.exec", 
                                   classPattern: "${service}/target/classes", 
                                   sourcePattern: "${service}/src/main/java", 
                                   htmlReport: true
                        }
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    def servicesToBuild = env.SERVICES_TO_BUILD ? env.SERVICES_TO_BUILD.split(',') : []
                    for (service in servicesToBuild) {
                        dir(service) {
                            sh '../mvnw clean package -DskipTests'
                        }
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
            echo 'Build and test completed successfully for all changed services!'
        }
        failure {
            echo 'Build or test failed for some services!'
        }
    }
}
