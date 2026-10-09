# Build any module:  docker build --build-arg MODULE=media-service -t qpic/media-service .

FROM maven:3.9-eclipse-temurin-21 AS build
ARG MODULE
WORKDIR /src
COPY . .
RUN mvn -q -B -pl media-service -am -DskipTests 
RUN mvn -q -B -pl auth-service -am -DskipTests 
RUN mvn -q -B -pl album-service -am -DskipTests 
RUN mvn -q -B -pl api-gateway -am -DskipTests 
RUN mvn -q -B -pl share-service -am -DskipTests 
RUN mvn -q -B -pl discovery-server -am -DskipTests 

FROM eclipse-temurin:21-jre
ARG MODULE
WORKDIR /app
COPY --from=build /src/${MODULE}/target/${MODULE}-1.0.0.jar app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
