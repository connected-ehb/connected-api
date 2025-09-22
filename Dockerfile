# syntax=docker/dockerfile:1

############################
# Stage 1: Build the JAR
############################
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

############################
# Stage 2: Run with JRE and wait-for-db
############################
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Install netcat for a tiny wait loop (no healthchecks involved)
RUN apt-get update && apt-get install -y --no-install-recommends netcat-openbsd \
  && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/target/connected-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Wait for MySQL on "db:3306" (docker-compose service) before starting the app
ENTRYPOINT [ "sh", "-c", "until nc -z db 3306; do echo 'waiting for db...'; sleep 1; done; java -jar /app/app.jar" ]
