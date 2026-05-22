# Build stage: Use the Kotlin Toolchain CLI to build the application
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Install curl so we can pull the Kotlin CLI installer
RUN apt-get update && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Install the Kotlin Toolchain CLI without touching the shell profile
ENV KOTLIN_CLI_NO_MODIFY_PATH=1
RUN curl -fsSL https://kotl.in/install.sh | sh
ENV PATH="/root/.local/bin:${PATH}"

COPY . .

# Produce the Spring-Boot-loader style executable JAR (replacement for ./gradlew buildFatJar)
RUN kotlin package

# Copy the produced artifact to a stable, predictable path
RUN cp build/tasks/_*_executableJarJvm/*-executable.jar /tmp/app.jar

# Runtime stage: Use JRE for a smaller image
FROM eclipse-temurin:25.0.1_8-jre

WORKDIR /app
COPY --from=build /tmp/app.jar app.jar
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
