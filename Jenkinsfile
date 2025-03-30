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
                    
                    def services = ['spring-petclinic-vets-service', 
                                    'spring-petclinic-visits-service', 
                                    'spring-petclinic-customers-service']
                    
                    services.each { service ->
                        env["BUILD_${service}"] = changedFiles.contains("${service}/") ? "true" : "false"
                    }
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
                    expression { env["BUILD_${SERVICE}"] == "true" }
                }
                stages {
                    stage('Build') {
                        steps {
                            dir("${SERVICE}") {
                                sh "mvn clean package -DskipTests"
                            }
                        }
                    }
                    stage('Test & Coverage') {
                        steps {
                            dir("${SERVICE}") {
                                sh "mvn test"
                                junit '**/target/surefire-reports/*.xml'
                                cobertura coberturaReportFile: '**/target/site/cobertura/coverage.xml'
                            }
                        }
                    }
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
