dockerNode{
  stage('Preparation') { // for display purposes
     // Get some code from a GitHub repository
     git 'https://github.com/laurentgrangeau/spring-petclinic-microservices.git'
  }
  stage('Build') {
     withDocker(image: 'maven:3.3.9-jdk-8-alpine',
                commands:"""
                   mvn clean install -PbuildDocker
                """)
     // Run the maven build
     if (isUnix()) {
        sh "'${mvnHome}/bin/mvn' -Dmaven.test.failure.ignore clean package"
     } else {
        bat(/"${mvnHome}\bin\mvn" -Dmaven.test.failure.ignore clean package/)
     }
  }
  stage('Results') {
     junit '**/target/surefire-reports/TEST-*.xml'
     archive 'target/*.jar'
  }
}