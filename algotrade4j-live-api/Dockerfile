FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install wget to download the New Relic agent
RUN apk add --no-cache wget
# Download and unzip the New Relic Java agent
RUN wget https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip newrelic-java.zip && \
    rm newrelic-java.zip

# Add Arthas for remote debugging
RUN wget https://arthas.aliyun.com/arthas-boot.jar -O /app/arthas-boot.jar

COPY target/algotrade4j-live-api-*.jar /app/algotrade4j-live-api.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8081 9011

ENTRYPOINT ["sh", "-c", "java -javaagent:/app/newrelic/newrelic.jar -jar /app/algotrade4j-live-api.jar"]