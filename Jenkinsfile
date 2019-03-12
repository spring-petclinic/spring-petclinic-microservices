podTemplate(label: 'maven', activeDeadlineSeconds: 180, containers: [
    containerTemplate(name: 'maven', image: 'maven:3.5.4-jdk-10-slim')
  ]) {
 
                sh 'mvn clean package sonar:sonar'

}

