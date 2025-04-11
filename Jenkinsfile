pipeline {
    agent any
    
    tools {
        maven 'M3'
        jdk 'jdk17'
    }
    
    stages {
        stage('Checkout & Detect Changes') {
            steps {
                checkout scm
                script {
                    def changes = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()
                    env.CHANGED_SERVICES = getChangedServices(changes)
                }
            }
        }
        
        stage('Build & Test') {
            steps {
                script {
                    if (env.CHANGED_SERVICES == 'all') {
                        sh './mvnw clean verify'
                    } else {
                        def services = env.CHANGED_SERVICES.split(',')
                        services.each { service ->
                            sh "./mvnw clean verify -pl :${service} -am"
                        }
                    }
                }
            }
        }
        
        stage('Coverage Analysis') {
            steps {
                script {
                    // Generate aggregated coverage report
                    sh './mvnw jacoco:report-aggregate'
                    
                    // Define coverage pattern based on changed services
                    def coveragePattern = (env.CHANGED_SERVICES == 'all') ? 
                        '**/target/site/jacoco/jacoco.xml' : 
                        env.CHANGED_SERVICES.split(',').collect { 
                            "**/${it}/target/site/jacoco/jacoco.xml" 
                        }.join(',')
                    
                    // Record coverage for multibranch view
                    recordCoverage(
                        tools: [[
                            parser: 'JACOCO',
                            pattern: coveragePattern
                        ]],
                        sourceFileResolver: [
                            [projectDir: "$WORKSPACE"],
                            [projectDir: "$WORKSPACE", subDir: "spring-petclinic-*"]
                        ],
                        // Optional: Set coverage thresholds
                        healthyTarget: [
                            instructionCoverage: 70,
                            lineCoverage: 70,
                            branchCoverage: 60
                        ],
                        unstableTarget: [
                            instructionCoverage: 60,
                            lineCoverage: 60,
                            branchCoverage: 50
                        ]
                    )
                    
                    // Publish HTML report for detailed viewing
                    publishHTML(
                        target: [
                            reportDir: 'target/site/jacoco-aggregate',
                            reportFiles: 'index.html',
                            reportName: 'JaCoCo Coverage Report',
                            keepAll: true
                        ]
                    )
                }
            }
        }
    }
    
    post {
        always {
            // Publish test results
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
            cleanWs()
        }
    }
}

def getChangedServices(String changes) {
    if (changes.isEmpty() || changes.contains('pom.xml')) {
        return 'all'
    }
    
    def serviceMap = [
        'spring-petclinic-api-gateway': 'api-gateway',
        'spring-petclinic-customers-service': 'customers-service',
        'spring-petclinic-vets-service': 'vets-service',
        'spring-petclinic-visits-service': 'visits-service',
        'spring-petclinic-config-server': 'config-server',
        'spring-petclinic-discovery-server': 'discovery-server',
        'spring-petclinic-admin-server': 'admin-server'
    ]
    
    def changedServices = []
    changes.split('\n').each { change ->
        serviceMap.each { dir, service ->
            if (change.contains(dir) && !changedServices.contains(service)) {
                changedServices.add(service)
            }
        }
    }
    
    return changedServices.isEmpty() ? 'all' : changedServices.join(',')
}
