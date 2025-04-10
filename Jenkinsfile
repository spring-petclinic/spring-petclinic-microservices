stage('Check Changes') {
    steps {
        script {
            def changes = sh(script: 'git diff --name-only $GIT_PREVIOUS_COMMIT $GIT_COMMIT', returnStdout: true).trim()

            // Kiểm tra thay đổi trong thư mục dịch vụ
            if (changes.contains('vets-service/')) {
                env.SERVICE = 'vets-service'
            } else if (changes.contains('customer-service/')) {
                env.SERVICE = 'customer-service'
            } else if (changes.contains('visit-service/')) {
                env.SERVICE = 'visit-service'
            } else if (changes.any { it.startsWith('pom.xml') || it.startsWith('Jenkinsfile') }) {
                // Nếu thay đổi ở thư mục root (như pom.xml hoặc Jenkinsfile), build tất cả các dịch vụ
                env.SERVICE = 'all-services'
            } else {
                env.SERVICE = null
            }

            if (env.SERVICE == null) {
                currentBuild.result = 'SUCCESS'
                echo "No relevant changes detected. Skipping build and tests."
            } else if (env.SERVICE == 'all-services') {
                echo "Changes detected in root directory. Building and testing all services."
            } else {
                echo "Changes detected in ${env.SERVICE}. Proceeding with build and tests."
            }
        }
    }
}
