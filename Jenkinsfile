#!/usr/bin/env groovy
import hudson.model.*

def getChangedServices() {
    def changedFiles = sh(returnStdout: true, script: "git diff --name-only HEAD~1").split()
    def services = []
    def serviceDirs = ["spring-petclinic-config-server", "spring-petclinic-discovery-server", "spring-petclinic-vets-service", "spring-petclinic-customers-service", "spring-petclinic-visits-service", "spring-petclinic-api-gateway", "spring-petclinic-genai-service", "spring-petclinic-admin-server"]
    for (file in changedFiles) {
        for (dir in serviceDirs) {
            if (file.startsWith(dir + "/")) {
                services.add(dir)
                break
            }
        }
    }
    return services.unique()
}

node {
    environment {
        GITHUB_TOKEN = credentials('github-token')
    }
    stage('Checkout') {
        checkout scm
    }

    def changedServices = getChangedServices()
    if (changedServices.isEmpty()) {
        echo "No changes in service directories."
        notifyGitHub("success", "ci/jenkins", "No changes detected, build skipped.")
        return
    }

    try {
        stage('Test') {
            for (service in changedServices) {
                dir(service) {
                    sh "mvn test"
                    sh "mvn jacoco:report"
                    
                    junit 'target/surefire-reports/*.xml'
                  
                    archiveArtifacts artifacts: 'target/surefire-reports/*.xml', allowEmptyArchive: true
                    archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                    
                    step([$class: 'JacocoPublisher', 
                          execPattern: 'target/jacoco.exec', 
                          classPattern: 'target/classes', 
                          sourcePattern: 'src/main/java', 
                          exclusionPattern: ''])
                }
            }
        }

        stage('Build') {
            for (service in changedServices) {
                dir(service) {
                    sh "mvn package -DskipTests"
                    sh "zip -r test-results.zip target/surefire-reports/"
                    archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
                }
            }
        }
}
