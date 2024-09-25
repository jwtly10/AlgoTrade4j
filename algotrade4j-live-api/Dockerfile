FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/algotrade4j-live-api-*.jar /app/algotrade4j-live-api.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8081 9011

ENTRYPOINT ["sh", "-c", "java -jar /app/algotrade4j-live-api.jar"]