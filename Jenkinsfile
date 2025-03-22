pipeline {
    agent {
        label 'development-server'
    }

    tools {
        maven '3.9.9'
    }

    environment {
        PROJECT_NAME = 'ing-petclinic-microservices'
        PROJECT_PATH = "${WORKSPACE}/${PROJECT_NAME}"
        USERNAME = '22120207'
    }

    stages {
        stage('Check SCM') {
            steps {
                sh 'echo "Checking SCM..."'
            }
        }

        stage('Run Unit Test') {
            steps {
                sh 'mvn test -pl spring-petclinic-visits-service -Dtest=VisitResourceTest'

                jacoco(
                    classPattern: '**/spring-petclinic-visits-service/target/classes', 
                    execPattern: '**/spring-petclinic-visits-service/target/jacoco.exec',
                    sourcePattern: '**/spring-petclinic-visits-service/src/main/java',
                    exclusionPattern: '**/*Test*.class',
                    changeBuildStatus: true,
                    minimumInstructionCoverage: '70',
                    maximumInstructionCoverage: '100'
                )
                
                script {
                    def coverageReport = readFile(file: "${WORKSPACE}/spring-petclinic-visits-service/target/site/jacoco/index.html")
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