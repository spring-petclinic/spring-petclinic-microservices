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
        
        stage('Coverage Processing') {
            steps {
                script {
                    // Process coverage for multibranch view
                    def coveragePattern = (env.CHANGED_SERVICES == 'all') ? 
                        '**/target/site/jacoco/jacoco.xml' : 
                        env.CHANGED_SERVICES.split(',').collect { 
                            "**/${it}/target/site/jacoco/jacoco.xml" 
                        }.join(',')
                    
                    recordCoverage(
                        tools: [[
                            parser: 'JACOCO',
                            pattern: coveragePattern
                        ]],
                        sourceFileResolver: [
                            [projectDir: "$WORKSPACE"],
                            [projectDir: "$WORKSPACE", subDir: "spring-petclinic-*"]
                        ],
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
                    
                    // Archive coverage reports as build artifacts instead of using publishHTML
                    if (env.CHANGED_SERVICES != 'all') {
                        env.CHANGED_SERVICES.split(',').each { service ->
                            archiveArtifacts artifacts: "${service}/target/site/jacoco/**/*"
                        }
                    } else {
                        archiveArtifacts artifacts: "target/site/jacoco/**/*"
                    }
                }
            }
        }
    }
    
    post {
        always {
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
    
    return changedServices.isEmpty() ? '' : changedServices.join(',')
}
