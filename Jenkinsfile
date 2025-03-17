SERVICES_CHANGED = []

pipeline {
    agent none
    stages {
        stage('Check Changes') {
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    def changes = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim().split("\n")
            
                    if (changes.any { it.startsWith("customers-service/") }) { SERVICES_CHANGED.add('customers-service') }
                    if (changes.any { it.startsWith("genai-service/") }) { SERVICES_CHANGED.add('genai-service') }
                    if (changes.any { it.startsWith("vets-service/") }) { SERVICES_CHANGED.add('vets-service') }
                    if (changes.any { it.startsWith("visits-service/") }) { SERVICES_CHANGED.add('visits-service') }
                }
            }
        }

        stage('Build') {
            when {
                expression { return SERVICES_CHANGED.size() > 0}
            }
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    for (service in SERVICES_CHANGED) {
                        echo "Building ${service}....."
                        sh "./mvnw clean package -f spring-petclinic-${service}"
                    }
                }
            }
        }

        stage('Test') {
            when {
                expression { return SERVICES_CHANGED.size() > 0}
            }
            agent { label 'ptb-agent || nnh-agent' }
            steps {
                script {
                    for (service in SERVICES_CHANGED) {
                        echo "Testing ${service}........"
                        sh "./mvnw test -f spring-petclinic-${service}"
                        if (${service} != 'genai-service') {
                            junit "spring-petclinic-${service}/target/surefire-reports/*.xml"
                        }
                        jacoco execPattern: '**/target/jacoco.exec', classPattern: '**/target/classes', sourcePattern: '**/src/main/java'
                    }
                }
            }
        }
    }
}