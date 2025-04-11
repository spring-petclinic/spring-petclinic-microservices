pipeline {
    agent any

    tools {
        maven 'M3'
        jdk 'jdk17'
    }

    environment {
        COVERAGE_THRESHOLD = 70 // Strict 70% threshold
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout & Initialize') {
            steps {
                checkout scm
                sh 'mvn --version'
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    def changedServices = getChangedServices()
                    
                    if (changedServices.isEmpty()) {
                        echo "Building all services"
                        sh 'mvn clean package'
                    } else {
                        changedServices.each { service ->
                            echo "Building and testing ${service}"
                            sh "mvn -pl spring-petclinic-${service} clean package"
                        }
                    }
                }
            }
        }

        stage('Coverage Analysis') {
            steps {
                script {
                    def changedServices = getChangedServices()
                    def servicesToCheck = changedServices.isEmpty() ? getAllServices() : changedServices
                    
                    // Generate enhanced HTML reports
                    sh 'mvn jacoco:report -Djacoco.destFile=aggregate.exec'
                    
                    // Process coverage for each service
                    servicesToCheck.each { service ->
                        def coverage = verifyCoverage(service)
                        generateCoverageBadge(service, coverage)
                    }
                    
                    // Generate aggregated report
                    generateAggregateReport(servicesToCheck)
                }
            }
        }
    }

    post {
        always {
            // Publish consolidated test results
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
            
            // Archive HTML reports with better visualization
            publishHTML target: [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/site/jacoco-aggregate',
                reportFiles: 'index.html',
                reportName: 'JaCoCo Coverage Report'
            ]
            
            // Publish coverage badges
            archiveArtifacts artifacts: 'coverage-badges/*.svg', allowEmptyArchive: true
        }
        
        success {
            slackSend(color: 'good', 
                     message: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}\n" +
                     "Coverage: ${getCoverageSummary()}\n" +
                     "Details: ${env.BUILD_URL}testReport/")
        }
        
        unstable {
            slackSend(color: 'warning',
                     message: "UNSTABLE: ${env.JOB_NAME} #${env.BUILD_NUMBER}\n" +
                     "Low Coverage: ${getCoverageSummary()}\n" +
                     "Details: ${env.BUILD_URL}jacoco/")
        }
    }
}

// ============= Enhanced Coverage Functions =============

def getChangedServices() {
    def changes = []
    if (env.CHANGE_ID) {
        def changeLogSets = currentBuild.changeSets
        changes = changeLogSets.collectMany { it.items.collectMany { it.affectedFiles.collect { it.path } } }
    } else {
        changes = sh(script: 'git diff --name-only HEAD~1 HEAD', returnStdout: true).split('\n')
    }
    
    def serviceMap = [
        'spring-petclinic-discovery-server': 'discovery-server',
        'spring-petclinic-admin-server': 'admin-server',
        // Add all other services...
    ]
    
    def services = changes.collect { file ->
        serviceMap.find { dir, _ -> file.startsWith(dir) }?.value
    }.findAll().unique()
    
    return changes.any { it.contains('pom.xml') || it.contains('Jenkinsfile') } ? [] : services
}

def verifyCoverage(service) {
    def reportFile = "spring-petclinic-${service}/target/site/jacoco/jacoco.csv"
    if (!fileExists(reportFile)) {
        error "No coverage report found for ${service}"
    }
    
    def report = readFile(reportFile)
    def (missed, covered) = report.split('\n').tail().collect {
        def cols = it.split(',')
        [cols[3].toInteger(), cols[4].toInteger()]
    }.transpose().collect { it.sum() }
    
    def coverage = (covered * 100) / (missed + covered)
    coverage = coverage.round(2)
    
    if (coverage < env.COVERAGE_THRESHOLD.toInteger()) {
        unstable("${service} coverage ${coverage}% < ${env.COVERAGE_THRESHOLD}% threshold")
    }
    
    return coverage
}

def generateCoverageBadge(service, coverage) {
    def color = coverage >= env.COVERAGE_THRESHOLD.toInteger() ? 'brightgreen' : 'red'
    def badge = """
    <svg xmlns="http://www.w3.org/2000/svg" width="120" height="20">
        <linearGradient id="b" x2="0" y2="100%">
            <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
            <stop offset="1" stop-opacity=".1"/>
        </linearGradient>
        <mask id="a">
            <rect width="120" height="20" rx="3" fill="#fff"/>
        </mask>
        <g mask="url(#a)">
            <rect width="80" height="20" fill="#555"/>
            <rect x="80" width="40" height="20" fill="#${color}"/>
            <rect width="120" height="20" fill="url(#b)"/>
        </g>
        <text x="40" y="14" fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,sans-serif" font-size="11">${service}</text>
        <text x="100" y="14" fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,sans-serif" font-size="11">${coverage}%</text>
    </svg>
    """
    
    sh "mkdir -p coverage-badges"
    writeFile file: "coverage-badges/${service}-coverage.svg", text: badge
}

def generateAggregateReport(services) {
    def totalMissed = 0
    def totalCovered = 0
    
    services.each { service ->
        def report = readFile("spring-petclinic-${service}/target/site/jacoco/jacoco.csv")
        def (missed, covered) = report.split('\n').tail().collect {
            def cols = it.split(',')
            [cols[3].toInteger(), cols[4].toInteger()]
        }.transpose().collect { it.sum() }
        
        totalMissed += missed
        totalCovered += covered
    }
    
    def aggregateCoverage = (totalCovered * 100) / (totalMissed + totalCovered)
    generateCoverageBadge('aggregate', aggregateCoverage.round(2))
}

def getCoverageSummary() {
    def badges = findFiles(glob: 'coverage-badges/*.svg')
    return badges.collect { badge ->
        def service = badge.name.replace('-coverage.svg', '')
        def coverage = readFile(badge.path).split('>')[9].split('<')[0].replace('%', '')
        "${service}: ${coverage}%"
    }.join(', ')
}

def getAllServices() {
    return [
        'discovery-server',
        'admin-server',
        'customers-service',
        'vets-service',
        'visits-service',
        'api-gateway'
    ]
}
