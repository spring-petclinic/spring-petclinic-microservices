pipeline {
    agent any

    options {
        buildDiscarder(logRotator(
            numToKeepStr: '10',
            artifactNumToKeepStr: '5'
        ))
        // Thêm timeout để tránh build treo
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('Checkout with Full History') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    extensions: [
                        // Tắt shallow clone để lấy toàn bộ history
                        [$class: 'CloneOption', depth: 0, shallow: false],
                        // Xóa workspace trước khi checkout để sạch sẽ
                        [$class: 'CleanBeforeCheckout']
                    ],
                    userRemoteConfigs: [[url: 'https://github.com/OpsInUs/DA01-Jenkins.git']]
                ])
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    sh 'pwd'
                    // So sánh với branch chính (main/master) thay vì chỉ HEAD~1
                    def changedFiles = sh(script: 'git diff --name-only origin/main HEAD', returnStdout: true).trim()
                    echo "Changed files: ${changedFiles}"

                    // Debug: Hiển thị lịch sử commit
                    sh 'git log --oneline -n 5'

                    def changedServices = []

                    // Sửa lỗi chính tả 'customers' (trước đây là 'customers')
                    if (changedFiles.contains('spring-petclinic-genai-service')) {
                        changedServices.add('genai')
                    }
                    if (changedFiles.contains('spring-petclinic-customers-service')) {
                        changedServices.add('customers')
                    }
                    if (changedFiles.contains('spring-petclinic-vets-service')) {
                        changedServices.add('vets')
                    }
                    if (changedFiles.contains('spring-petclinic-visits-service')) {
                        changedServices.add('visits')
                    }
                    if (changedFiles.contains('spring-petclinic-api-gateway')) {
                        changedServices.add('api-gateway')
                    }
                    if (changedFiles.contains('spring-petclinic-discovery-server')) {
                        changedServices.add('discovery')
                    }
                    if (changedFiles.contains('spring-petclinic-config-server')) {
                        changedServices.add('config')
                    }
                    if (changedFiles.contains('spring-petclinic-admin-server')) {
                        changedServices.add('admin')
                    }

                    if (changedServices.isEmpty()) {
                        changedServices = ['all']
                    }

                    echo "Detected changes in services: ${changedServices}"
                    env.CHANGED_SERVICES_LIST = changedServices
                    env.CHANGED_SERVICES_STRING = changedServices.join(',')
                }
            }
        }

        stage('Test with Strict Coverage') {
            steps {
                script {
                    try {
                        if (env.CHANGED_SERVICES_LIST.contains('all')) {
                            echo 'Testing all modules'
                            sh './mvnw clean test'
                        } else {
                            def modules = env.CHANGED_SERVICES_LIST.collect { "spring-petclinic-${it}-service" }.join(',')
                            echo "Testing modules: ${modules}"
                            sh "./mvnw clean test -pl ${modules}"
                        }
                    } catch (Exception e) {
                        error("Unit tests failed: ${e.getMessage()}")
                    }
                }
            }
            post {
                always {
                    junit testResults: '**/surefire-reports/TEST-*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: '**/target/surefire-reports/*, **/target/jacoco.exec', allowEmptyArchive: true
                }
            }
        }

        stage('Enforce Strict Coverage') {
            steps {
                script {
                    def jacocoPattern = env.CHANGED_SERVICES_LIST.contains('all') ?
                        '**/jacoco.exec' :
                        env.CHANGED_SERVICES_LIST.collect { "spring-petclinic-${it}-service/target/jacoco.exec" }.join(',')

                    // Fail build if no coverage data found
                    if (findFiles(glob: jacocoPattern).size() == 0) {
                        error("FATAL: No JaCoCo coverage data found! Tests may not have run correctly.")
                    }

                    // STRICT COVERAGE RULES
                    jacoco(
                        execPattern: jacocoPattern,
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java',

                        // MAIN COVERAGE THRESHOLDS (will fail build if not met)
                        minimumInstructionCoverage: '70',
                        minimumLineCoverage: '70',
                        minimumMethodCoverage: '70',


                        // ENFORCEMENT SETTINGS
                        changeBuildStatus: true,
                        skipCopyOfSrcFiles: false,

                        // BUILD WILL FAIL IF:
                        // 1. Any non-excluded package has <70% instruction/line/method coverage
                        // 2. No coverage data is found
                    )
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    if (env.CHANGED_SERVICES_LIST.contains('all')) {
                        echo 'Building all modules'
                        sh './mvnw clean package -DskipTests'
                    } else {
                        def modules = env.CHANGED_SERVICES_LIST.collect { "spring-petclinic-${it}-service" }.join(',')
                        echo "Building modules: ${modules}"
                        sh "./mvnw clean package -DskipTests -pl ${modules}"
                    }
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                }
            }
        }
    }

    post {
        failure {
            emailext body: '${DEFAULT_CONTENT}\n\nBuild URL: ${BUILD_URL}',
                     subject: 'FAILED: Job ${JOB_NAME} - Build ${BUILD_NUMBER}',
                     to: 'your-email@example.com'
        }
        success {
            echo 'Build succeeded!'
        }
    }
}
