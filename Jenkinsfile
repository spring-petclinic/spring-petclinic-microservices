pipeline {
    agent {
        label 'development-server'
    }

    tools {
        maven '3.9.9'
    }

    environment {
        USERNAME = '22120207'
    }

    stages {
        stage('Check SCM') {
            steps {
                checkout scm
                sh "echo ${WORKSPACE}"
            }
        }

        stage('Check Changed Files') {
            steps {
                script {
                    def targetBranch = env.CHANGE_TARGET ?: 'test'
                    def changedFiles = sh(script: "git diff --name-only origin/${targetBranch}", returnStdout: true).trim()
                    echo "Changed Files:\n${changedFiles}"
                }
            }
        }  

        stage('Run Unit Test') {
            steps {
                sh 'mvn test -pl spring-petclinic-visits-service -Dtest=VisitResourceTest'
                sh 'mvn jacoco:report -pl spring-petclinic-visits-service'

                script {
                    def reportFile = "${WORKSPACE}/spring-petclinic-visits-service/target/site/jacoco/index.html"
                    if (fileExists(reportFile)) {
                        def coverageReport = readFile(file: reportFile)
                        def matcher = coverageReport =~ /<tfoot>(.*?)<\/tfoot>/
                        if (matcher.find()) {
                            def coverage = matcher[0]
                            def instructionMatcher = coverage =~ /<td class="ctr2">(.*?)%<\/td>/
                            if (instructionMatcher.find()) {
                                def coveragePercentage = instructionMatcher[0][1]
                                echo "Overall code coverage: ${coveragePercentage}%"
                                env.CODE_COVERAGE = coveragePercentage
                            }
                        }
                    } else {
                        error "JaCoCo report not found at ${reportFile}"
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    sh 'echo "Building..."'
                    echo "Previous stage code coverage: ${env.CODE_COVERAGE}%"
                }
            }
        }
    }
    
    post {
        always {
            publishHTML(
                target: [
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'spring-petclinic-visits-service/target/site/jacoco',
                    reportFiles: 'index.html',
                    reportName: 'JaCoCo Code Coverage'
                ]
            )
        }
    }
}
