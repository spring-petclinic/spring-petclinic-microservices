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
                    // Run with JaCoCo agent and generate all reports
                    sh 'mvn clean verify'
                }
            }
        }
        
        stage('Coverage Verification') {
            steps {
                script {
                    // Find the CSV report wherever it was generated
                    def csvReport = findFiles(glob: '**/site/jacoco/jacoco.csv').first()?.path
                    
                    if (!csvReport) {
                        error "JaCoCo CSV report not found. Searched in: ${findFiles(glob: '**/site/jacoco/').collect{it.path}}"
                    }
                    
                    // Calculate and verify coverage
                    def coverage = calculateCoverage(csvReport)
                    echo "Current test coverage: ${coverage}%"
                    
                    if (coverage < 70) {
                        unstable "Test coverage ${coverage}% is below required 70% threshold"
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
                            pattern: '**/site/jacoco/jacoco.xml'
                        ]],
                        sourceFileResolver: [
                            [projectDir: "$WORKSPACE"],
                            [projectDir: "$WORKSPACE", subDir: "**/src/main/java"]
                        ]
                    )
                    
                    // Archive reports
                    archiveArtifacts artifacts: '**/site/jacoco/**,**/target/jacoco.exec'
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
