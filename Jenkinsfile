pipeline {
    agent { label 'agent-1 || agent-2' }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    def affectedServices = []
                    def changedFiles = sh(script: 'git diff --name-only HEAD~1', returnStdout: true).trim().split("\n")
                    echo "Changed files: ${changedFiles}"

                    for (file in changedFiles) {
                        if (file.startsWith("spring-petclinic-visits-service")) {
                            if (!affectedServices.contains("spring-petclinic-visits-service")) {
                                affectedServices << "spring-petclinic-visits-service"
                            }
                        }
                    }

                    if (affectedServices.isEmpty()) {
                        echo "No changes detected in spring-petclinic-visits-service. Skipping pipeline."
                        currentBuild.result = 'SUCCESS'
                        return
                    }

                    echo "Affected service: ${affectedServices}"
                    env.AFFECTED_SERVICES = affectedServices.join(',')
                }
            }
        }

        stage('Test and Coverage') {
            when {
                expression { return env.AFFECTED_SERVICES != null && env.AFFECTED_SERVICES != "" }
            }
            steps {
                script {
                    def affectedServices = env.AFFECTED_SERVICES.split(',')
                    for (service in affectedServices) {
                        echo "Testing service: ${service} on ${env.NODE_NAME}"
                        dir(service) {
                            timeout(time: 10, unit: 'MINUTES') {
                                retry(3) {
                                    sh 'mvn clean test jacoco:report jacoco:check'
                                }
                            }
                        }
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'

                    script {
                        def affectedServices = env.AFFECTED_SERVICES.split(',')
                        for (service in affectedServices) {
                            echo "Generating JaCoCo report for: ${service}"
                            jacoco(
                                execPattern: "${service}/target/jacoco.exec",
                                classPattern: "${service}/target/classes",
                                sourcePattern: "${service}/src/main/java",
                                exclusionPattern: "${service}/src/test/**",
                                minimumLineCoverage: '70',
                                changeBuildStatus: true,
                                failBuild: true
                            )
                        }
                    }
                }
            }
        }

        stage('Build') {
            when {
                expression { return env.AFFECTED_SERVICES != null && env.AFFECTED_SERVICES != "" }
            }
            steps {
                script {
                    def affectedServices = env.AFFECTED_SERVICES.split(',')
                    for (service in affectedServices) {
                        echo "Building service: ${service} on ${env.NODE_NAME}"
                        dir(service) {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }
    }
}
