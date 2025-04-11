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
                    // Generate aggregated report
                    sh './mvnw jacoco:report-aggregate'
                    
                    // Record coverage with proper paths
                    recordCoverage(
                        tools: [[
                            parser: 'JACOCO',
                            pattern: env.CHANGED_SERVICES == 'all' ? 
                                '**/target/site/jacoco/jacoco.xml' : 
                                env.CHANGED_SERVICES.split(',').collect { 
                                    "**/${it}/target/site/jacoco/jacoco.xml" 
                                }.join(',')
                        ]],
                        sourceFileResolver: [
                            [projectDir: "$WORKSPACE"],
                            [projectDir: "$WORKSPACE", subDir: "spring-petclinic-*"]
                        ]
                    )
                    
                    // Publish HTML report for visualization
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
            
            // Clean workspace
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
