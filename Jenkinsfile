pipeline {
    agent none  // We'll define an agent per stage instead
    environment {
        // Ensure Windows system commands can be found
        PATH = "C:\\Windows\\System32;${env.PATH}"
    }

    tools {
        maven "M3" // Ensure this matches your Maven installation configured in Jenkins
    }

    stages {
        stage('Build & Test Admin Server') {
            agent { label 'admin-agent' }
            // Only run if changes are detected in the admin server module
            when { changeset "*/spring-petclinic-admin-server/*" }
            steps {
                // Always checkout your code
                checkout scm

                // Build & test only the admin server module (plus any dependencies)
                bat 'mvn -pl spring-petclinic-admin-server -am clean test jacoco:report'
            }
            post {
                always {
                    // Publish test results
                    junit '*/spring-petclinic-admin-server/target/surefire-reports/.xml'

                    // Publish coverage (adjust if jacoco.xml is in a different path)
                    recordCoverage(
                        tools: [[parser: 'JACOCO', pattern: '**/spring-petclinic-admin-server/target/site/jacoco/jacoco.xml']],
                        qualityGates: [
                            [threshold: 70.0, metric: 'LINE', baseline: 'PROJECT', unstable: false],
                            [threshold: 70.0, metric: 'BRANCH', baseline: 'PROJECT', unstable: false]
                        ]
                    )
                }
            }
        }

        stage('Build & Test API Gateway') {
            agent { label 'gateway-agent' }
            when { changeset "*/spring-petclinic-api-gateway/*" }
            steps {
                checkout scm
                bat 'mvn -pl spring-petclinic-api-gateway -am clean test jacoco:report'
            }
            post {
                always {
                    junit '*/spring-petclinic-api-gateway/target/surefire-reports/.xml'
                    recordCoverage(
                        tools: [[parser: 'JACOCO', pattern: '**/spring-petclinic-api-gateway/target/site/jacoco/jacoco.xml']],
                        qualityGates: [
                            [threshold: 70.0, metric: 'LINE', baseline: 'PROJECT', unstable: false],
                            [threshold: 70.0, metric: 'BRANCH', baseline: 'PROJECT', unstable: false]
                        ]
                    )
                }
            }
        }

        stage('Build & Test Config Server') {
            agent { label 'config-agent' }
            when { changeset "*/spring-petclinic-config-server/*" }
            steps {
                checkout scm
                bat 'mvn -pl spring-petclinic-config-server -am clean test jacoco:report'
            }
            post {
                always {
                    junit '*/spring-petclinic-config-server/target/surefire-reports/.xml'
                    recordCoverage(
                        tools: [[parser: 'JACOCO', pattern: '**/spring-petclinic-config-server/target/site/jacoco/jacoco.xml']],
                        qualityGates: [
                            [threshold: 70.0, metric: 'LINE', baseline: 'PROJECT', unstable: false],
                            [threshold: 70.0, metric: 'BRANCH', baseline: 'PROJECT', unstable: false]
                        ]
                    )
                }
            }
        }

        stage('Build & Test Customers Service') {
            agent { label 'customers-agent' }
            when { changeset "*/spring-petclinic-customers-service/**/*" }
            steps {
                checkout scm
                bat 'mvn -pl spring-petclinic-customers-service -am clean package'
            }
            post {
                always {
                    junit '*/spring-petclinic-customers-service/target/surefire-reports/.xml'
                    recordCoverage(
                        tools: [[parser: 'JACOCO']],
                        qualityGates: [
                            [threshold: 70.0, metric: 'LINE', baseline: 'PROJECT', unstable: false],
                            [threshold: 70.0, metric: 'BRANCH', baseline: 'PROJECT', unstable: false]
                        ]
                    )
                }
            }
        }

        stage('Build & Test Discovery Server') {
            agent { label 'discovery-agent' }
            when { changeset "*/spring-petclinic-discovery-server/*" }
            steps {
                checkout scm
                bat 'mvn -pl spring-petclinic-discovery-server -am clean test jacoco:report'
            }
            post {
                always {
                    junit '*/spring-petclinic-discovery-server/target/surefire-reports/.xml'
                    recordCoverage(
                        tools: [[parser: 'JACOCO', pattern: '**/spring-petclinic-discovery-server/target/site/jacoco/jacoco.xml']],
                        qualityGates: [
                            [threshold: 70.0, metric: 'LINE', baseline: 'PROJECT', unstable: false],
                            [threshold: 70.0, metric: 'BRANCH', baseline: 'PROJECT', unstable: false]
                        ]
                    )
                }
            }
        }

        stage('Build & Test GenAI Service') {
            agent { label 'genai-agent' }
            when { changeset "*/spring-petclinic-genai-service/**/" }
            steps {
                checkout scm
                bat 'mvn -Dmaven.test.failure.ignore=true -pl spring-petclinic-genai-service -am clean package'
            }
            post {
                success {
                    junit '*/spring-petclinic-genai-service/target/surefire-reports/.xml'
                    recordCoverage(tools: [[parser: 'JACOCO']],
                        id: 'jacoco', name: 'JaCoCo Coverage',
                        sourceCodeRetention: 'EVERY_BUILD',
                        qualityGates: [
                                [threshold: 70.0, metric: 'LINE', baseline: 'PROJECT', unstable: false],
                                [threshold: 70.0, metric: 'BRANCH', baseline: 'PROJECT', unstable: false]])

                }
            }
        }

        stage('Build & Test Vets Service') {
            agent { label 'vets-agent' }
            when { changeset "*/spring-petclinic-vets-service/**/*" }
            steps {
                checkout scm
                bat 'mvn -pl spring-petclinic-vets-service -am clean package'
            }
            post {
                always {
                    junit '*/spring-petclinic-vets-service/target/surefire-reports/.xml'
                    recordCoverage(
                        tools: [[parser: 'JACOCO']],
                        qualityGates: [
                            [threshold: 70.0, metric: 'LINE', baseline: 'PROJECT', unstable: false],
                            [threshold: 70.0, metric: 'BRANCH', baseline: 'PROJECT', unstable: false]
                        ]
                    )
                }
            }
        }

        stage('Build & Test Visits Service') {
            agent { label 'visits-agent' }
            when { changeset "*/spring-petclinic-visits-service/**/*" }
            steps {
                checkout scm
                bat 'mvn -pl spring-petclinic-visits-service -am clean package'
            }
            post {
                always {
                    junit '*/spring-petclinic-visits-service/target/surefire-reports/.xml'
                    recordCoverage(
                        tools: [[parser: 'JACOCO']],
                        qualityGates: [
                            [threshold: 70.0, metric: 'LINE', baseline: 'PROJECT', unstable: false],
                            [threshold: 70.0, metric: 'BRANCH', baseline: 'PROJECT', unstable: false]
                        ]
                    )
                }
            }
        }
    }

    post {
        success {
            echo 'All builds succeeded!'
        }
        failure {
            echo 'One or more builds failed!'
        }
    }
}
