# Stage 1: Build the Maven application
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source and build package
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal JRE Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the generated JAR
COPY --from=builder /app/target/*.jar app.jar

# Render dynamically passes PORT (e.g. 10000)
ENV PORT=8080
EXPOSE 8080

# Optimize JVM memory consumption for Render's 512MB free RAM limit
ENTRYPOINT ["java", "-Xmx384m", "-Xss512k", "-XX:+UseG1GC", "-Dserver.port=${PORT}", "-jar", "app.jar"]
