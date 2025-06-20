# Build stage: Use Gradle to build the application
FROM gradle:8.14.2-jdk21 AS build

WORKDIR /app
COPY . .

# Add this line to clear the cache before building
RUN rm -rf /home/gradle/.gradle/caches/

# Your original build command
RUN gradle build --no-daemon

# Runtime stage: Use JRE for a smaller image
FROM eclipse-temurin:21.0.7_6-jre

WORKDIR /app
# Copy only the built JAR and configuration from the build stage
COPY --from=build /app/build/libs/*.jar app.jar
COPY --from=build /app/src/main/resources/application.yaml /app/application.yaml

# Environment variables that can be overridden
# Kafka configuration
ENV KAFKA_BOOTSTRAP_SERVERS=kafka:9092
ENV KAFKA_TOPIC=my-topic
ENV KAFKA_GROUP_ID=ktor-kafka-group
ENV KAFKA_AUTO_OFFSET_RESET=earliest
ENV KAFKA_ACKS_CONFIG=all

# Database configuration
ENV POSTGRES_HOST=postgres
ENV POSTGRES_PORT=5432
ENV POSTGRES_DB=mydatabase
ENV POSTGRES_USER=myuser

# Application configuration
ENV PORT=8080

EXPOSE $PORT

ENTRYPOINT ["java", "-jar", "app.jar"]
