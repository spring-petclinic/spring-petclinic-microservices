pipeline {
    agent any
    
    tools {
        maven 'M3' // Make sure Maven is configured in Jenkins
        jdk 'jdk17' // Make sure JDK11 is configured in Jenkins
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                script {
                    // Determine which services changed
                    def changedFiles = getChangedFiles()
                    def servicesToBuild = getServicesToBuild(changedFiles)
                    
                    if (servicesToBuild.isEmpty()) {
                        servicesToBuild = ['all'] // Build all if no specific changes
                    }
                    
                    // Build each changed service or all
                    servicesToBuild.each { service ->
                        if (service == 'all' || service == 'discovery-server') {
                            sh 'mvn -pl spring-petclinic-discovery-server clean package'
                        }
                        if (service == 'all' || service == 'admin-server') {
                            sh 'mvn -pl spring-petclinic-admin-server clean package'
                        }
                        // Add other services similarly
                    }
                }
            }
        }
        
        stage('Test') {
            steps {
                script {
                    def changedFiles = getChangedFiles()
                    def servicesToTest = getServicesToBuild(changedFiles)
                    
                    if (servicesToTest.isEmpty()) {
                        servicesToTest = ['all']
                    }
                    
                    servicesToTest.each { service ->
                        if (service == 'all' || service == 'discovery-server') {
                            sh 'mvn -pl spring-petclinic-discovery-server test'
                            junit 'spring-petclinic-discovery-server/target/surefire-reports/*.xml'
                            jacoco execPattern: 'spring-petclinic-discovery-server/target/jacoco.exec'
                        }
                        // Add other services similarly
                    }
                }
            }
            post {
                always {
                    // Archive test results and coverage
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                    jacoco()
                }
            }
        }
    }
    
    post {
        failure {
            emailext body: '${DEFAULT_CONTENT}', recipientProviders: [[$class: 'DevelopersRecipientProvider']], subject: '${DEFAULT_SUBJECT}'
        }
    }
}

// Helper functions
def getChangedFiles() {
    def changeLogSets = currentBuild.changeSets
    def changedFiles = []
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            def files = new ArrayList(entry.affectedFiles)
            for (int k = 0; k < files.size(); k++) {
                changedFiles.add(files[k].path)
            }
        }
    }
    return changedFiles
}

def getServicesToBuild(changedFiles) {
    def services = []
    
    changedFiles.each { file ->
        if (file.startsWith('spring-petclinic-discovery-server/')) {
            if (!services.contains('discovery-server')) services.add('discovery-server')
        }
        // Add similar conditions for other services
    }
    
    return services
}
