FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Download the Datadog Java agent
RUN wget -O dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'

COPY algotrade4j-api/target/algotrade4j-api-*.jar /app/algotrade4j-api.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080 9010

ENTRYPOINT ["sh", "-c", "java -jar /app/algotrade4j-api.jar"]