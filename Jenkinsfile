pipeline {
    agent any
    
    environment {
        SERVICES = ''
    }
    
    stages {
        stage('Check Changes') {
            steps {
                script {
                    def changedFiles = sh(script: "git diff --name-only origin/main", returnStdout: true).trim().split("\n")
                    def services = ['spring-petclinic-customers-service', 'spring-petclinic-vets-service', 'spring-petclinic-visits-service']
                
                    echo "Changed files: ${changedFiles}"
                
                    if (changedFiles.size() == 0 || changedFiles[0] == '') {
                        echo "No changes detected. Skipping pipeline."
                        currentBuild.result = 'ABORTED'
                        return
                    }
                
                    def detectedServices = services.findAll { service -> 
                        changedFiles.any { it.startsWith(service + '/') }
                    }
                
                    if (detectedServices.isEmpty()) {
                        echo "No relevant service changes detected. Skipping pipeline."
                        currentBuild.result = 'ABORTED'
                        return
                    }
                
                    env.SERVICES = detectedServices.join(",")
                    echo "Detected services: ${env.SERVICES}"
                }
            }
        }
        
        stage('Create Branch & Add Tests') {
            when {
                expression { return env.SERVICES != '' }
            }
            steps {
                script {
                    def services = env.SERVICES.split(",")
                    for (service in services) {
                        def branchName = "add-tests-${service}-${env.BUILD_ID}"
                        echo "Creating branch: ${branchName}"
                        
                        sh """
                            git checkout -b ${branchName}
                            echo "// New test case" >> ${service}/src/test/java/NewTest.java
                            git add ${service}/src/test/java/NewTest.java
                            git commit -m "Add unit test for ${service}"
                            git push origin ${branchName}
                        """
                    }
                }
            }
        }
        
        stage('Run Unit Tests') {
            when {
                expression { return env.SERVICES != '' }
            }
            steps {
                script {
                    def services = env.SERVICES.split(",")
                    for (service in services) {
                        echo "Running unit tests for: ${service}"
                        sh "./mvnw test -pl ${service} -am"
                    }
                }
            }
            post {
                always {
                    junit "**/target/surefire-reports/*.xml"
                }
            }
        }
    }
    
    post {
        success {
            echo "All tests passed successfully!"
        }
        failure {
            echo "Some tests failed."
        }
    }
}
