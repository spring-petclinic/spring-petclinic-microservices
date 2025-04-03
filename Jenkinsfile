pipeline {
    agent { label 'built-in' }

    environment {
        REPO_URL = 'https://github.com/htloc0610/spring-petclinic-microservices'
        WORKSPACE_DIR = "repo"
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    echo "Cloning repository ${REPO_URL}"
                    sh "rm -rf ${WORKSPACE_DIR}"
                    sh "mkdir -p ${WORKSPACE_DIR}"

                    dir(WORKSPACE_DIR) {
                        if (env.CHANGE_ID) {
                            // Pull Request
                            echo "Checking out PR #${env.CHANGE_ID} (target: ${env.CHANGE_TARGET})"
                            sh "git init"
                            sh "git remote add origin ${REPO_URL}"
                            sh "git fetch origin refs/pull/${env.CHANGE_ID}/merge:pr-${env.CHANGE_ID}"
                            sh "git fetch origin ${env.CHANGE_TARGET}:refs/remotes/origin/${env.CHANGE_TARGET}"
                            sh "git checkout pr-${env.CHANGE_ID}"
                        } else {
                            // Branch
                            echo "Checking out branch ${env.BRANCH_NAME}"
                            sh "git clone -b ${env.BRANCH_NAME} ${REPO_URL} ."
                        }
                    }
                }
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    dir(WORKSPACE_DIR) {
                        def isPR = env.CHANGE_ID != null
                        def changes = ''

                        if (isPR) {
                            changes = sh(script: "git diff --name-only origin/${env.CHANGE_TARGET}", returnStdout: true).trim()
                        } else {
                            changes = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()
                        }

                        echo "Files changed:\n${changes}"

                        def services = [
                            'spring-petclinic-admin-server',
                            'spring-petclinic-api-gateway',
                            'spring-petclinic-config-server',
                            'spring-petclinic-customers-service',
                            'spring-petclinic-discovery-server',
                            'spring-petclinic-genai-service',
                            'spring-petclinic-vets-service',
                            'spring-petclinic-visits-service'
                        ]

                        def affectedServices = changes.tokenize("\n")
                            .collect { it =~ /^([^\/]+)\// ? (it =~ /^([^\/]+)\//)[0][1] : null }
                            .unique()
                            .findAll { it in services }

                        if (affectedServices.isEmpty()) {
                            echo "No relevant changes, skipping tests and build"
                            env.SKIP_PIPELINE = "true"
                        } else {
                            env.AFFECTED_SERVICES = affectedServices.join(",")
                            echo "Services to build: ${env.AFFECTED_SERVICES}"
                        }
                    }
                }
            }
        }

        stage('Test') {
            when {
                allOf {
                    expression { env.AFFECTED_SERVICES }
                    expression { env.SKIP_PIPELINE != "true" }
                }
            }
            steps {
                script {
                    env.AFFECTED_SERVICES.split(",").each { service ->
                        echo "Running tests for ${service}..."
                        dir("${WORKSPACE_DIR}/${service}") {
                            try {
                                sh 'mvn test'
                            } catch (Exception e) {
                                error "Tests failed for ${service}"
                            }
                        }
                    }
                }
            }
            post {
                always {
                    junit "**/${WORKSPACE_DIR}/**/target/surefire-reports/*.xml"
                }
            }
        }

        stage('Code Coverage') {
            when {
                allOf {
                    expression { env.AFFECTED_SERVICES }
                    expression { env.SKIP_PIPELINE != "true" }
                }
            }
            steps {
                script {
                    env.AFFECTED_SERVICES.split(",").each { service ->
                        echo "Checking test coverage for ${service}..."
                        dir("${WORKSPACE_DIR}/${service}") {
                            try {
                                sh 'mvn jacoco:report'
                                sh 'cat target/site/jacoco/jacoco.csv'

                                def coverageData = sh(script: '''
                                    tail -n +2 target/site/jacoco/jacoco.csv | awk -F',' '
                                    { total+=$4+$5; covered+=$5 }
                                    END { if (total>0) { coverage=(covered/total)*100; if (coverage>100) coverage=100; print coverage } else print 0 }'
                                ''', returnStdout: true).trim()

                                echo "Code Coverage for ${service}: ${coverageData}%"

                                // Check coverage > 70 pull request for main
                                if (env.CHANGE_ID && env.CHANGE_TARGET == 'main') {
                                    def coverageValue = coverageData.toFloat()
                                    if (coverageValue < 70) {
                                        error "Code coverage for ${service} is ${coverageValue}%, which is below the required 70% for PRs to main. Failing the pipeline."
                                    }
                                }
                            } catch (Exception e) {
                                error "Code coverage report generation failed for ${service}"
                            }
                        }
                    }
                }
            }
            post {
                always {
                    script {
                        env.AFFECTED_SERVICES.split(",").each { service ->
                            publishHTML([
                                target: [
                                    reportDir: "${WORKSPACE_DIR}/${service}/target/site/jacoco",
                                    reportFiles: 'index.html',
                                    reportName: "Code Coverage - ${service}"
                                ]
                            ])
                        }
                    }
                }
            }
        }

        stage('Build') {
            when {
                allOf {
                    expression { env.AFFECTED_SERVICES }
                    expression { env.SKIP_PIPELINE != "true" }
                }
            }
            steps {
                script {
                    env.AFFECTED_SERVICES.split(",").each { service ->
                        echo "Building ${service}..."
                        dir("${WORKSPACE_DIR}/${service}") {
                            try {
                                sh 'mvn clean package'
                            } catch (Exception e) {
                                error "Build failed for ${service}"
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully!"
        }
        failure {
            echo "Pipeline failed!"
        }
        always {
            script {
                echo "Pipeline execution ended with status: ${currentBuild.result}"
            }
        }
    }
}
