podTemplate(label: 'maven', activeDeadlineSeconds: 180, containers: [
    containerTemplate(name: 'maven', image: 'maven:3.5.4-jdk-10-slim')
  ]) {
  steps {
              withSonarQubeEnv('My SonarQube Server') {
                sh 'mvn clean package sonar:sonar'
              }
            }
}

