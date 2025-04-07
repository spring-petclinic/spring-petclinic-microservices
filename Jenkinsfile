pipeline {
    agent any

    environment {
        MIN_COVERAGE = 70
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changed Services') {
            steps {
                script {
                    def services = [
                        'spring-petclinic-customers-service',
                        'spring-petclinic-vets-service',
                        'spring-petclinic-visits-service'
                    ]

                    sh 'git fetch origin refs/heads/main:refs/remotes/origin/main'

                    def list_files = sh(
                        script: "git diff --name-only origin/main HEAD",
                        returnStdout: true
                    ).trim().split('\n')

                    echo "List files: ${list_files}"

                    def affectedServicesList = []

                    for (svc in services) {
                        if (list_files.any { it.startsWith("${svc}/") }) {
                            echo "service: ${svc}"
                            affectedServicesList.add(svc)
                            echo "List services: ${affectedServicesList}"
                        }
                    }

                    if (affectedServicesList) {
                        echo "String services: ${affectedServicesList.join(' ')}"
                        env.SERVICES_TO_BUILD = affectedServicesList.join(' ')
                        echo "Affected services: ${env.SERVICES_TO_BUILD}"
                    } else {
                        echo "No relevant service changes detected."
                    }
                }
            }
        }

        stage('Test Affected Services') {
            when {
                expression { return env.SERVICES_TO_BUILD?.trim() }
            }
            steps {
                script {
                    def servicesToTest = env.SERVICES_TO_BUILD.split(' ')
                    for (svc in servicesToTest) {
                        dir("${svc}") {
                            echo "Running tests for ${svc}"
                            // Chạy các bài kiểm tra của Maven
                            sh 'mvn clean test'
                            junit 'target/surefire-reports/*.xml'

                            // // Xuất báo cáo JaCoCo
                            // jacoco execPattern: 'target/jacoco.exec',
                            //     classPattern: 'target/classes',
                            //     sourcePattern: 'src/main/java'

                            sh 'mvn jacoco:report'
                            sh 'cat target/site/jacoco/jacoco.csv'

                            // Đọc báo cáo JaCoCo và tính toán coverage
                            def coverageResult = sh(script: '''
                                tail -n +2 target/site/jacoco/jacoco.csv | awk -F',' '
                                { total+=$4+$5; covered+=$5 }
                                END { if (total>0) { coverage=(covered/total)*100; if (coverage>100) coverage=100; print coverage } else print 0 }'
                            ''', returnStdout: true).trim()

                            // In kết quả coverage
                            echo "Coverage for ${svc}: ${coverageResult}%"

                            // Kiểm tra coverage có đạt yêu cầu không
                            def coverage = coverageResult.toFloat()
                            if (coverage < env.MIN_COVERAGE.toInteger()) {
                                error("Coverage for ${svc} is too low (${coverage}%), must be >= ${env.MIN_COVERAGE}%")
                            } else {
                                echo "Coverage OK (${coverage}%)"
                            }
                        }
                    }
                }

            }
        }

        stage('Build Affected Services') {
            when {
                expression { return env.SERVICES_TO_BUILD?.trim() }
            }
            steps {
                script {
                    def servicesToBuild = env.SERVICES_TO_BUILD.split(' ')
                    for (svc in servicesToBuild) {
                        dir("${svc}") {
                            echo "Building ${svc}"
                            sh 'mvn clean package -DskipTests'
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
