# ─── Stage 1: Build Stage ───────────────────────────────────────────────────
FROM maven:3.9.8-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Cache dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production fat jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Stage 2: Runtime Stage ─────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runner

WORKDIR /app

# Create a non-privileged user and group for security
RUN addgroup -S dixygroup && adduser -S dixyuser -G dixygroup

# Install curl for container health check
RUN apk --no-cache add curl

# Copy jar from builder stage
COPY --from=builder /build/target/dixy-backend-0.0.1-SNAPSHOT.jar app.jar

# Set ownership
RUN chown -R dixyuser:dixygroup /app

# Run as unprivileged user
USER dixyuser

EXPOSE 8080

# Configure JVM options for container memory awareness
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
