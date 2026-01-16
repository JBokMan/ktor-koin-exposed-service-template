# Ktor-Koin-Exposed Service Template

A production-quality Kotlin microservice template demonstrating best practices for building modern services with Ktor, Koin DI, and Exposed ORM.

## Claude Code Instructions

- Always use Context7 MCP when I need library/API documentation, code generation, setup or configuration steps without me having to explicitly ask.
- Always use the JetBrains MCP for IDE operations like file navigation, code analysis, refactoring, running configurations, and terminal commands when working with IntelliJ IDEA.
- Always use the PostgreSQL MCP (read-only) to query the local database for debugging and data inspection when the application is running locally.
- Use the Bash tool with Kafka CLI for debugging Kafka when the application is running locally:
  ```bash
  # List topics
  docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list

  # Consume messages
  docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic test-topic --from-beginning

  # Describe consumer groups
  docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group test-group
  ```

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.3.0 | Language |
| JDK | 25 | Runtime |
| Ktor | 3.3.3 | Web framework (CIO engine) |
| Koin | 4.2.0-beta2 | Dependency injection (with KSP annotations) |
| Exposed | 1.0.0-rc-4 | Database ORM |
| PostgreSQL | 42.7.8 driver | Database |
| HikariCP | - | Connection pooling |
| Kotlinx Serialization | - | JSON serialization |
| Retrofit/OkHttp | - | HTTP clients |
| Kafka | reactor-kafka | Message streaming |
| Cohort | - | Health checks/metrics |

## Project Structure

```
src/main/kotlin/
├── Application.kt           # Entry point, module configuration
├── config/
│   ├── CohortConfiguration.kt      # Health check probes (K8s)
│   ├── DatabaseConfiguration.kt    # HikariCP + PostgreSQL setup
│   ├── HttpClientConfiguration.kt  # Ktor HttpClient with OkHttp
│   ├── KafkaConfiguration.kt       # Kafka consumer (reactive)
│   ├── KoinConfiguration.kt        # DI modules
│   ├── RetrofitConfiguration.kt    # Retrofit for external APIs
│   ├── SerializationConfiguration.kt
│   └── SwaggerConfiguration.kt     # OpenAPI/Swagger UI
├── boundary/
│   ├── KafkaController.kt          # POST /kafka/messages
│   ├── UserController.kt           # CRUD /users endpoints
│   └── pokemon/
│       ├── PokemonController.kt    # GET /api/pokemon/* endpoints
│       ├── PokemonRetrofitApi.kt   # Retrofit interface for PokeAPI
│       └── *.kt                    # Request/Response models
├── repository/
│   └── UserRepository.kt           # Exposed table + CRUD operations
└── service/
    ├── KafkaService.kt             # Kafka producer
    └── UserService.kt              # User business logic

src/test/kotlin/
├── IntegrationTestBase.kt          # TestContainers setup (PostgreSQL, Kafka)
├── ApplicationTest.kt              # Basic integration tests
└── PokemonApiIntegrationTest.kt    # External API tests
```

## Architecture Pattern

```
HTTP Request → Controller → Service → Repository → Database
                   ↓
              Koin DI (auto-wired via @Single annotations)
```

- **Controllers**: Route handlers with OpenAPI annotations
- **Services**: Business logic, marked with `@Single` for Koin
- **Repositories**: Exposed DSL for database operations using `suspendTransaction`

## Key Configuration Files

| File | Purpose |
|------|---------|
| `build.gradle.kts` | Build config, dependencies, Ktor plugin |
| `gradle/libs.versions.toml` | Centralized version catalog |
| `src/main/resources/application.yaml` | Ktor YAML config |
| `docker-compose.yml` | Local dev environment (PostgreSQL + Kafka) |
| `Dockerfile` | Multi-stage build (gradle → temurin JRE) |

## API Endpoints

### Users (`/users`)
- `POST /users` - Create user (returns ID)
- `GET /users` - List all users
- `GET /users/{id}` - Get user by ID
- `PUT /users/{id}` - Update user
- `DELETE /users/{id}` - Delete user

### Pokemon Proxy (`/api/pokemon`)
- `GET /api/pokemon?offset=X&limit=Y` - Paginated list
- `GET /api/pokemon/{idOrName}` - Get Pokemon details
- `GET /api/pokemon-species/{idOrName}` - Get species info

### Kafka (`/kafka`)
- `POST /kafka/messages` - Publish message to topic

### Health/Observability
- `GET /health` - General health
- `GET /liveness` - K8s liveness probe
- `GET /readiness` - K8s readiness probe
- `GET /startup` - K8s startup probe
- `GET /swagger` - Swagger UI
- `GET /api.json` - OpenAPI spec

## Database Schema

**Users table** (Exposed DSL in `UserRepository.kt`):
```kotlin
object Users : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", length = 50)
    val age = integer("age")
    override val primaryKey = PrimaryKey(id)
}
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | 8080 | Server port |
| `POSTGRES_HOST` | localhost | Database host |
| `POSTGRES_PORT` | 5432 | Database port |
| `POSTGRES_DB` | mydatabase | Database name |
| `POSTGRES_USER` | myuser | Database user |
| `POSTGRES_PASSWORD` | mypassword | Database password |
| `KAFKA_BOOTSTRAP_SERVERS` | localhost:9092 | Kafka brokers |
| `KAFKA_TOPIC` | test-topic | Kafka topic |
| `KAFKA_GROUP_ID` | test-group | Consumer group |

## Common Commands

```bash
# Development
./gradlew run                    # Start server
./gradlew build                  # Build project
./gradlew test                   # Run tests
docker-compose up -d             # Start PostgreSQL + Kafka

# Docker
./gradlew buildFatJar            # Create executable JAR
./gradlew buildImage             # Build Docker image
./gradlew runDocker              # Run in Docker

# Code Quality
./gradlew ktfmtFormat            # Format code
```

## Testing

- **Framework**: JUnit 5 + Kotlin Test + Ktor TestApplication
- **Containers**: TestContainers for PostgreSQL and Kafka
- **Base Class**: `IntegrationTestBase.kt` - Smart container detection (skips if services already running)

## CI/CD

- **build-and-test.yml**: PR builds with Gradle caching
- **update-badges.yml**: Weekly badge updates via automated PR
- **Renovate**: Automated dependency updates (no auto-merge)

## Code Style

- **Formatter**: ktfmt with Kotlin official style
- **Post-commit hook**: Auto-formats and amends commits (`scripts/post-commit`)
- **SDK**: SDKMAN with `.sdkmanrc` for Java 25.0.1-amzn

## Key Patterns

1. **Koin Annotations**: Use `@Single` on classes for auto-registration via KSP
2. **Suspend Transactions**: All DB operations use `suspendTransaction` for coroutine safety
3. **Modular Config**: Each concern (DB, Kafka, HTTP, etc.) has dedicated configuration file
4. **OpenAPI First**: All endpoints documented with Ktor OpenAPI annotations
5. **Health Probes**: Comprehensive Cohort checks for K8s deployment readiness

## Important Implementation Details

- **Application entry**: `Application.kt:main()` starts CIO server
- **Koin setup**: `KoinConfiguration.kt` composes `ktorModule`, `defaultModule` (KSP-generated), `networkModule`, `retrofitModule`
- **DB initialization**: `DatabaseConfiguration.kt` creates HikariCP pool and auto-creates tables
- **Graceful shutdown**: Custom `ShutdownPlugin` in Application.kt handles cleanup
