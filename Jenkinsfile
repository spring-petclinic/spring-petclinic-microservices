pipeline {
    agent any

    tools {
        maven 'M3'
        jdk 'jdk11'
    }

    environment {
        COVERAGE_THRESHOLD = 70
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    def changedFiles = getChangedFiles()
                    env.CHANGED_SERVICES = changedFiles.collect { 
                        findServiceDir(it) 
                    }.findAll { it != null }.unique().join(',')
                    
                    if (!env.CHANGED_SERVICES) {
                        env.CHANGED_SERVICES = getAllServices().join(',')
                    }
                }
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(',') as List
                    def parallelStages = [:]
                    
                    services.each { service ->
                        parallelStages[service] = {
                            stage("Build ${service}") {
                                dir(service) {
                                    sh 'mvn clean verify -pl . -am'
                                }
                            }
                        }
                    }
                    parallel parallelStages
                }
            }
        }

        stage('Coverage Analysis') {
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(',') as List
                    
                    // Generate consolidated coverage report
                    sh 'mvn jacoco:report-aggregate'
                    
                    // Record coverage with strict thresholds
                    recordCoverage(
                        tools: [[
                            parser: 'JACOCO',
                            pattern: services.collect { "**/${it}/target/site/jacoco/jacoco.xml" }.join(',')
                        ]],
                        sourceFileResolver: [
                            [projectDir: "$WORKSPACE"],
                            [projectDir: "$WORKSPACE", subDir: "spring-petclinic-*"]
                        ],
                        healthyTarget: [
                            instructionCoverage: env.COVERAGE_THRESHOLD,
                            lineCoverage: env.COVERAGE_THRESHOLD,
                            branchCoverage: env.COVERAGE_THRESHOLD - 10
                        ],
                        unstableTarget: [
                            instructionCoverage: env.COVERAGE_THRESHOLD - 5,
                            lineCoverage: env.COVERAGE_THRESHOLD - 5,
                            branchCoverage: env.COVERAGE_THRESHOLD - 15
                        ],
                        failUnhealthy: true,
                        failUnstable: true
                    )
                    
                    // Generate visual report
                    publishHTML(
                        target: [
                            reportDir: 'target/site/jacoco-aggregate',
                            reportFiles: 'index.html',
                            reportName: 'Coverage Report',
                            keepAll: true
                        ]
                    )
                    
                    // Verify coverage programmatically
                    verifyCoverageThresholds(services)
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}

// Helper functions
def findServiceDir(String filePath) {
    def pathComponents = filePath.split('/')
    for (int i = pathComponents.size() - 1; i >= 0; i--) {
        def potentialPath = pathComponents[0..i].join('/')
        if (fileExists("${potentialPath}/pom.xml")) {
            return potentialPath
        }
    }
    return null
}

def getChangedFiles() {
    def changedFiles = []
    if (env.CHANGE_ID) {
        currentBuild.changeSets.each { changeSet ->
            changeSet.items.each { commit ->
                commit.affectedFiles.each { file ->
                    changedFiles.add(file.path)
                }
            }
        }
    } else {
        changedFiles = sh(
            script: 'git diff --name-only HEAD~1 HEAD',
            returnStdout: true
        ).split('\n')
    }
    return changedFiles
}

def getAllServices() {
    return [
        'spring-petclinic-discovery-server',
        'spring-petclinic-admin-server',
        'spring-petclinic-customers-service',
        'spring-petclinic-vets-service',
        'spring-petclinic-visits-service',
        'spring-petclinic-api-gateway'
    ]
}

def verifyCoverageThresholds(services) {
    def failedServices = []
    
    services.each { service ->
        def coverageFile = "${service}/target/site/jacoco/jacoco.csv"
        if (fileExists(coverageFile)) {
            def coverage = calculateCoverage(coverageFile)
            if (coverage < env.COVERAGE_THRESHOLD.toInteger()) {
                failedServices.add("${service} (${coverage}%)")
            }
        } else {
            error "Coverage report missing for ${service}"
        }
    }
    
    if (failedServices) {
        error "Coverage below ${env.COVERAGE_THRESHOLD}% for: ${failedServices.join(', ')}"
    }
}

def calculateCoverage(reportPath) {
    def report = readFile(reportPath)
    def (missed, covered) = report.split('\n').tail().collect {
        def cols = it.split(',')
        [cols[3].toInteger(), cols[4].toInteger()]
    }.transpose().collect { it.sum() }
    
    return (covered * 100 / (missed + covered)).round(2)
}
