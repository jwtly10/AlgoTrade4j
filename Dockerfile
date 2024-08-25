FROM openjdk:21-jdk-slim

WORKDIR /app

COPY algotrade4j-api/target/algotrade4j-api-*.jar /app/algotrade4j-api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/algotrade4j-api.jar"]