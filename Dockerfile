# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21 AS build

ARG MODULE=api-gateway
WORKDIR /src

COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY media-service/pom.xml media-service/pom.xml
COPY album-service/pom.xml album-service/pom.xml
COPY share-service/pom.xml share-service/pom.xml

COPY common common
COPY discovery-server discovery-server
COPY api-gateway api-gateway
COPY auth-service auth-service
COPY media-service media-service
COPY album-service album-service
COPY share-service share-service

RUN mvn -B -q -pl ${MODULE} -am package -DskipTests

FROM eclipse-temurin:21-jre
ARG MODULE=api-gateway
WORKDIR /app

COPY --from=build /src/${MODULE}/target/${MODULE}-1.0.0.jar /app/app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]