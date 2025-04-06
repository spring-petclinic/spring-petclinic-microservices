pipeline {
    agent any

    environment {
        CHANGED_FILES = ''
    }

    stages {
        stage('Detect Changed Service') {
            steps {
                script {
                    // Lấy danh sách các file thay đổi từ git
                    CHANGED_FILES = sh(
                        script: "git diff --name-only origin/main",
                        returnStdout: true
                    ).trim().split('\n')
                    
                    echo "Changed Files: ${CHANGED_FILES}"

                    if (CHANGED_FILES.size() == 0 || CHANGED_FILES[0].trim() == "") {
                        error("No changed files detected! Nothing to test or build.")
                    }

                    // Gán danh sách service thay đổi
                    def changedServices = CHANGED_FILES.findAll { it.startsWith("spring-petclinic-") }
                        .collect { it.split("/")[0] }
                        .unique()
                    
                    if (changedServices.size() == 0) {
                        error("No microservice directories changed.")
                    }

                    env.CHANGED_SERVICES = changedServices.join(',')
                    echo "Changed Services: ${env.CHANGED_SERVICES}"
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(',')

                    for (service in services) {
                        dir(service) {
                            echo "Running tests for ${service}"
                            sh "./mvnw test"
                        }
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(',')

                    for (service in services) {
                        dir(service) {
                            echo "Building ${service}"
                            sh "./mvnw clean package -DskipTests"
                        }
                    }
                }
            }
        }

        stage('Check Coverage') {
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(',')

                    for (service in services) {
                        dir(service) {
                            echo "Checking code coverage for ${service}"
                            def reportFile = "target/site/jacoco/jacoco.xml"
                            if (!fileExists(reportFile)) {
                                error "Coverage report not found for ${service}"
                            }

                            def coverage = sh(
                                script: "grep '<counter type=\"INSTRUCTION\"' ${reportFile} | sed -E 's/.*covered=\"([0-9]+)\".*missed=\"([0-9]+)\".*/\\1 \\2/'",
                                returnStdout: true
                            ).trim().split(" ")
                            
                            def covered = coverage[0] as Integer
                            def missed = coverage[1] as Integer
                            def percent = (covered * 100) / (covered + missed)

                            echo "${service} coverage: ${percent}%"
                            if (percent < 70) {
                                error "${service} has insufficient coverage (${percent}%)"
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo '📦 CI Process Completed'
        }
        failure {
            echo '❌ CI Failed'
        }
        success {
            echo '✅ CI Passed'
        }
    }
}
