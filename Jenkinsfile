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
                            echo "Checking out PR #${env.CHANGE_ID} (target: ${env.CHANGE_TARGET})"
                            sh "git init"
                            sh "git remote add origin ${REPO_URL}"
                            sh "git fetch origin refs/pull/${env.CHANGE_ID}/merge:pr-${env.CHANGE_ID}"
                            sh "git checkout pr-${env.CHANGE_ID}"
                        } else {
                            echo "Checking out branch ${env.BRANCH_NAME}"
                            sh "git clone -b ${env.BRANCH_NAME} ${REPO_URL} ."
                            sh "git fetch origin ${env.BRANCH_NAME}"  // Đảm bảo có dữ liệu để diff
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
                            changes = sh(script: "git diff --name-only FETCH_HEAD", returnStdout: true).trim()
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
    }
}
