//podTemplate(label: 'maven', activeDeadlineSeconds: 180, containers: [
//    containerTemplate(name: 'maven', image: 'maven:3.5.4-jdk-10-slim')
//  ]) {
 
pipeline {
  agent {
      label "default"
  }
  stages {
    stage('Run maven') {
      steps {
        sh 'mvn -version'
          sh 'mvnw -version'
      }
    }
  }
}



