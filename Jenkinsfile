pipeline {
    agent {
        label 'development-server'
    }

    options {
        // Clean before build
        skipDefaultCheckout(true)
    }

    tools {
        maven '3.9.9'
    }

    environment {
        USERNAME = "22120207"
    }

    stages {
        stage('Check SCM') {
            steps {
                cleanWs()

                checkout scm

                script {
                    // Get the first 7 characters of the SHA Git Commit
                    def gitCommitHash = sh(script: "git describe --always", returnStdout: true).trim()
                    env.COMMIT_HASH = gitCommitHash
                }
            }
        }

        stage('Check Changed Files') {
            steps {
                script {
                    def branch_name = ""

                    if (env.CHANGE_ID) {
                        branch_name = "${env.CHANGE_TARGET}"
                    }
                    else {
                        branch_name = 'HEAD~1'
                    }

                    def changedFiles = sh(script: "git diff --name-only ${branch_name}", returnStdout: true).trim()

                    echo "${changedFiles}"

                    def folderList = ['spring-petclinic-customers-service', 'spring-petclinic-vets-service', 'spring-petclinic-visits-service']
                    
                    def changedFolders = changedFiles.split('\n')
                        .collect { it.split('/')[0] }
                        .unique()
                        .findAll { folderList.contains(it) }
                    
                    echo "Changed Folders: \n${changedFolders.join('\n')}"
                    
                    env.CHANGED_MODULES = changedFolders.join(',')
                }
            }
        }  

        stage('Run Unit Test') {
            when {
                expression { env.CHANGED_MODULES?.trim() }
            }
            steps {
                script {
                    def modules = env.CHANGED_MODULES ? env.CHANGED_MODULES.split(',') : []

                    for (module in modules) {
                        def testCommand = "mvn test -pl ${module}"
                        echo "Running tests for affected modules: ${module}"
                        sh "${testCommand}"

                        // Generate JaCoCo HTML Report
                        jacoco classPattern: "**/${module}/target/classes", 
                            execPattern: "**/${module}/target/coverage-reports/jacoco.exec",
                            runAlways: true, 
                            sourcePattern: "**/${module}/src/main/java"

                        // Pushish HTML Artifact of Code Coverage Report
                        publishHTML(
                            target: [
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: "${module}/target/site/jacoco",
                                reportFiles: 'index.html',
                                reportName: "${module}_code_coverage_report_${env.COMMIT_HASH}_v${env.BUILD_ID}"
                            ]
                        )

                        // Get Code Coverage
                        def codeCoverages = []
                        def coverageReport = readFile(file: "${WORKSPACE}/${module}/target/site/jacoco/index.html")
                        def matcher = coverageReport =~ /<tfoot>(.*?)<\/tfoot>/
                        if (matcher.find()) {
                            def coverage = matcher[0]
                            def instructionMatcher = coverage =~ /<td class="ctr2">(.*?)%<\/td>/
                            if (instructionMatcher.find()) {
                                def coveragePercentage = instructionMatcher[0][1]
                                echo "Overall code coverage of ${module}: ${coveragePercentage}%"
                                
                                codeCoverages.add(coveragePercentage)
                            }
                        }

                        env.CODE_COVERAGES = codeCoverages.join(',')
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    boolean testSuccess = true

                    def reports = env.CODE_COVERAGES ? env.CODE_COVERAGES.split(',') : []

                    if (env.CHANGE_ID && env.CHANGE_TARGET == 'main') {
                        for (codeCoverage in reports) {
                            if (codeCoverage.toDouble() < 70) {
                                testSuccess = false

                                publishChecks(
                                    name: 'Test Code Coverage',
                                    title: 'Code Coverage Check Failed',
                                    summary: "Coverage must be at least 70%. Your coverage for one module is ${codeCoverage}%.",
                                    text: 'Increase test coverage and retry the build.',
                                    detailsURL: env.BUILD_URL,
                                    conclusion: 'FAILURE'
                                )

                                break
                            }
                        }

                        publishChecks(
                            name: 'Test Code Coverage',
                            title: 'Code Coverage Check Success!',
                            summary: 'All test code coverage is greater than 70%',
                            text: 'Check Success!',
                            detailsURL: env.BUILD_URL,
                            conclusion: 'SUCCESS'
                        )
                    }
                    
                    def modules = env.CHANGED_MODULES ? env.CHANGED_MODULES.split(',') : []
                    if (testSuccess && modules.size() > 0) {
                        
                        for (module in modules) {
                            def buildCommand = "mvn -pl ${module} -am clean install"
                            echo "Build for affected modules: ${module}"
                            sh "${buildCommand}"
                        }

                        try {
                            archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true, allowEmptyArchive: false
                        }
                        catch (Exception e) {
                            echo "No artifacts found to archive. Skipping artifact archival."
                        }
                    }
                }
            }
        }
    }
}