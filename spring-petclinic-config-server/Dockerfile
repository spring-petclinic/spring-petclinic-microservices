# Use OpenJDK 17 as the base image
FROM eclipse-temurin:17-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/spring-petclinic-config-server-3.4.1.jar app.jar

# Expose the port the app runs on
EXPOSE 8888

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]