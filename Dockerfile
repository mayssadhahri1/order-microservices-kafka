# Image commune aux 4 services : le service à construire est passé avec --build-arg SERVICE=...
FROM maven:3.9-eclipse-temurin-21 AS build
ARG SERVICE
WORKDIR /app
COPY pom.xml .
COPY order-service/pom.xml order-service/
COPY inventory-service/pom.xml inventory-service/
COPY notification-service/pom.xml notification-service/
COPY api-gateway/pom.xml api-gateway/
COPY ${SERVICE}/src ${SERVICE}/src
RUN mvn -q -pl ${SERVICE} -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
ARG SERVICE
WORKDIR /app
COPY --from=build /app/${SERVICE}/target/${SERVICE}-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
