pipeline {
    agent any
    environment {
        MVN_CMD = 'mvn clean'
    }
    tools {
        maven 'Maven 3'
    }
    stages {
        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = sh(script: "git diff --name-only origin/main", returnStdout: true).trim().split("\n")
                    env.RUN_CUSTOMERS = changedFiles.any { it.startsWith("spring-petclinic-customers-service/") } ? "true" : "false"
                    env.RUN_VETS = changedFiles.any { it.startsWith("spring-petclinic-vets-service/") } ? "true" : "false"
                    env.RUN_VISITS = changedFiles.any { it.startsWith("spring-petclinic-visits-service/") } ? "true" : "false"
                }
            }
        }
        stage('Test and Build Services') {
            parallel {
                stage('Customers Service') {
                    when { expression { env.RUN_CUSTOMERS == "true" } }
                    stages {
                        stage('Test') {
                            steps {
                                dir('spring-petclinic-customers-service') {
                                    sh "${MVN_CMD} test"
                                }
                            }
                            post {
                                always {
                                    junit 'spring-petclinic-customers-service/target/surefire-reports/*.xml'
                                    publishCoverage adapters: [jacocoAdapter('spring-petclinic-customers-service/target/site/jacoco/jacoco.xml')]
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('spring-petclinic-customers-service') {
                                    sh "${MVN_CMD} package -DskipTests"
                                }
                            }
                            post {
                                success {
                                    archiveArtifacts artifacts: 'spring-petclinic-customers-service/target/*.jar', fingerprint: true
                                }
                            }
                        }
                    }
                }
                stage('Vets Service') {
                    when { expression { env.RUN_VETS == "true" } }
                    stages {
                        stage('Test') {
                            steps {
                                dir('spring-petclinic-vets-service') {
                                    sh "${MVN_CMD} test"
                                }
                            }
                            post {
                                always {
                                    junit 'spring-petclinic-vets-service/target/surefire-reports/*.xml'
                                    publishCoverage adapters: [jacocoAdapter('spring-petclinic-vets-service/target/site/jacoco/jacoco.xml')]
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('spring-petclinic-vets-service') {
                                    sh "${MVN_CMD} package -DskipTests"
                                }
                            }
                            post {
                                success {
                                    archiveArtifacts artifacts: 'spring-petclinic-vets-service/target/*.jar', fingerprint: true
                                }
                            }
                        }
                    }
                }
                stage('Visits Service') {
                    when { expression { env.RUN_VISITS == "true" } }
                    stages {
                        stage('Test') {
                            steps {
                                dir('spring-petclinic-visits-service') {
                                    sh "${MVN_CMD} test"
                                }
                            }
                            post {
                                always {
                                    junit 'spring-petclinic-visits-service/target/surefire-reports/*.xml'
                                    publishCoverage adapters: [jacocoAdapter('spring-petclinic-visits-service/target/site/jacoco/jacoco.xml')]
                                }
                            }
                        }
                        stage('Build') {
                            steps {
                                dir('spring-petclinic-visits-service') {
                                    sh "${MVN_CMD} package -DskipTests"
                                }
                            }
                            post {
                                success {
                                    archiveArtifacts artifacts: 'spring-petclinic-visits-service/target/*.jar', fingerprint: true
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
