FROM openjdk:21-jdk-slim

WORKDIR /app

COPY algotrade4j-api/target/algotrade4j-api-1.0-SNAPSHOT.jar algotrade4j-api.jar

COPY algotrade4j-api/src/main/resources/application.properties /app/application.properties

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/algotrade4j-api.jar", ""]