pipeline {
    agent any

    tools {
        maven 'Maven 3.8.6' // tên giống tên bạn đã đặt khi thêm Maven tool trong Jenkins
    }

    options {
        skipDefaultCheckout()
    }

    environment {
        MAVEN_OPTS = "-Dmaven.repo.local=.m2/repository"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    if (env.CHANGE_ID) {
                        echo "PR detected: Fetching PR from refs/pull/${env.CHANGE_ID}/head"
                    } else {
                        echo "No PR detected, checking out branch ${env.BRANCH_NAME}"
                    }
                }
            }
        }

        stage('Determine Changed Services') {
            steps {
                script {
                    def baseCommit = sh(script: '''
                        git fetch origin main || true
                        if git show-ref --verify --quiet refs/remotes/origin/main; then
                            git merge-base origin/main HEAD
                        else
                            git rev-parse HEAD~1
                        fi
                    ''', returnStdout: true).trim()

                    def changedServices = sh(script: "git diff --name-only ${baseCommit} HEAD | awk -F/ '{print \$1}' | sort -u", returnStdout: true).trim().split('\n')

                    def allServices = [
                        'spring-petclinic-vets-service',
                        'spring-petclinic-visits-service',
                        'spring-petclinic-customers-service',
                        'spring-petclinic-genai-service'
                    ]

                    def changedServicesList = changedServices as List
                    env.SERVICES_TO_BUILD = allServices.findAll { it in changedServicesList }.join(',')
                    echo "Services to test and build: ${env.SERVICES_TO_BUILD}"
                }
            }
        }

       stage('Test') {
            when {
                expression { return env.SERVICES_TO_BUILD?.trim() }
            }
            steps {
                script {
                    def services = env.SERVICES_TO_BUILD.split(',')
                    for (s in services) {
                        dir("${s}") {
                            echo "Testing service: ${s}"
                            sh "mvn clean test jacoco:report"

                            junit '**/target/surefire-reports/*.xml'
                            jacoco execPattern: '**/target/jacoco.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java'

                            // Check instruction coverage from jacoco.xml
                            def jacocoXmlPath = "target/site/jacoco/jacoco.xml"
                            def xmlFile = new File(jacocoXmlPath)

                            if (!xmlFile.exists()) {
                                echo "jacoco.xml not found for ${s}, skipping coverage check"
                                continue
                            }

                            def parser = new XmlSlurper().parse(xmlFile)
                            def instructionCounter = parser.counter.find { it.@type == 'INSTRUCTION' }
                            def missed = instructionCounter.@missed.toInteger()
                            def covered = instructionCounter.@covered.toInteger()
                            def total = missed + covered
                            def coverage = (covered / total) * 100

                            echo "${s} instruction coverage: ${String.format('%.2f', coverage)}%"

                            if (coverage < 70.0) {
                                error "${s} instruction coverage is below 70% (${String.format('%.2f', coverage)}%). Failing pipeline."
                            }
                        }
                    }
                }
            }
        }
        
        stage('Build') {
            when {
                expression { return env.SERVICES_TO_BUILD?.trim() }
            }
            steps {
                script {
                    def services = env.SERVICES_TO_BUILD.split(',')
                    for (s in services) {
                        dir("${s}") {
                            echo "Building service: ${s}"
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
        failure {
            echo "Pipeline failed"
        }
    }
}
