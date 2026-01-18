# Ktor Koin Exposed Service Template

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-blue.svg)](https://github.com/JetBrains/kotlin)
[![Ktor](https://img.shields.io/badge/Ktor-3.3.3-blue.svg)](https://github.com/ktorio/ktor)
[![Koin](https://img.shields.io/badge/Koin-4.2.0-beta2-blue.svg)](https://github.com/InsertKoinIO/koin)
[![Exposed](https://img.shields.io/badge/Exposed-1.0.0-rc-4-blue.svg)](https://github.com/JetBrains/Exposed)

This project is a Kotlin-based web service built using [Ktor](https://ktor.io), [Koin](https://insert-koin.io/), and [Exposed](https://www.jetbrains.com/exposed/). It provides a structured, dependency-injected, and database-integrated backend solution.

## Features

| Feature                                                          | Description                                                          |
|------------------------------------------------------------------|----------------------------------------------------------------------|
| [Routing](https://ktor.io/docs/routing.html)                     | Provides a structured routing DSL for defining API endpoints.        |
| [Swagger UI](https://ktor.io/docs/openapi.html)                  | Serves an interactive Swagger UI for API documentation.              |
| [Kotlinx Serialization](https://ktor.io/docs/serialization.html) | Enables JSON serialization using kotlinx.serialization.              |
| [Content Negotiation](https://ktor.io/docs/serialization.html)   | Supports automatic content conversion based on Content-Type headers. |
| [Koin](https://insert-koin.io/)                                  | Dependency injection for managing service components.                |
| [Exposed](https://github.com/JetBrains/Exposed)                  | SQL database access via Kotlin's Exposed ORM.                        |
| [PostgreSQL](https://www.postgresql.org/)                        | Uses PostgreSQL as the relational database.                          |
| [HikariCP](https://github.com/brettwooldridge/HikariCP)          | Connection pooling for efficient database interaction.               |
| [Retrofit](https://square.github.io/retrofit/)                   | Type-safe HTTP client for API communication.                         |
| [Okio](https://square.github.io/okio/)                           | Modern I/O library for efficient data processing.                    |

## Running the Project

To build and run the project, use the following Gradle tasks:

| Command                                 | Description                                    |
|-----------------------------------------|------------------------------------------------|
| `./gradlew test`                        | Run unit tests.                                |
| `./gradlew build`                       | Build the project.                             |
| `./gradlew run`                         | Start the server.                              |
| `./gradlew buildFatJar`                 | Build an executable JAR with dependencies.     |
| `./gradlew buildImage`                  | Build a Docker image for deployment.           |
| `./gradlew publishImageToLocalRegistry` | Publish the Docker image locally.              |
| `./gradlew runDocker`                   | Run the application inside a Docker container. |

When the server starts successfully, you should see output similar to:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## API Documentation

Once the application is running locally, you can access the Swagger UI for API documentation at:

http://localhost:8080/swagger [Swagger UI](http://localhost:8080/swagger)

## Using Ktor

[Ktor](https://ktor.io/) is the main framework used to build this service. It provides an asynchronous, lightweight, and modular way to build web applications in Kotlin.

### Application Startup

The application starts using an embedded server:

```kotlin
fun main() {
    embeddedServer(CIO, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    configureKoin()
    configureSerialization()
    configureSwagger()

    val databaseConfiguration: DatabaseConfiguration by inject()
    databaseConfiguration.configureDatabase()

    val userController by inject<UserController>()
    userController.registerUserRoutes(this)
}
```

The `module` function initializes dependencies and sets up configurations.

### Routing with Ktor

Ktor provides a DSL for defining routes. For example, the `UserController` registers API endpoints as follows:

```kotlin
@Single
class UserController(private val userRepository: UserRepository) {
    fun registerUserRoutes(application: Application) {
        application.routing {
            post("/users") { /* Handle user creation */ }
            get("/users/{id}") { /* Handle fetching a user */ }
            get("/users") { /* Handle fetching all users */ }
            put("/users/{id}") { /* Handle updating a user */ }
            delete("/users/{id}") { /* Handle deleting a user */ }
        }
    }
}
```

## Using Koin for Dependency Injection

[Koin](https://insert-koin.io/) is a lightweight dependency injection framework for Kotlin. In this application, Koin is used to manage dependencies and service components efficiently.

### Koin Configuration

Koin is installed in the Ktor application using the following configuration:

```kotlin
fun Application.configureKoin() {
    install(Koin) {
        modules(
            defaultModule
        )
    }
}
```

### Dependency Injection with Koin Annotations

Koin annotations simplify dependency injection by automatically handling object creation. Below is an example of how it is used:

```kotlin
@Single
class UserController(private val userRepository: UserRepository)

@Single
class UserRepository()
```

The `@Single` annotation ensures that a single instance of the class is used throughout the application.

## Using Exposed for Database Access

[Exposed](https://github.com/JetBrains/Exposed) is a lightweight SQL library for Kotlin that provides a typesafe and concise way to interact with databases.

### Exposed Schema and Queries

In this application, Exposed is used to define and interact with the `Users` table:

```kotlin
object Users : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", length = 50)
    val age = integer("age")
    override val primaryKey = PrimaryKey(id)
}
```

### CRUD Operations with Exposed

Exposed provides a simple way to perform database operations using Kotlin DSL:

- **Create a user:**

```kotlin
Users.insert {
    it[name] = user.name
    it[age] = user.age
}[Users.id]
```

- **Read a user:**

```kotlin
Users.selectAll()
    .where { Users.id eq id }
    .map { ExposedUser(it[Users.name], it[Users.age]) }
    .singleOrNull()
```

- **Update a user:**

```kotlin
Users.update({ Users.id eq id }) {
    it[name] = user.name
    it[age] = user.age
}
```

- **Delete a user:**

```kotlin
Users.deleteWhere { Users.id.eq(id) }
```

## Running Tests with PostgreSQL

To run tests locally, you need a PostgreSQL database. You can start a local instance using Docker Compose:

```sh
docker-compose up -d
```

```sh
docker build -t my-ktor-app .
```

```sh
docker run --rm -it \
  --network=host \
  -e POSTGRES_HOST=localhost \
  -e KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
  -e POSTGRES_PASSWORD=mypassword \
  my-ktor-app
```

This will spin up a PostgreSQL container in the background. Make sure your application is configured to connect to this instance.

## Using SDKMAN for Environment Management

This project includes a `.sdkmanrc` file, which helps manage SDK versions via [SDKMAN!](https://sdkman.io/). To automatically switch to the required Java version, run:

```sh
sdk env
```

Ensure you have SDKMAN installed before using this command.

## Dependency Auto-Updates with Renovate

This project uses [Renovate](https://docs.renovatebot.com/) for automated dependency updates. Renovate ensures dependencies stay up-to-date by automatically creating pull requests when new versions are available. The configuration enables auto-merging of approved updates and requires successful build and test checks before merging.

## Using Retrofit and Okio for HTTP Communication

This project uses [Retrofit](https://square.github.io/retrofit/) and [Okio](https://square.github.io/okio/) for efficient HTTP communication with external APIs.

### Retrofit Configuration

Retrofit is a type-safe HTTP client for Android and Java/Kotlin. It's configured in this project as follows:

```kotlin
val retrofitModule = module {
    // JSON serialization configuration
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }
    }

    // HTTP client configuration with OkHttp
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    // Retrofit instance configuration
    single<Retrofit> {
        val okHttpClient = get<OkHttpClient>()
        val converterFactory = get<Converter.Factory>()

        Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .build()
    }
}
```

### Okio Benefits

[Okio](https://square.github.io/okio/) is a modern I/O library that complements OkHttp (used by Retrofit internally). It provides:

- More efficient I/O operations than traditional Java I/O
- Simplified resource management
- Better performance for network operations
- Reduced memory consumption

### Pokemon API Integration

This project integrates with the [PokeAPI](https://pokeapi.co/), a comprehensive Pokemon data API. The integration is implemented using Retrofit:

#### API Interface

```kotlin
interface PokemonApi {
    @GET("pokemon/")
    fun getPokemonList(
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
    ): Call<PokemonListResponse>

    @GET("pokemon/{idOrName}/") 
    fun getPokemon(@Path("idOrName") idOrName: String): Call<Pokemon>

    @GET("pokemon-species/{idOrName}/")
    fun getPokemonSpecies(@Path("idOrName") idOrName: String): Call<PokemonSpecies>
}
```

#### Available Endpoints

The Pokemon API integration provides the following endpoints:

| Endpoint                       | Description                                           |
|-------------------------------|-------------------------------------------------------|
| `GET /api/pokemon`            | Get a paginated list of Pokemon                       |
| `GET /api/pokemon/{idOrName}` | Get detailed information about a specific Pokemon     |
| `GET /api/pokemon-species/{idOrName}` | Get species information about a Pokemon       |

These endpoints serve as a proxy to the PokeAPI, handling error cases and providing a consistent interface for clients.

