pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo "🏃🏃🏃 Running Build phase..."
                echo "☑️ Build completed successfully!"
            }
        }

        stage('Test') {
            steps {
                echo "🏃🏃🏃 Running Test phase..."
                
                echo "Uploading test results..."
                echo "☑️ Test results uploaded successfully!"
                
                echo "Uploading test case coverage..."
                echo "☑️ Test case coverage uploaded successfully!"
            }
        }
    }
    
    post {
        always {
            echo "🔥🔥🔥 Pipeline finished. 🔥🔥🔥"
        }
    }
}
