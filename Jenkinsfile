pipeline {
    agent any
    
    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }
    
    environment {
        CHANGED_SERVICES = ""
        MINIMUM_COVERAGE = 70
    }
    
    stages {
        stage('Detect Changes') {
            steps {
                script {
                    // Define services array here, not in environment
                    def SERVICES = ['spring-petclinic-admin-server', 
                                    'spring-petclinic-api-gateway', 
                                    'spring-petclinic-config-server', 
                                    'spring-petclinic-discovery-server', 
                                    'spring-petclinic-customers-service', 
                                    'spring-petclinic-vets-service', 
                                    'spring-petclinic-visits-service']
                    
                    def compareTarget = env.CHANGE_TARGET ? "origin/${env.CHANGE_TARGET}" : "HEAD~1"
                    def changedFiles = sh(script: "git diff --name-only ${compareTarget}", returnStdout: true).trim()
                    
                    def changedServicesList = []
                    SERVICES.each { service ->
                        if (changedFiles.split("\n").any { it.startsWith(service) }) {
                            changedServicesList.add(service)
                        }
                    }
                    
                    CHANGED_SERVICES = changedServicesList.join(",")
                    
                    if (CHANGED_SERVICES.isEmpty() && 
                        changedFiles.split("\n").any { it == "pom.xml" || it.startsWith("src/") }) {
                        CHANGED_SERVICES = SERVICES.join(",")
                    }
                    
                    echo "Services to build: ${CHANGED_SERVICES ?: 'None'}"
                }
            }
        }
        
        stage('Test') {
            when { expression { return !CHANGED_SERVICES.isEmpty() } }
            steps {
                script {
                    def SERVICES = ['spring-petclinic-admin-server', 
                                    'spring-petclinic-api-gateway', 
                                    'spring-petclinic-config-server', 
                                    'spring-petclinic-discovery-server', 
                                    'spring-petclinic-customers-service', 
                                    'spring-petclinic-vets-service', 
                                    'spring-petclinic-visits-service']
                    
                    if (CHANGED_SERVICES == SERVICES.join(",")) {
                        sh 'mvn verify'
                    } else {
                        CHANGED_SERVICES.split(",").each { service ->
                            dir(service) {
                                echo "Testing ${service}"
                                sh 'mvn verify'
                            }
                        }
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    
                    // Make the build unstable if coverage is below threshold
                    recordCoverage(
                        tools: [[parser: 'JACOCO']],
                        sourceDirectories: [[path: 'src/main/java']],
                        sourceCodeRetention: 'EVERY_BUILD',
                        qualityGates: [
                            [threshold: env.MINIMUM_COVERAGE.toInteger(), metric: 'LINE', baseline: 'PROJECT', unstable: true]
                        ]
                    )
                    
                    // Now check if build became unstable due to coverage, and fail it explicitly
                    script {
                        if (currentBuild.result == 'UNSTABLE') {
                            error "Build failed: Line coverage is below the required minimum ${env.MINIMUM_COVERAGE}%"
                        }
                    }
                    
                    // Debug step to verify JaCoCo files
                    sh 'echo "Checking for JaCoCo files:"'
                    sh 'find . -name "jacoco.exec" | xargs ls -la || echo "No jacoco.exec files found"'
                    sh 'find . -name "jacoco.xml" | xargs ls -la || echo "No jacoco.xml files found"'
                }
            }
        }
        
        stage('Build') {
            when { expression { return !CHANGED_SERVICES.isEmpty() } }
            steps {
                script {
                    def SERVICES = ['spring-petclinic-admin-server', 
                                    'spring-petclinic-api-gateway', 
                                    'spring-petclinic-config-server', 
                                    'spring-petclinic-discovery-server', 
                                    'spring-petclinic-customers-service', 
                                    'spring-petclinic-vets-service', 
                                    'spring-petclinic-visits-service']
                    
                    if (CHANGED_SERVICES == SERVICES.join(",")) {
                        sh 'mvn package -DskipTests'
                    } else {
                        CHANGED_SERVICES.split(",").each { service ->
                            dir(service) {
                                echo "Building ${service}"
                                sh 'mvn package -DskipTests'
                            }
                        }
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo "Pipeline completed with result: ${currentBuild.currentResult}"
            echo "Pipeline run by: ${currentBuild.getBuildCauses()[0].userId ?: 'unknown'}"
            echo "Completed at: ${new java.text.SimpleDateFormat('yyyy-MM-dd HH:mm:ss').format(new Date())}"
            cleanWs()
        }
    }
}