pipeline {
    agent any

    tools {
        maven 'maven3.9.9'
    }

    options {
        skipDefaultCheckout()
    }

    environment {
        BUILD_VETS = 'false'
        BUILD_VISITS = 'false'
        BUILD_CUSTOMERS = 'false'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    checkout scm
                }
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()
                    echo "Changed files:\n${changedFiles}"
                    env.changedFiles = changedFiles
                }
            }
        }

        stage('Build & Test Services') {
            matrix {
                axes {
                    axis {
                        name 'SERVICE'
                        values 'spring-petclinic-vets-service', 
                               'spring-petclinic-visits-service', 
                               'spring-petclinic-customers-service'
                    }
                }

                when {
                    expression {
                        return (SERVICE == 'spring-petclinic-vets-service' && changedFiles.contains("vets-service")) ||
                               (SERVICE == 'spring-petclinic-visits-service' && changedFiles.contains("visits-service")) ||
                               (SERVICE == 'spring-petclinic-customers-service' && changedFiles.contains("customers-service"))
                    }
                }

                stages {
                    stage('Build') {
                        steps {
                            dir("${SERVICE}") {
                                sh "mvn clean package -DskipTests"
                            }
                        }
                    }
                    stage('Test') {
                        steps {
                            dir("${SERVICE}") {
                                sh "mvn test verify"
                                junit '**/target/surefire-reports/*.xml'
                            }
                        }
                    }
                }
            }
        }

        stage('Publish Coverage') {
            steps {
                jacoco(
                    execPattern: '**/target/jacoco.exec',
                    classPattern: '**/target/classes',
                    sourcePattern: '**/src/main/java',
                    inclusionPattern: '**/*.class',
                    exclusionPattern: '**/*Test.class',
                    minimumInstructionCoverage: '70',
                    minimumBranchCoverage: '70'
                )
            }
        }
    }

    post {
        always {
            echo "Pipeline completed!"
        }
    }
}
