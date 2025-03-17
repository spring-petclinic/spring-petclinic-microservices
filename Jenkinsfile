// pipeline {
//     agent { label '!master' } 
//     stages {
//         stage('Build') { 
//             steps {
//                 // 
//             }
//         }
//         stage('Test') { 
//             steps {
//                 // 
//             }
//         }
//         stage('Deploy') { 
//             steps {
//                 // 
//             }
//         }
//     }
// }

pipeline {
    agent { label '!master' }

    environment {
        CUSTOMERS_GENAI_SERVICES = ''
        VETS_VISITS_SERVICES = ''
    }

    stages {
        stage('Check Changes') {
            agent { label '!master' }
            steps {
                script {
                    def changes = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim().split("\n")

                    def customers_vets = []
                    def genai_visits = []

                    if (changes.any { it.startsWith("spring-petclinic-customers-service/") }) { customers_vets.add('customers-service') }
                    if (changes.any { it.startsWith("spring-petclinic-vets-service/") }) { customers_vets.add('vets-service') }
                    if (changes.any { it.startsWith("spring-petclinic-genai-service/") }) { genai_visits.add('genai-service') }
                    if (changes.any { it.startsWith("spring-petclinic-visits-service/") }) { genai_visits.add('visits-service') }
                    
                    CUSTOMERS_VETS_SERVICES = customers_vets.join(",")
                    GENAI_VISITS_SERVICES = genai_visits.join(",")
                }
            }
        }

        stage('Build if Customers & GenAI are changed') {
            when {
                expression { return env.CUSTOMERS_VETS_SERVICES != '' }
            }
            agent { label '!master' }
            steps {
                script {
                    def services = env.CUSTOMERS_VETS_SERVICES.split(",")

                    for (service in services) {
                        echo "Building ${service}........"
                        sh "./mvnw install -f spring-petclinic-${service}"
                        junit "spring-petclinic-${service}/target/surefire-reports/*.xml"
                    }    
                }
            }
        }

        // stage('Test if Customers & GenAI are changed') {
        //     when {
        //         expression { return env.CUSTOMERS_GENAI_SERVICES != '' }
        //     }
        //     agent { label '!master' }
        //     steps {
        //         script {
        //             def services = env.CUSTOMERS_GENAI_SERVICES.split(",")

        //             for (service in services) {
        //                 echo "Running tests for ${service}"
        //                 dir("${service}") {
        //                     sh "./mvnw test"
        //                     junit '**/target/surefire-reports/*.xml'
        //                     jacoco execPattern: '**/target/jacoco.exec',
        //                            classPattern: '**/target/classes',
        //                            sourcePattern: '**/src/main/java'
        //                 }
        //             }
        //         }
        //     }
        // }

        // stage('Test if Vets & Visits are changed') {
        //     when {
        //         expression { return env.VETS_VISITS_SERVICES != '' }
        //     }
        //     agent { label '!master' }
        //     steps {
        //         script {
        //             def services = env.VETS_VISITS_SERVICES.split(",")

        //             for (service in services) {
        //                 echo "Running tests for ${service}"
        //                 dir("${service}") {
        //                     sh "./mvnw test"
        //                     junit '**/target/surefire-reports/*.xml'
        //                     jacoco execPattern: '**/target/jacoco.exec',
        //                            classPattern: '**/target/classes',
        //                            sourcePattern: '**/src/main/java'
        //                 }
        //             }
        //         }
        //     }
        // }

        

        // stage('Build if Vets & Visits are changed') {
        //     when {
        //         expression { return env.VETS_VISITS_SERVICES != '' }
        //     }
        //     agent { label '!master' }
        //     steps {
        //         script {
        //             def services = env.VETS_VISITS_SERVICES.split(",")

        //             for (service in services) {
        //                 echo "Building ${service}"
        //                 dir("${service}") {
        //                     sh "./mvnw clean package"
        //                 }
        //             }
        //         }
        //     }
        // }
    }
}