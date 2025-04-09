pipeline {
agent any
stages {
stage('Build') {
steps {
echo "Building.."
script {
                    def content = readFile('a.txt').trim()  // Read the content of a.txt

                    if (content == 'a') {
                        echo "The content is 'a'"
                        // Add your actions for 'a' here
                    } else if (content == 'b') {
                        error("Simulating error due to content 'b'")
                        // Add your actions for 'b' here
                    } else {
                        echo "The content is neither 'a' nor 'b'"
                        // Add fallback actions if needed
                    }
                }
}
}
stage('Test') {
steps {
echo "Testing.."
sh '''
echo "doing test stuff.."
'''
}
}
stage('Deliver') {
steps {
echo 'Deliver....'
sh '''
echo "doing delivery stuff.."
'''
}
}
}
}
