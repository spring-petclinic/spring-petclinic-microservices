pipeline {
    agent any
    
    tools {
        maven 'M3' // Cần cấu hình Maven trong Global Tool Configuration
        jdk 'jdk17' // Cần cấu hình JDK trong Global Tool Configuration
    }
    
    stages {
        stage('Checkout & Detect Changes') {
            steps {
                checkout scm
                script {
                    // Xác định service bị thay đổi
                    def changes = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()
                    env.CHANGED_SERVICES = getChangedServices(changes)
                }
            }
        }
        
        stage('Build Changed Services') {
            when {
                expression { return env.CHANGED_SERVICES != 'all' && env.CHANGED_SERVICES != '' }
            }
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(',')
                    services.each { service ->
                        echo "Building ${service}"
                        sh "./mvnw clean package -DskipTests -pl :${service} -am"
                    }
                }
            }
        }
        
        stage('Build All') {
            when {
                expression { return env.CHANGED_SERVICES == 'all' }
            }
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }
        
        stage('Test Changed Services') {
            when {
                expression { return env.CHANGED_SERVICES != 'all' && env.CHANGED_SERVICES != '' }
            }
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(',')
                    services.each { service ->
                        echo "Testing ${service}"
                        sh "./mvnw test -pl :${service}"
                        junit "**/${service}/target/surefire-reports/*.xml"
                        jacoco execPattern: "**/${service}/target/jacoco.exec"
                    }
                }
            }
        }
        
        stage('Test All') {
            when {
                expression { return env.CHANGED_SERVICES == 'all' }
            }
            steps {
                sh './mvnw test'
                junit '**/target/surefire-reports/*.xml'
                jacoco execPattern: '**/target/jacoco.exec'
            }
        }
    }
    
    post {
        always {
            // Xuất báo cáo JaCoCo
            jacoco exclusionPattern: '**/target/classes/**',
            sourceExclusionPattern: '**/src/test/**',
            sourceInclusionPattern: '**/src/main/**'
            
            // Dọn dẹp workspace
            cleanWs()
        }
    }
}

def getChangedServices(String changes) {
    if (changes.isEmpty()) {
        return 'all'
    }
    
    def serviceMap = [
        // 'spring-petclinic-api-gateway': 'api-gateway',
        // 'spring-petclinic-customers-service': 'customers-service',
        // 'spring-petclinic-vets-service': 'vets-service',
        // 'spring-petclinic-visits-service': 'visits-service',
        // 'spring-petclinic-config-server': 'config-server',
        // 'spring-petclinic-discovery-server': 'discovery-server',
        // 'spring-petclinic-admin-server': 'admin-server'
    ]
    
    def changedServices = []
    serviceMap.each { dir, service ->
        if (changes.contains(dir)) {
            changedServices.add(service)
        }
    }
    
    return changedServices.isEmpty() ? 'all' : changedServices.join(',')
}
