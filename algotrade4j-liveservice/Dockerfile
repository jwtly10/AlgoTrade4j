FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/algotrade4j-liveservice-*.jar /app/algotrade4j-liveservice.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8081 9011

ENTRYPOINT ["sh", "-c", "java -jar /app/algotrade4j-liveservice.jar"]