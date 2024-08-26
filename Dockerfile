FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY algotrade4j-api/target/algotrade4j-api-*.jar /app/algotrade4j-api.jar

ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseStringDeduplication"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/algotrade4j-api.jar"]