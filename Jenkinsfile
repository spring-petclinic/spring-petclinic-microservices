def CUSTOMERS_SERVICES = []
def GENAI_SERVICES = []
def VETS_SERVICES = []
def VISITS_SERVICES = []

pipeline {
    agent none
    stages {
        stage('Check Changes') {
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    def changes = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim().split("\n")

                    if (changes.any { it.startsWith("spring-petclinic-customers-service/") }) { CUSTOMERS_SERVICES.add('customers-service') }
                    if (changes.any { it.startsWith("spring-petclinic-vets-service/") }) { VETS_SERVICES.add('vets-service') }
                    if (changes.any { it.startsWith("spring-petclinic-genai-service/") }) { GENAI_SERVICES.add('genai-service') }
                    if (changes.any { it.startsWith("spring-petclinic-visits-service/") }) { VISITS_SERVICES.add('visits-service') }
                }
            }
        }

        stage('Build and Test Customers') {
            when {
                expression { return CUSTOMERS_SERVICES.size() > 0 }
            }
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    echo "Building ${CUSTOMERS_SERVICES[0]}........"
                    sh "./mvnw clean package -f spring-petclinic-${CUSTOMERS_SERVICES[0]}"
                    echo "Testing ${CUSTOMERS_SERVICES[0]}........"
                    sh "./mvnw test -f spring-petclinic-${CUSTOMERS_SERVICES[0]}"
                    junit "spring-petclinic-${CUSTOMERS_SERVICES[0]}/target/surefire-reports/*.xml"
                    jacoco execPattern: '**/target/jacoco.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java'
                }
            }
        }

        stage('Build and Test Vets') {
            when {
                expression { return VETS_SERVICES.size() > 0 }
            }
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    echo "Building ${VETS_SERVICES[0]}........"
                    sh "./mvnw clean package -f spring-petclinic-${VETS_SERVICES[0]}"
                    echo "Testing ${VETS_SERVICES[0]}........"
                    sh "./mvnw test -f spring-petclinic-${VETS_SERVICES[0]}"
                    junit "spring-petclinic-${VETS_SERVICES[0]}/target/surefire-reports/*.xml"
                    jacoco execPattern: '**/target/jacoco.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java'
                }
            }
        }

        stage('Build and Test Visits') {
            when {
                expression { return VISITS_SERVICES.size() > 0 }
            }
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    echo "Building ${VISITS_SERVICES[0]}........"
                    sh "./mvnw clean package -f spring-petclinic-${VISITS_SERVICES[0]}"
                    echo "Testing ${VISITS_SERVICES[0]}........"
                    sh "./mvnw test -f spring-petclinic-${VISITS_SERVICES[0]}"
                    junit "spring-petclinic-${VISITS_SERVICES[0]}/target/surefire-reports/*.xml"
                    jacoco execPattern: '**/target/jacoco.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java'
                }
            }
        }

        stage('Build and Test GenAI') {
            when {
                expression { return GENAI_SERVICES.size() > 0 }
            }
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    echo "Building ${GENAI_SERVICES[0]}........"
                    sh "./mvnw clean package -f spring-petclinic-${GENAI_SERVICES[0]}"
                    echo "Testing ${GENAI_SERVICES[0]}........"
                    sh "./mvnw test -f spring-petclinic-${GENAI_SERVICES[0]}"
                    junit "spring-petclinic-${GENAI_SERVICES[0]}/target/surefire-reports/*.xml"
                    jacoco execPattern: '**/target/jacoco.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java'
                }
            }
        }
    }
}