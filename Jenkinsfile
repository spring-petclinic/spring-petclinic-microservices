pipeline {
    agent {
        label 'docker-agent'
    }

    environment {
        COVERAGE_THRESHOLD = 70
    }

    stages {
        stage('Identify Service Changed') {
            steps {
                script {
                    def diffFiles = sh(script: "git diff --name-only origin/main", returnStdout: true).trim().split("\n")
                    changedService = diffFiles.find { it ==~ /^.*-service\/.*/ }?.split('/')[0]
                    if (changedService == null) {
                        currentBuild.result = 'SUCCESS'
                        echo "No service changed, skipping build"
                        skipRemainingStages = true
                    } else {
                        echo "Changed service detected: ${changedService}"
                    }
                }
            }
        }

        stage('Test') {
            when {
                expression { return !binding.hasVariable('skipRemainingStages') }
            }
            steps {
                dir("${changedService}") {
                    sh './mvnw test'
                    junit '**/target/surefire-reports/*.xml'
                    cobertura coberturaReportFile: '**/target/site/cobertura/coverage.xml'
                }
            }
        }

        stage('Build') {
            when {
                expression { return !binding.hasVariable('skipRemainingStages') }
            }
            steps {
                dir("${changedService}") {
                    sh './mvnw package -DskipTests'
                }
            }
        }

        stage('Check Coverage') {
            when {
                expression { return !binding.hasVariable('skipRemainingStages') }
            }
            steps {
                script {
                    def coverage = coberturaAutoUpdate()
                    echo "Coverage: ${coverage}%"
                    if (coverage.toInteger() < COVERAGE_THRESHOLD.toInteger()) {
                        error("Coverage too low: ${coverage}% < ${COVERAGE_THRESHOLD}%")
                    }
                }
            }
        }
    }

    post {
        always {
            echo "CI Finished"
        }
    }
}
