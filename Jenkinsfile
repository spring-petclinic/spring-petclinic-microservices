pipeline {
    
    podTemplate(label: 'jenkins-ja', containers: [
    containerTemplate(name: 'jenkinsja', image: 'cloudbees/jnlp-slave-with-java-build-tools', ttyEnabled: true, 
        command: 'cat')    
  ]) {
    node('jenkins-ja') {
        container('jenkinsja') {
  //          stage('Run Command') {
  //              sh 'cat /etc/issue'
  //          }
 //       }
 //   }
//}
//    agent {
//        docker 'cloudbees/jnlp-slave-with-java-build-tools'
//    }
    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
            }
        }

        stage ('Build') {
            steps {
                sh 'mvn -Dmaven.test.failure.ignore=true install' 
            }
            post {
                success {
                    junit 'target/surefire-reports/**/*.xml' 
                }
            }
        }
    }
        }
    }
    }
}

