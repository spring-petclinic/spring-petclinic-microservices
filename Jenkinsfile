pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Determine Build') {
            steps {
                script {
                    // Lấy danh sách các file đã thay đổi
                    env.CHANGED_FILES = sh(returnStdout: true, script: 'git diff --name-only HEAD^ HEAD').trim()
                    // Xác định service nào cần build/test
                    def servicesToBuild = determineService(env.CHANGED_FILES.readLines())

                    // Kiểm tra xem có thay đổi nào nằm ngoài các thư mục service không
                    def changedOutsideServices = env.CHANGED_FILES.readLines().any { filePath ->
                        !servicesToBuild.any { service -> filePath.contains(service) }
                    }

                    if (changedOutsideServices) {
                        env.SERVICES_TO_BUILD = "all" // Build tất cả nếu có thay đổi bên ngoài
                    } else {
                        env.SERVICES_TO_BUILD = servicesToBuild  // Sử dụng kết quả từ hàm determineServices
                    }

                    echo "Services to build: ${env.SERVICES_TO_BUILD}" // In ra để kiểm tra
                }
            }
        }

       stage('Test') {
            when {
                expression { env.SERVICES_TO_BUILD != null }
            }
            steps {
                script {
                    if (env.SERVICES_TO_BUILD instanceof String && env.SERVICES_TO_BUILD != 'all') {
                        // Test 1 service
                        echo "Testing service: ${env.SERVICES_TO_BUILD}"
                        sh "./mvnw -f ${env.SERVICES_TO_BUILD}/pom.xml test"

                        // JUnit report
                        junit(
                            testResults: "${env.SERVICES_TO_BUILD}/target/surefire-reports/*.xml",
                            allowEmptyResults: true
                        )

                    } else if (env.SERVICES_TO_BUILD == 'all'){
                         // Test all services
                        echo "Testing all services"
                        sh "./mvnw test"

                        // JUnit report
                        junit(
                            testResults: "**/target/surefire-reports/*.xml",
                            allowEmptyResults: true
                        )

                    }
                }
            }
        }

        stage('Code Coverage') {
           when {
                expression { env.SERVICES_TO_BUILD != null }
           }
            steps {
                script {
                   if (env.SERVICES_TO_BUILD instanceof String && env.SERVICES_TO_BUILD != 'all') {
                        // Code coverage cho 1 service
                        echo "Generating code coverage for service: ${env.SERVICES_TO_BUILD}"
                        sh "./mvnw -f ${env.SERVICES_TO_BUILD}/pom.xml org.jacoco:jacoco-maven-plugin:report"
                        recordCoverage(
                            tools: [[parser: 'JACOCO', pattern: "${env.SERVICES_TO_BUILD}/target/site/jacoco/**/*.xml"]]
                        )
                    } else if(env.SERVICES_TO_BUILD == 'all'){
                         // Test all services
                        echo "Generating code coverage for all services"
                        sh "./mvnw  org.jacoco:jacoco-maven-plugin:report"
                         recordCoverage(
                            tools: [[parser: 'JACOCO', pattern: "**/target/site/jacoco/**/*.xml"]]
                        )

                    } 
                }
            }
        }

          stage('Build') {
            when {
                expression { env.SERVICES_TO_BUILD != null }
            }
            steps {
                script {
                    if (env.SERVICES_TO_BUILD instanceof String && env.SERVICES_TO_BUILD != 'all') {
                        // Build 1 service
                        echo "Building service: ${env.SERVICES_TO_BUILD}"
                        sh "./mvnw -f ${env.SERVICES_TO_BUILD}/pom.xml clean install -DskipTests"
                        archiveArtifacts artifacts: "${env.SERVICES_TO_BUILD}/target/*.jar"

                    } else if (env.SERVICES_TO_BUILD == 'all'){
                        // Build all services
                        echo "Building all services"
                        sh "./mvnw clean install -DskipTests"
                        archiveArtifacts artifacts: "**/target/*.jar"

                    } 
                }
            }
        }
    }

    post {
        always {
            echo "Finished pipeline"
        }
    }
}

// Function to determine which services to build/test
def determineService(changedFiles) {
    println "Changed Files: ${changedFiles}"
    def services = [
        'spring-petclinic-customers-service',
        'spring-petclinic-vets-service',
        'spring-petclinic-visits-service',
        'spring-petclinic-api-gateway',
        'spring-petclinic-admin-server'
    ]
    // Không cần danh sách servicesToBuild nữa, vì chỉ cần tìm một

    for (service in services) {
        if (changedFiles.any { it.contains(service) }) {
            return service // Trả về ngay khi tìm thấy service đầu tiên
        }
    }

    return null // Trả về null nếu không tìm thấy
}
