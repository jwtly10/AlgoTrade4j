FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Download the Datadog Java agent
RUN wget -O dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'

COPY target/algotrade4j-liveservice-*.jar /app/algotrade4j-liveservice.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8081 9011

ENTRYPOINT ["sh", "-c", "java -javaagent:/app/dd-java-agent.jar -jar /app/algotrade4j-liveservice.jar"]