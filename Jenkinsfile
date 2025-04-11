pipeline {
    agent any
    
    tools {
        maven 'M3'
        jdk 'jdk17'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build & Test') {
            steps {
                script {
                    // Always use clean verify to ensure JaCoCo reports are generated
                    sh 'mvn clean verify'
                }
            }
        }
        
        stage('Coverage') {
            steps {
                script {
                    // Record coverage with explicit paths
                    recordCoverage(
                        tools: [[
                            parser: 'JACOCO',
                            pattern: '**/target/site/jacoco/jacoco.xml'
                        ]],
                        sourceFileResolver: [
                            [projectDir: "$WORKSPACE"]
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
                    
                    // Archive the coverage report files
                    archiveArtifacts artifacts: '**/target/site/jacoco/jacoco.xml,**/target/site/jacoco/index.html'
                }
            }
        }
    }
    
    post {
        always {
            junit '**/target/surefire-reports/*.xml'
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
