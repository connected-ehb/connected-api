# syntax=docker/dockerfile:1.6

############################
# Stage 1: Build the JAR
############################
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

# Keep Maven repo + tmp small and cached across layers
ENV MAVEN_OPTS="-Djava.io.tmpdir=/tmp/mvn-tmp"
ENV MAVEN_CONFIG=/root/.m2

COPY pom.xml ./
# (No go-offline; just let the package step resolve deps, with cache)
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=cache,target=/tmp/mvn-tmp \
    mvn -B -DskipTests -Dmaven.repo.local=/root/.m2/repository package

############################
# Stage 2: Run with JRE and wait-for-db
############################
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Small runtime helper for waiting on DB
RUN apt-get update && apt-get install -y --no-install-recommends netcat-openbsd \
  && rm -rf /var/lib/apt/lists/*

# Copy the built jar (use a glob so version bumps don’t break builds)
COPY --from=build /workspace/target/*-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

# Wait for MySQL on "db:3306" before starting the app
# Use exec to forward signals correctly
ENTRYPOINT ["sh","-c","until nc -z db 3306; do echo 'waiting for db...'; sleep 1; done; exec java -jar /app/app.jar"]
