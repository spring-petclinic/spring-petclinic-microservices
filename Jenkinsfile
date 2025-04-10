def changedServices = []

pipeline {
    agent any

    stages {
        stage('Detect Changed Services') {
            steps {
                script {
                    changedServices = getChangedServices()
                    echo "Changed services: ${changedServices}"
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    for (service in changedServices) {
                        echo "Testing ${service} ..."
                        sh "./mvnw clean verify -f ${service}/pom.xml"
                        junit "${service}/target/surefire-reports/*.xml"
                        jacoco (
                            execPattern: "${service}/target/jacoco.exec",
                            classPattern: "${service}/target/classes",
                            sourcePattern: "${service}/src/main/java",
                            exclusionPattern: "${service}/target/test-classes"
                        )
                        def coveragePercentage = getCoveragePercentage("${service}/target/site/jacoco/jacoco.csv")
                        if (coveragePercentage < 0.7) {
                            echo "Code coverage for ${service} is below 70: ${coveragePercentage * 100}"
                            error "Code coverage for ${service} is below 70: ${coveragePercentage * 100}"
                        } else {
                            echo "Code coverage for ${service} is ${coveragePercentage * 100}"
                        }
                        echo "${service} test completed."
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    for (service in changedServices) {
                        echo "Building ${service} ..."
                        sh "./mvnw clean install -f ${service}/pom.xml -DskipTests"
                        echo "${service} build completed."
                    }
                }
            }
        }
    }

    post {
        success {
            setBuildStatus("Build Successful", "SUCCESS")
        }

        failure {
            setBuildStatus("Build Failed", "FAILURE")
        }
    }


}

void setBuildStatus(String message, String state) {
    step([
        $class: "GitHubCommitStatusSetter",
        reposSource: [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/LeBaoHongHanh/spring-petclinic-microservices.git"],
        contextSource: [$class: "ManuallyEnteredCommitContextSource", context: "ci/jenkins/build-status"],
        errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
        statusResultSource: [$class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]]]
    ]);
}

String getChangedServices() {
    def changedServices = []
    def pattern = /^spring-petclinic-.*-service$/;

    for (changeLogSet in currentBuild.changeSets) {
        for (entry in changeLogSet.getItems()) { 
            for (file in entry.getAffectedFiles()) {
                def service = file.getPath().split("/")[0]
                if (service ==~ pattern) {
                    changedServices.add(service)
                }
            }
        }
    }
    return changedServices.unique()
}

double getCoveragePercentage(String filepath) {
    def fileContents = readFile(filepath)
    def totalMissed = 0
    def totalCovered = 0

    fileContents.split('\n').eachWithIndex { line, index ->
        if (index == 0) return 

        def columns = line.split(",")
        totalMissed += columns[3].toInteger() + columns[5].toInteger() + columns[7].toInteger() + columns[9].toInteger() + columns[11].toInteger()
        totalCovered += columns[4].toInteger() + columns[6].toInteger() + columns[8].toInteger() + columns[10].toInteger() + columns[12].toInteger()
    }

    return (totalCovered / (totalCovered + totalMissed))
}