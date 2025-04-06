pipeline {
    agent any

    stages {
        // stage('Test') {
        //     steps {
        //         echo 'Testing ...'
        //         sh '''
        //             cd spring-petclinic-visits-service
        //             mvn clean test -Djacoco.destFile=target/jacoco.exec
        //         '''
        //     }
        // }

        // stage('Building') {
        //     steps {
        //         echo 'Building ...'
        //         sh '''
        //             cd spring-petclinic-visits-service
        //             mvn clean install
        //         '''
        //     }
        // }
        stage('Test') {
            steps {
                echo 'Testing ...'
                // sh "mvn clean test"
                sh '''
                    ./mvnw clean test -f spring-petclinic-customers-service/pom.xml  
                '''
            }
        }
    }

    post {
        always {
            // junit '**/target/surefire-reports/*.xml'
            junit 'spring-petclinic-customers-service/target/surefire-reports/*.xml'

            // jacoco (
            //     execPattern: '**/target/jacoco.exec',
            //     classPattern: '**/target/classes',
            //     sourcePattern: '**/src/main/java',
            //     exclusionPattern: '**/target/test-classes'
            // )

            jacoco (
                execPattern: 'spring-petclinic-customers-service/target/jacoco.exec',
                classPattern: 'spring-petclinic-customers-service/target/classes',
                sourcePattern: 'spring-petclinic-customers-service/src/main/java',
                exclusionPattern: 'spring-petclinic-customers-service/target/test-classes'
            )

            updateGithubCommitStatus(currentBuild)
        }
    }


}

def getRepoURL() {
    sh "git config --get remote.origin.url > .git/remote-url"
    return readFile(".git/remote-url").trim()
}

def getCommitSha() {
    sh "git rev-parse HEAD > .git/current-commit"
    return readFile(".git/current-commit").trim()
}

def updateGithubCommitStatus(build) {
    repoUrl = getRepoURL()
    commitSha = getCommitSha()

    step([
        $class: 'GitHubCommitStatusSetter',
        reposSource: [$class: "ManuallyEnteredRepositorySource", url: repoUrl],
        commitShaSource: [$class: "ManuallyEnteredShaSource", sha: commitSha],
        errorHandlers: [[$class: 'ShallowAnyErrorHandler']],
        statusResultSource: [
        $class: 'ConditionalStatusResultSource',
        results: [
            [$class: 'BetterThanOrEqualBuildResult', result: 'SUCCESS', state: 'SUCCESS', message: build.description, statusName: 'CI/Success'],
            [$class: 'BetterThanOrEqualBuildResult', result: 'FAILURE', state: 'FAILURE', message: build.description, statusName: 'CI/Failure'],
            [$class: 'AnyBuildResult', state: 'FAILURE', message: 'Loophole', statusName: 'CI/Failure']
        ]
        ]
    ])
}

// Haha hoho