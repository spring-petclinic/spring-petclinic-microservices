FROM eclipse-temurin:17 AS build

WORKDIR /app

COPY . /app

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
