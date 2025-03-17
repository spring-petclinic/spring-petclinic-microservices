def CUSTOMERS_VETS_SERVICES = []
def GENAI_VISITS_SERVICES = []

pipeline {
    agent none
    stages {
        stage('Check Changes') {
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    def changes = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim().split("\n")

                    if (changes.any { it.startsWith("spring-petclinic-customers-service/") }) { CUSTOMERS_VETS_SERVICES.add('customers-service') }
                    if (changes.any { it.startsWith("spring-petclinic-vets-service/") }) { CUSTOMERS_VETS_SERVICES.add('vets-service') }
                    if (changes.any { it.startsWith("spring-petclinic-genai-service/") }) { GENAI_VISITS_SERVICES.add('genai-service') }
                    if (changes.any { it.startsWith("spring-petclinic-visits-service/") }) { GENAI_VISITS_SERVICES.add('visits-service') }
                }
            }
        }

        stage('Build if Customers & Vets are changed') {
            when {
                expression { return CUSTOMERS_VETS_SERVICES.size() > 0 }
            }
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    for (service in CUSTOMERS_VETS_SERVICES) {
                        echo "Building ${service}........"
                        sh "./mvnw clean package -f spring-petclinic-${service}"
                    }    
                }
            }
        }

        stage('Test if Customers & Vets are changed. Upload test results and testcase coverage') {
            when {
                expression { return CUSTOMERS_VETS_SERVICES.size() > 0 }
            }
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    for (service in CUSTOMERS_VETS_SERVICES) {
                        echo "Testing ${service}........"
                        sh "./mvnw test -f spring-petclinic-${service}"
                        junit "spring-petclinic-${service}/target/surefire-reports/*.xml"
                        jacoco execPattern: '**/target/jacoco.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java'
                    }    
                }
            }
        }

        stage('Build if GenAI & Visits are changed') {
            when {
                expression { return GENAI_VISITS_SERVICES.size() > 0 }
            }
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    for (service in GENAI_VISITS_SERVICES) {
                        echo "Building ${service}........"
                        sh "./mvnw clean package -f spring-petclinic-${service}"
                    }    
                }
            }
        }

        stage('Test if GenAI & Visits are changed. Upload test results and testcase coverage') {
            when {
                expression { return GENAI_VISITS_SERVICES.size() > 0 }
            }
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    for (service in GENAI_VISITS_SERVICES) {
                        echo "Testing ${service}........"
                        sh "./mvnw test -f spring-petclinic-${service}"
                        junit "spring-petclinic-${service}/target/surefire-reports/*.xml"
                        jacoco execPattern: '**/target/jacoco.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java'
                    }    
                }
            }
        }
    }
}