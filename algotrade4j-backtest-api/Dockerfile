FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/algotrade4j-backtest-api-*.jar /app/algotrade4j-backtest-api.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080 9010

ENTRYPOINT ["sh", "-c", "java -jar /app/algotrade4j-backtest-api.jar"]