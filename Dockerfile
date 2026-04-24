FROM eclipse-temurin:21-jre
WORKDIR /app

COPY target/gatewayservice-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]