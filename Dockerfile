FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the Spring Boot JAR
COPY target/demoaccount-1.0.0-SNAPSHOT.jar app.jar

# Copy WireMock mappings and files to a location WireMock can access
COPY src/main/resources/wiremock /app/wiremock

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=70.0", "-jar", "/app/app.jar", "--wiremock.root.dir=/app/wiremock"]
