pipeline {
    agent any

    options {
        skipDefaultCheckout()
    }

    environment {
        MAVEN_OPTS = "-Dmaven.repo.local=.m2/repository"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "*/${env.BRANCH_NAME}"]],
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [[$class: 'CloneOption', noTags: false, shallow: false, depth: 0]],
                    userRemoteConfigs: [[
                        url: 'https://github.com/MyTruong28022004/spring-petclinic-microservices.git',
                        credentialsId: 'github-token-1'
                    ]]
                ])
            }
        }

        stage('Determine Changed Services') {
            steps {
                script {
                    // Lấy base commit để so sánh, hoạt động cả với PR
                    def baseCommit = sh(script: '''
                        git fetch origin main || true
                        if git show-ref --verify --quiet refs/remotes/origin/main; then
                            git merge-base origin/main HEAD
                        else
                            git rev-parse HEAD~1
                        fi
                    ''', returnStdout: true).trim()

                    // Lấy danh sách file thay đổi
                    def changedServices = sh(script: "git diff --name-only ${baseCommit} HEAD | awk -F/ '{print \$1}' | sort -u", returnStdout: true).trim().split('\n')

                    def allServices = [
                        'spring-petclinic-vets-service',
                        'spring-petclinic-visits-service',
                        'spring-petclinic-customers-service',
                        'spring-petclinic-genai-service'
                    ]

                    def changedServicesList = changedServices as List
                    env.SERVICES_TO_BUILD = allServices.findAll { it in changedServicesList }.join(',')
                    echo "Services to build: ${env.SERVICES_TO_BUILD}"
                }
            }
        }

        stage('Build and Test') {
            when {
                expression { return env.SERVICES_TO_BUILD?.trim() }
            }
            steps {
                script {
                    def services = env.SERVICES_TO_BUILD.split(',')
                    for (s in services) {
                        dir("${s}") {
                            sh "mvn clean test"
                            junit '**/target/surefire-reports/*.xml'
                            jacoco execPattern: '**/target/jacoco.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java'
                            sh "mvn clean package"
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
