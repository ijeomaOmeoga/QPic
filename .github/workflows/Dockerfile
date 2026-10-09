# Stage 1: Build stage using Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copy dependencies configuration
COPY pom.xml .

# 2. Copy application source code
COPY src ./src

# 3. Compile and package app targeting Java 21
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

# Stage 2: Lightweight Runtime stage
FROM eclipse-temurin:21-jdk
WORKDIR /app

# 4. Copy final JAR executable from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
