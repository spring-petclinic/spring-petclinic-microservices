pipeline {
    agent any

    environment {
        SERVICES = "spring-petclinic-customers-service,spring-petclinic-vets-service,spring-petclinic-visits-service,spring-petclinic-genai-service"
        TEST_RESULTS = 'target/surefire-reports'
    }

    stages {
        stage('Checkout Code') {
            steps {
                script {
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "*/${env.BRANCH_NAME}"]],
                        userRemoteConfigs: [[
                            url: 'https://github.com/BinhTran666/spring-petclinic-microservices-CI-CD-Implementation.git',
                            credentialsId: 'githib-user-password-PAS'
                        ]],
                        extensions: [[$class: 'CloneOption', noTags: false, depth: 0]]
                    ])
                }
            }
        }

        stage('Check Changes') {
            steps {
                script {
                    sh 'git fetch origin main' // Fetch main trước khi kiểm tra
                    
                    def servicesList = env.SERVICES.split(',')
                    def targetBranch = env.CHANGE_TARGET ?: "main"
                    def commonAncestor = sh(returnStdout: true, script: "git merge-base HEAD origin/${targetBranch}").trim()
                    def changedFiles = sh(returnStdout: true, script: "git diff --name-only ${commonAncestor}").trim()

                    def servicesToBuild = servicesList.findAll { service ->
                        changedFiles.split('\n').any { it.startsWith("${service}/") }
                    }

                    if (servicesToBuild.isEmpty()) {
                        echo "No changes in any services. Skipping build."
                        currentBuild.result = 'SUCCESS'
                        return
                    }

                    env.SERVICES_TO_BUILD = servicesToBuild.join(',')
                    echo "Services to build: ${env.SERVICES_TO_BUILD}"
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    publishChecks name: 'Test', status: 'IN_PROGRESS'
                    def servicesToBuild = env.SERVICES_TO_BUILD ? env.SERVICES_TO_BUILD.split(',') : []
                    for (service in servicesToBuild) {
                        dir(service) {
                            sh '../mvnw clean verify -P coverage'
                        }
                    }
                }
            }
            post {
                always {
                    script {
                        def servicesToBuild = env.SERVICES_TO_BUILD ? env.SERVICES_TO_BUILD.split(',') : []
                        for (service in servicesToBuild) {
                            def junitReportPath = "${service}/target/surefire-reports/*.xml"
                            def junitReportExists = sh(script: "ls ${junitReportPath} 2>/dev/null || echo 'notfound'", returnStdout: true).trim()
                            if (junitReportExists == 'notfound') {
                                echo "No test report found for ${service}"
                            } else {
                                junit junitReportPath
                            }
                            recordCoverage(tools: [[parser: 'JACOCO', pattern: "${service}/target/jacoco.exec"]])
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
            publishChecks name: 'Pipeline', status: 'COMPLETED', conclusion: 'SUCCESS'
            echo 'Build and test completed successfully for changed services!'
        }
        failure {
            publishChecks name: 'Pipeline', status: 'COMPLETED', conclusion: 'FAILURE'
            echo 'Build or test failed for some services!'
        }
    }
}
