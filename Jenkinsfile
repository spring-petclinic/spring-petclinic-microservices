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
        PROJECT_NAME = "spring-petclinic-microservices"
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
                        branch_name = "origin/${env.CHANGE_TARGET}"
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

                        def minCoverage = reports.collect { it.toDouble() }.min()

                        if (minCoverage < 70) {
                            testSuccess = false
                            publishGitHubCheck(
                                'Test Code Coverage',
                                'Code Coverage Check Failed',
                                "Coverage must be at least 70%. Your lowest coverage is ${minCoverage}%.",
                                'Increase test coverage and retry the build.',
                                'failure'
                            )
                        }
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

                    if (!testSuccess) {
                        publishGitHubCheck(
                            'Test Code Coverage',
                            'Code Coverage Check Failed',
                            "Coverage must be at least 70%. Your coverage for one module is ${codeCoverage}%.",
                            'Increase test coverage and retry the build.',
                            'failure'
                        )
                    } 
                    else {
                        publishGitHubCheck(
                            'Test Code Coverage',
                            'Code Coverage Check Success!',
                            'All test code coverage is greater than 70%',
                            'Check Success!',
                            'success'
                        )
                    }
                }
            }
        }
    }
}

def publishGitHubCheck(String name, String title, String summary, String text, String conclusion) {
    withCredentials([usernamePassword(credentialsId: 'GITHUB-JENKINS-TOKEN', usernameVariable: 'GITHUB_USER', passwordVariable: 'GITHUB_TOKEN')]) {
        def commitSHA = env.COMMIT_HASH
        def repoOwner = env.GITHUB_USER
        def repoName = env.PROJECT_NAME
        def githubApiUrl = "https://api.github.com/repos/${repoOwner}/${repoName}/check-runs"

        sh(
            'curl -X POST -u $GITHUB_USER:$GITHUB_TOKEN ' +
            '-H "Accept: application/vnd.github.v3+json" ' +
            '-d \'{"name": "' + name + '", ' +
            '"head_sha": "' + commitSHA + '", ' +
            '"status": "completed", ' +
            '"conclusion": "' + conclusion + '", ' +
            '"details_url": "' + env.BUILD_URL + '", ' +
            '"output": {"title": "' + title + '", ' +
            '"summary": "' + summary + '", ' +
            '"text": "' + text + '"}}\' ' +
            githubApiUrl
        )
    }
}
