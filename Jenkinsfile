post {
    always {
        // Process test results
        junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
        
        // Record coverage for multibranch view
        script {
            def coveragePattern = (env.CHANGED_SERVICES == 'all') ? 
                '**/target/site/jacoco/jacoco.xml' : 
                env.CHANGED_SERVICES.split(',').collect { 
                    "**/${it}/target/site/jacoco/jacoco.xml" 
                }.join(',')
            
            recordCoverage(
                tools: [[
                    parser: 'JACOCO',
                    pattern: coveragePattern
                ]],
                sourceFileResolver: [
                    [projectDir: "$WORKSPACE"],
                    [projectDir: "$WORKSPACE", subDir: "spring-petclinic-*"]
                ]
            )
        }
        
        // Publish HTML report for detailed viewing
        publishHTML(
            target: [
                reportDir: 'target/site/jacoco-aggregate',
                reportFiles: 'index.html',
                reportName: 'JaCoCo Coverage Report',
                keepAll: true
            ]
        )
        
        cleanWs()
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
