# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Add wget for healthcheck
RUN apk add --no-cache wget

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Create volume for file uploads
VOLUME /internship-reports

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Set JVM options for Koyeb free tier (512MB RAM)
ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m -Xss256k"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
