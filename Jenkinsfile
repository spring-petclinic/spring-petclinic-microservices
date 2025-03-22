pipeline {
    agent {
        label 'development-server'
    }

    tools {
        maven '3.9.9'
    }

    environment {
        HASH_VERSION = ""
    }

    stages {

        stage('Check Changed Files') {
            steps {
                script {
                    def GIT_COMMIT_HASH = env.GIT_COMMIT.substring(0,8)
                    evn.HASH_VERSION = GIT_COMMIT_HASH

                    def changedFiles = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()
                    def folderList = ['spring-petclinic-customers-service', 'spring-petclinic-discovery-server', 'spring-petclinic-visits-service']
                    
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
                    def modules = env.CHANGED_MODULES.split(',')

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
                                reportName: "${module}_code_coverage_report_${env.HASH_VERSION}"
                            ]
                        )
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    sh 'echo "Building..."'
                }
            }
        }
    }
}
