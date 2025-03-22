pipeline {
    agent {
        label 'development-server'
    }

    tools {
        maven '3.9.9'
    }

    environment {
        USERNAME = '22120207'
    }

    stages {
        stage('Check SCM') {
        }

        stage('Check Changed Files') {
            steps {
                script {
                    def targetBranch = env.CHANGE_TARGET ?: 'test'
                    def changedFiles = sh(script: "git diff --name-only origin/${targetBranch}", returnStdout: true).trim()

                    sh "echo ${changedFiles}"
                    
                    def changedFolders = changedFiles.split('\n')
                        .collect { it.split('/')[0] }
                        .unique()
                    
                    echo "Changed Folders:\n${changedFolders.join('\n')}"
                    
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
                    def folderToModule = [
                        "spring-petclinic-visits-service": "spring-petclinic-visits-service",
                        "spring-petclinic-customers-service": "spring-petclinic-customers-service",
                        "spring-petclinic-vets-service": "spring-petclinic-vets-service"
                    ]

                    def affectedModules = env.CHANGED_MODULES.split(',')
                        .findAll { folderToModule.containsKey(it) }
                        .collect { folderToModule[it] }

                    if (affectedModules) {
                        def testCommand = "mvn test -pl " + affectedModules.join(',')
                        echo "Running tests for affected modules: ${affectedModules.join(', ')}"
                        sh testCommand
                    } else {
                        echo "No relevant modules changed, skipping tests."
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
    
    post {
        always {
            publishHTML(
                target: [
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'spring-petclinic-visits-service/target/site/jacoco',
                    reportFiles: 'index.html',
                    reportName: 'JaCoCo Code Coverage'
                ]
            )
        }
    }
}
