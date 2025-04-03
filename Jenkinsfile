pipeline {
    agent any

    options {
        buildDiscarder(logRotator(
            numToKeepStr: '10',      // Giữ logs của 10 builds
            artifactNumToKeepStr: '5' // Chỉ giữ artifacts của 5 builds gần nhất
        ))
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    sh 'pwd'

                    def changedFiles = sh(script: 'git diff --name-only HEAD~1 HEAD', returnStdout: true).trim()
                    echo "Changed files: ${changedFiles}"

                    def changedServices = []

                    if (changedFiles.contains('spring-petclinic-genai-service')) {
                        changedServices.add('genai')
                    }
                    if (changedFiles.contains('spring-petclinic-customers-service')) {
                        changedServices.add('customers')
                    }
                    if (changedFiles.contains('spring-petclinic-vets-service')) {
                        changedServices.add('vets')
                    }
                    if (changedFiles.contains('spring-petclinic-visits-service')) {
                        changedServices.add('visits')
                    }
                    if (changedFiles.contains('spring-petclinic-api-gateway')) {
                        changedServices.add('api-gateway')
                    }
                    if (changedFiles.contains('spring-petclinic-discovery-server')) {
                        changedServices.add('discovery')
                    }
                    if (changedFiles.contains('spring-petclinic-config-server')) {
                        changedServices.add('config')
                    }
                    if (changedFiles.contains('spring-petclinic-admin-server')) {
                        changedServices.add('admin')
                    }

                    if (changedServices.isEmpty()) {
                        changedServices = ['all']
                    }

                    echo "Detected changes in services: ${changedServices}"

                    CHANGED_SERVICES_LIST = changedServices
                    CHANGED_SERVICES_STRING = changedServices.join(',')
                    echo "Changed services: ${CHANGED_SERVICES_STRING}"
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if (CHANGED_SERVICES_LIST.contains('all')) {
                        echo 'Testing all modules'
                        sh './mvnw clean test'
                        sh 'find . -name "surefire-reports" -type d'
                        sh 'find . -name "jacoco.exec" -type f'
                    } else {
                        def modules = CHANGED_SERVICES_LIST.collect { "spring-petclinic-${it}-service" }.join(',')
                        echo "Testing modules: ${modules}"
                        sh "./mvnw clean test -pl ${modules}"
                        sh 'find . -name "surefire-reports" -type d'
                        sh 'find . -name "jacoco.exec" -type f'
                    }
                }
            }
            post {
                always {
                    script {
                        def testReportPattern = ''
                        def jacocoPattern = ''

                        if (CHANGED_SERVICES_LIST.contains('all')) {
                            testReportPattern = '**/surefire-reports/TEST-*.xml'
                            jacocoPattern = '**/jacoco.exec'
                        } else {
                            def patterns = CHANGED_SERVICES_LIST.collect {
                                "spring-petclinic-${it}-service/target/surefire-reports/TEST-*.xml"
                            }.join(',')
                            testReportPattern = patterns

                            def jacocoPatterns = CHANGED_SERVICES_LIST.collect {
                                "spring-petclinic-${it}-service/target/jacoco.exec"
                            }.join(',')
                            jacocoPattern = jacocoPatterns
                        }

                        echo "Looking for test reports with pattern: ${testReportPattern}"
                        sh "find . -name 'TEST-*.xml' -type f"

                        def testFiles = sh(script: "find . -name 'TEST-*.xml' -type f", returnStdout: true).trim()
                        if (testFiles) {
                            echo "Found test reports: ${testFiles}"
                            junit testReportPattern
                        } else {
                            echo 'No test reports found, likely no tests were executed.'
                        }

                        echo "Looking for JaCoCo data with pattern: ${jacocoPattern}"
                        sh "find . -name 'jacoco.exec' -type f"

                        def jacocoFiles = sh(script: "find . -name 'jacoco.exec' -type f", returnStdout: true).trim()
                        if (jacocoFiles) {
                            echo "Found JaCoCo files: ${jacocoFiles}"
                            def coveragePass = true
                            def coverageResults = [:]
                            
                            // Analyze coverage for each changed service
                            CHANGED_SERVICES_LIST.each { service ->
                                if (service in ['customers', 'vets', 'visits']) {
                                    def modulePath = "spring-petclinic-${service}-service"
                                    def jacocoExec = "${modulePath}/target/jacoco.exec"
                                    def classesDir = "${modulePath}/target/classes"
                                    def sourceDir = "${modulePath}/src/main/java"
                                    
                                    // Parse JaCoCo report
                                    def coverage = sh(script: """
                                        java -jar jacococli.jar report ${jacocoExec} \
                                          --classfiles ${classesDir} \
                                          --sourcefiles ${sourceDir} \
                                          --xml ${modulePath}/target/coverage-report.xml
                                        grep -oP '(?<=<counter type="INSTRUCTION" missed=")[^"]*(?=")' ${modulePath}/target/coverage-report.xml | head -1
                                        grep -oP '(?<=<counter type="INSTRUCTION" covered=")[^"]*(?=")' ${modulePath}/target/coverage-report.xml | head -1
                                    """, returnStdout: true).trim().split('\n')
                                    
                                    if (coverage.size() >= 2) {
                                        def missed = coverage[0].toInteger()
                                        def covered = coverage[1].toInteger()
                                        def total = missed + covered
                                        def percentage = total > 0 ? (covered * 100 / total) : 0
                                        
                                        coverageResults[service] = percentage
                                        echo "Coverage for ${service}: ${percentage}% (${covered}/${total})"
                                        
                                        if (percentage < 70) {
                                            coveragePass = false
                                            echo "Coverage for ${service} is below 70% (${percentage}%)"
                                        }
                                    } else {
                                        coveragePass = false
                                        echo "Could not determine coverage for ${service}"
                                    }
                                }
                            }
                            
                            // Fail the build if coverage is insufficient
                            if (!coveragePass) {
                                error("One or more services have insufficient test coverage (minimum 70% required)")
                            }
                            
                            jacoco(
                                execPattern: jacocoPattern,
                                classPattern: CHANGED_SERVICES_LIST.contains('all') ?
                                    '**/target/classes' :
                                    CHANGED_SERVICES_LIST.collect { "spring-petclinic-${it}-service/target/classes" }.join(','),
                                sourcePattern: CHANGED_SERVICES_LIST.contains('all') ?
                                    '**/src/main/java' :
                                    CHANGED_SERVICES_LIST.collect { "spring-petclinic-${it}-service/src/main/java" }.join(',')
                            )
                        } else {
                            echo 'No JaCoCo execution data found, skipping coverage report.'
                        }
                    }
                }
            }
        }

stage('Check Code Coverage') {
    steps {
        script {
            def failedServices = []

            CHANGED_SERVICES_LIST.each { service ->
                if (service in ['customers', 'visits', 'vets']) {
                    def coverageReport = "spring-petclinic-${service}-service/target/site/jacoco/jacoco.xml"
                    def coverageThreshold = 70.0

                    def lineCoverage = sh(script: """
                        if [ -f ${coverageReport} ]; then
                            awk '
                                /<counter type="LINE"[^>]*missed=/ {
                                    split(\$0, a, "[ \\\"=]+");
                                    # Debug output
                                    print "Debug: Checking jacoco.xml for ${service}..." > "/dev/stderr";
                                    print "Raw line: " \$0 > "/dev/stderr";
                                    print "Array after split:" > "/dev/stderr";
                                    for (i in a) print "a[" i "] = " a[i] > "/dev/stderr";
                                    # Find missed and covered indices
                                    for (i in a) {
                                        if (a[i] == "missed") missed = a[i+1];
                                        if (a[i] == "covered") covered = a[i+1];
                                    }
                                    print "missed = " missed ", covered = " covered > "/dev/stderr";
                                    sum = missed + covered;
                                    print "sum (missed + covered) = " sum > "/dev/stderr";
                                    coverage = (sum > 0 ? (covered / sum) * 100 : 0);
                                    print "Coverage = " coverage "%" > "/dev/stderr";
                                    print "-----" > "/dev/stderr";
                                    # Output final coverage value to stdout
                                    print coverage;
                                }
                            ' ${coverageReport}
                        else
                            echo "File not found: ${coverageReport}" > "/dev/stderr"
                            echo "0"
                        fi
                    """, returnStdout: true).trim()

                    if (lineCoverage) {
                        echo "Code coverage for ${service}: ${lineCoverage}%"
                        def coverageValue = lineCoverage.toDouble()
                        if (coverageValue < coverageThreshold) {
                            failedServices.add(service)
                        }
                    } else {
                        echo "No coverage report found for ${service}, assuming 0%"
                        failedServices.add(service)
                    }
                }
            }

            if (!failedServices.isEmpty()) {
                error "The following services failed code coverage threshold (${coverageThreshold}%): ${failedServices.join(', ')}"
            }
        }
    }
}


        stage('Build') {
            when {
                expression { 
                    // Only build if all previous stages succeeded
                    currentBuild.result == null || currentBuild.result == 'SUCCESS' 
                }
            }
            steps {
                script {
                    if (CHANGED_SERVICES_LIST.contains('all')) {
                        echo 'Building all modules'
                        sh './mvnw clean package -DskipTests'
                    } else {
                        def modules = CHANGED_SERVICES_LIST.collect { "spring-petclinic-${it}-service" }.join(',')
                        echo "Building modules: ${modules}"
                        sh "./mvnw clean package -DskipTests -pl ${modules}"
                    }
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                }
            }
        }
    }
}
