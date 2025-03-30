pipeline {
    agent any

    tools {
        maven 'maven3.9.9' // Tên Maven trong Global Tool Configuration
    }

    options {
        skipDefaultCheckout()
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
                    env.BUILD_VETS = changedFiles.contains("spring-petclinic-vets-service/")
                    env.BUILD_VISITS = changedFiles.contains("spring-petclinic-visits-service/")
                    env.BUILD_CUSTOMERS = changedFiles.contains("spring-petclinic-customers-service/")
                }
            }
        }

        stage('Build & Test Services') {
            matrix {
                axes {
                    axis {
                        name 'SERVICE'
                        values 'spring-petclinic-vets-service', 'spring-petclinic-visits-service', 'spring-petclinic-customers-service'
                    }
                }
                when {
                    expression { env["BUILD_${SERVICE.toUpperCase()}"] == "true" }
                }
                stages {
                    stage('Build') {
                        steps {
                            dir("${SERVICE}") {
                                sh "mvn clean package -DskipTests"
                            }
                        }
                    }
                    // stage('Test & Coverage') {
                    //     steps {
                    //         dir("${SERVICE}") {
                    //             sh "mvn test"
                    //             junit '**/target/surefire-reports/*.xml'
                    //             cobertura coberturaReportFile: '**/target/site/cobertura/coverage.xml'
                    //         }
                    //     }
                    // }
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline completed!"
        }
    }
}
