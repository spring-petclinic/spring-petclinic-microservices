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
                    // Run with JaCoCo agent and generate reports
                    sh 'mvn clean org.jacoco:jacoco-maven-plugin:0.8.8:prepare-agent verify'
                }
            }
        }
        
        stage('Coverage Verification') {
            steps {
                script {
                    // Parse coverage report and enforce 70% minimum
                    def coverageFile = 'target/site/jacoco/jacoco.csv'
                    
                    if (!fileExists(coverageFile)) {
                        error "JaCoCo coverage report not found at ${coverageFile}"
                    }
                    
                    def coverage = calculateCoverage(coverageFile)
                    echo "Current test coverage: ${coverage}%"
                    
                    if (coverage < 70) {
                        error "Test coverage ${coverage}% is below required 70% threshold"
                    }
                }
            }
        }
        
        stage('Coverage Reporting') {
            steps {
                script {
                    // Record coverage for Jenkins UI
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
                    
                    // Archive reports
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
        failure {
            emailext body: '${DEFAULT_CONTENT}',
                     subject: 'Build Failed: ${JOB_NAME} #${BUILD_NUMBER}',
                     to: 'dev-team@yourcompany.com'
        }
    }
}

// Helper function to calculate coverage percentage
def calculateCoverage(reportPath) {
    def report = readFile(reportPath)
    def lines = report.split('\n')
    
    // Skip header line
    def instructionMissed = 0
    def instructionCovered = 0
    
    for (int i = 1; i < lines.size(); i++) {
        def cols = lines[i].split(',')
        instructionMissed += cols[3].toInteger()  // INSTRUCTION_MISSED
        instructionCovered += cols[4].toInteger() // INSTRUCTION_COVERED
    }
    
    def total = instructionMissed + instructionCovered
    return total > 0 ? (instructionCovered * 100 / total).round(2) : 0
}
