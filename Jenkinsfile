pipeline {
    agent any

    options {
        buildDiscarder(logRotator(
            numToKeepStr: '10',      // Giữ logs của 10 builds
            artifactNumToKeepStr: '5' // Chỉ giữ artifacts của 5 builds gần nhất
        ))
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = sh(script: 'git diff --name-only HEAD~1 HEAD', returnStdout: true).trim()
                    echo "Changed files: ${changedFiles}"

                    def changedServices = []

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

                    CHANGED_SERVICES_LIST = changedServices
                    CHANGED_SERVICES_STRING = changedServices.join(',')
                    echo "Changed services: ${CHANGED_SERVICES_STRING}"
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if (CHANGED_SERVICES_LIST.contains('all')) {
                        echo 'Testing all modules'
                        sh './mvnw clean test'
                    } else {
                        def modules = CHANGED_SERVICES_LIST.collect { "spring-petclinic-${it}-service" }.join(',')
                        echo "Testing modules: ${modules}"
                        sh "./mvnw clean test -pl ${modules}"
                    }
                }
            }
            post {
                always {
                    script {
                        def testReportPattern = env.CHANGED_SERVICES.split(',').collect { "${it == 'all' ? '' : 'spring-petclinic-' + it + '-service'}/target/surefire-reports/*.xml" }.join(',')
                        echo "Looking for test reports in: ${testReportPattern}"
                        if (fileExists(testReportPattern)) {
                            junit testReportPattern
                        } else {
                            echo "No test reports found in ${testReportPattern}"
                        }

                        def jacocoPattern = env.CHANGED_SERVICES.split(',').collect { "${it == 'all' ? '' : 'spring-petclinic-' + it + '-service'}/target/jacoco.exec" }.join(',')
                        echo "Looking for JaCoCo data in: ${jacocoPattern}"
                        if (fileExists(jacocoPattern)) {
                            jacoco(
                                execPattern: jacocoPattern,
                                classPattern: env.CHANGED_SERVICES.split(',').collect { "${it == 'all' ? '' : 'spring-petclinic-' + it + '-service'}/target/classes" }.join(','),
                                sourcePattern: env.CHANGED_SERVICES.split(',').collect { "${it == 'all' ? '' : 'spring-petclinic-' + it + '-service'}/src/main/java" }.join(',')
                            )
                        } else {
                            echo "No JaCoCo execution data found in ${jacocoPattern}"
                        }
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    if (CHANGED_SERVICES_LIST.contains('all')) {
                        echo 'Building all modules'
                        sh './mvnw clean package -DskipTests'
                    } else {
                        def modules = CHANGED_SERVICES_LIST.collect { "spring-petclinic-${it}-service" }.join(',')
                        echo "Building modules: ${modules}"
                        sh "./mvnw clean package -DskipTests -pl ${modules}"
                    }
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                }
            }
        }
    }
}
