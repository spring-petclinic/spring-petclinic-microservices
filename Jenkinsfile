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
                        sh './mvnw clean verify -Djacoco.destFile=target/jacoco.exec'
                    } else {
                        def services = env.CHANGED_SERVICES.split(',')
                        services.each { service ->
                            sh "./mvnw clean verify -pl :${service} -am -Djacoco.destFile=${service}/target/jacoco.exec"
                        }
                    }
                }
            }
        }
        
        stage('Coverage Processing') {
            steps {
                script {
                    // Ensure reports are generated
                    if (env.CHANGED_SERVICES == 'all') {
                        sh './mvnw jacoco:report -Djacoco.dataFile=target/jacoco.exec'
                    } else {
                        env.CHANGED_SERVICES.split(',').each { service ->
                            sh "./mvnw jacoco:report -pl :${service} -Djacoco.dataFile=${service}/target/jacoco.exec"
                        }
                    }
                    
                    // Record coverage with proper paths
                    recordCoverage(
                        tools: [[
                            parser: 'JACOCO',
                            pattern: (env.CHANGED_SERVICES == 'all') ? 
                                '**/target/site/jacoco/jacoco.xml' : 
                                env.CHANGED_SERVICES.split(',').collect { 
                                    "**/${it}/target/site/jacoco/jacoco.xml" 
                                }.join(',')
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
                }
            }
        }
    }
    
    post {
        always {
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
            
            // Archive the specific files we know exist
            script {
                if (env.CHANGED_SERVICES == 'all') {
                    archiveArtifacts artifacts: 'target/site/jacoco/jacoco.xml,target/site/jacoco/index.html'
                } else {
                    env.CHANGED_SERVICES.split(',').each { service ->
                        archiveArtifacts artifacts: "${service}/target/site/jacoco/jacoco.xml,${service}/target/site/jacoco/index.html"
                    }
                }
            }
            
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
