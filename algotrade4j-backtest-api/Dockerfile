FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install wget to download the New Relic agent
RUN apk add --no-cache wget

# Download and unzip the New Relic Java agent
RUN wget https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip newrelic-java.zip && \
    rm newrelic-java.zip

COPY target/algotrade4j-backtest-api-*.jar /app/algotrade4j-backtest-api.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080 9010

ENTRYPOINT ["sh", "-c", "java -javaagent:/app/newrelic/newrelic.jar -jar /app/algotrade4j-backtest-api.jar"]