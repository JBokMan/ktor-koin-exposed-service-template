# Production Readiness Gap Analysis

> **Document Version**: 1.1
> **Last Updated**: January 2026
> **Project**: Ktor-Koin-Exposed Service Template

This document analyzes the current state of the service template and identifies gaps that must be addressed for production deployment.

---

## Executive Summary

This Ktor service template provides an excellent architectural foundation with clean separation of concerns, modern dependency injection (Koin + KSP), and solid testing infrastructure. Recent improvements have addressed several production readiness gaps.

| Category | Status | Assessment |
|----------|--------|------------|
| Security | :orange_circle: Partial | Security headers implemented; missing authentication, authorization |
| Observability | :yellow_circle: Improved | Health checks, structured logging, correlation IDs; missing metrics, tracing |
| Resilience | :yellow_circle: Gaps | No circuit breakers, retry policies, or rate limiting |
| API Standards | :yellow_circle: Improved | OpenAPI docs, API versioning; missing validation, error standards |
| Database | :yellow_circle: Gaps | No migrations, query timeouts, or read replica support |
| Configuration | :white_check_mark: Good | Environment profiles implemented; missing feature flags, secret vault |
| Testing | :yellow_circle: Improved | Integration tests, JaCoCo coverage; missing unit, contract, load tests |
| Documentation | :yellow_circle: Partial | Good README; missing ADRs, runbooks, changelog |

### Recently Implemented (Quick Wins)

| Feature | Status | Location |
|---------|--------|----------|
| Security Headers | :white_check_mark: Implemented | `config/SecurityHeadersConfiguration.kt` |
| Structured JSON Logging | :white_check_mark: Implemented | `logback.xml` |
| Request Logging + Correlation IDs | :white_check_mark: Implemented | `config/RequestLoggingConfiguration.kt` |
| Environment Profiles | :white_check_mark: Implemented | `application-{dev,staging,prod}.yaml` |
| API Versioning | :white_check_mark: Implemented | All endpoints now use `/api/v1/` |
| JaCoCo Code Coverage | :white_check_mark: Implemented | `build.gradle.kts` |

---

## 1. Security

### Current State

| Feature | Status | Location |
|---------|--------|----------|
| Database credentials via env vars | :white_check_mark: Exists | `DatabaseConfiguration.kt` |
| HikariCP connection pooling | :white_check_mark: Exists | `DatabaseConfiguration.kt` |
| Transaction isolation (REPEATABLE_READ) | :white_check_mark: Exists | `DatabaseConfiguration.kt` |
| Exposed ORM (SQL injection protection) | :white_check_mark: Exists | `UsersSchema.kt` |
| **Security Headers** | :white_check_mark: **Implemented** | `config/SecurityHeadersConfiguration.kt` |

#### Implemented Security Headers

The following security headers are now configured:

```kotlin
// config/SecurityHeadersConfiguration.kt
- X-Frame-Options: DENY                    // Prevents clickjacking
- X-Content-Type-Options: nosniff          // Prevents MIME sniffing
- X-XSS-Protection: 1; mode=block          // XSS filter
- Referrer-Policy: strict-origin-when-cross-origin
- Permissions-Policy                       // Restricts browser features
- Content-Security-Policy                  // CSP rules
- Strict-Transport-Security (HSTS)         // Enabled via ENABLE_HSTS=true
- Cache-Control: no-store                  // API response caching
```

### Remaining Gaps

| Feature | Priority | Description |
|---------|----------|-------------|
| **Authentication** | :red_circle: Critical | No JWT, OAuth2, or API key implementation. All endpoints are publicly accessible. |
| **Authorization** | :red_circle: Critical | No RBAC, permission checks, or role-based access control. |
| **HTTPS/TLS** | :red_circle: Critical | No SSL/TLS configuration for encrypted communication. |
| **Input Validation** | :red_circle: Critical | No validation framework. Endpoints accept any input without sanitization. |
| **Secrets Management** | :red_circle: Critical | Hardcoded default credentials in `Dockerfile` and `application.yaml`. |

> **Note**: CORS is not implemented as this is a backend-to-backend service template. Add CORS configuration if serving web clients directly.

### Recommendations

```kotlin
// Example: JWT Authentication with Ktor
install(Authentication) {
    jwt("auth-jwt") {
        realm = "Service API"
        verifier(JWT.require(Algorithm.HMAC256(secret))
            .withAudience(audience)
            .withIssuer(issuer)
            .build())
        validate { credential ->
            if (credential.payload.audience.contains(audience)) {
                JWTPrincipal(credential.payload)
            } else null
        }
    }
}

// Example: Input Validation with Konform
val validateUser = Validation<ExposedUser> {
    ExposedUser::name {
        minLength(2)
        maxLength(50)
        pattern("[a-zA-Z\\s]+") hint "Name must contain only letters"
    }
    ExposedUser::age {
        minimum(0)
        maximum(150)
    }
}
```

---

## 2. Observability

### Current State

| Feature | Status | Location |
|---------|--------|----------|
| Logging (kotlin-logging) | :white_check_mark: Exists | Throughout codebase |
| Health checks (Cohort) | :white_check_mark: Exists | `CohortConfiguration.kt` |
| K8s probes (startup/readiness/liveness) | :white_check_mark: Exists | `CohortConfiguration.kt` |
| SQL query logging | :white_check_mark: Exists | `DatabaseConfiguration.kt` |
| JVM metrics via Cohort | :white_check_mark: Exists | `CohortConfiguration.kt` |
| **Structured JSON Logging** | :white_check_mark: **Implemented** | `logback.xml` |
| **Request Logging** | :white_check_mark: **Implemented** | `config/RequestLoggingConfiguration.kt` |
| **Correlation IDs** | :white_check_mark: **Implemented** | `config/RequestLoggingConfiguration.kt` |

#### Implemented Logging Features

**Structured JSON Logging** (`logback.xml`):
- Console output (colored) for development: `LOG_FORMAT=console`
- JSON output for production: `LOG_FORMAT=json`
- Includes: correlationId, service name, version, timestamp

**Request Logging with Correlation IDs** (`config/RequestLoggingConfiguration.kt`):
- Generates/propagates `X-Correlation-ID` header
- MDC fields: correlationId, requestPath, requestMethod
- Filters health check endpoints from logs

```bash
# Usage
LOG_FORMAT=console ./gradlew run  # Development (colored)
LOG_FORMAT=json ./gradlew run     # Production (JSON)
```

### Remaining Gaps

| Feature | Priority | Description |
|---------|----------|-------------|
| **Prometheus Metrics** | :orange_circle: High | No Micrometer/Prometheus integration for application metrics. |
| **Distributed Tracing** | :orange_circle: High | No OpenTelemetry, Jaeger, or Zipkin integration. |
| **Audit Logging** | :yellow_circle: Medium | No audit trail for user actions or data changes. |

### Recommendations

```kotlin
// Example: Micrometer Metrics
install(MicrometerMetrics) {
    registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    meterBinders = listOf(
        JvmMemoryMetrics(),
        JvmGcMetrics(),
        ProcessorMetrics(),
        JvmThreadMetrics()
    )
}

// Example: OpenTelemetry Tracing
val openTelemetry = OpenTelemetrySdk.builder()
    .setTracerProvider(tracerProvider)
    .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
    .build()
```

---

## 3. Resilience

### Current State

| Feature | Status | Location |
|---------|--------|----------|
| Graceful shutdown | :white_check_mark: Exists | `Application.kt` (ShutdownPlugin) |
| Connection pooling (HikariCP) | :white_check_mark: Exists | `DatabaseConfiguration.kt` |
| Connection timeouts | :white_check_mark: Exists | 5s connection, 3s validation |
| HTTP client timeouts | :white_check_mark: Exists | 30s connect/read in `HttpClient.kt` |
| Basic error handling | :white_check_mark: Exists | Try-catch in controllers |

### Remaining Gaps

| Feature | Priority | Description |
|---------|----------|-------------|
| **Circuit Breakers** | :orange_circle: High | No Resilience4j or similar implementation for external calls. |
| **Retry Policies** | :orange_circle: High | No automatic retries with exponential backoff. |
| **Rate Limiting** | :red_circle: Critical | No rate limiting on endpoints; vulnerable to abuse/DoS. |
| **Bulkhead Pattern** | :yellow_circle: Medium | No thread pool isolation or bulkheads. |
| **Request Timeouts** | :yellow_circle: Medium | No fine-grained per-endpoint timeout configuration. |
| **Graceful Degradation** | :yellow_circle: Medium | No fallback strategies for external API failures. |

### Recommendations

```kotlin
// Example: Resilience4j Circuit Breaker
val circuitBreaker = CircuitBreaker.of("pokemonApi", CircuitBreakerConfig.custom()
    .failureRateThreshold(50f)
    .waitDurationInOpenState(Duration.ofSeconds(30))
    .slidingWindowSize(10)
    .build())

// Example: Retry with Exponential Backoff
val retry = Retry.of("pokemonApi", RetryConfig.custom()
    .maxAttempts(3)
    .waitDuration(Duration.ofMillis(500))
    .retryExceptions(IOException::class.java)
    .build())

// Example: Rate Limiting (Ktor plugin)
install(RateLimit) {
    register(RateLimitName("api")) {
        rateLimiter(limit = 100, refillPeriod = 60.seconds)
    }
}
```

---

## 4. API Standards

### Current State

| Feature | Status | Location |
|---------|--------|----------|
| OpenAPI/Swagger documentation | :white_check_mark: Exists | `Swagger.kt`, all controllers |
| Consistent HTTP status codes | :white_check_mark: Exists | 200, 201, 202, 400, 404, 500 |
| Typed request/response models | :white_check_mark: Exists | Kotlin data classes |
| Pagination parameters | :white_check_mark: Exists | offset/limit in Pokemon endpoints |
| **API Versioning** | :white_check_mark: **Implemented** | All controllers use `/api/v1/` prefix |

#### Implemented API Versioning

All endpoints now use versioned paths:

| Before | After |
|--------|-------|
| `/users` | `/api/v1/users` |
| `/users/{id}` | `/api/v1/users/{id}` |
| `/kafka/messages` | `/api/v1/kafka/messages` |
| `/api/pokemon` | `/api/v1/pokemon` |
| `/api/pokemon/{idOrName}` | `/api/v1/pokemon/{idOrName}` |
| `/api/pokemon-species/{idOrName}` | `/api/v1/pokemon-species/{idOrName}` |

### Remaining Gaps

| Feature | Priority | Description |
|---------|----------|-------------|
| **RFC 7807 Problem Details** | :orange_circle: High | No standardized error response format. |
| **Request Validation** | :red_circle: Critical | No validation framework (Konform, Valiktor). |
| **Consistent Error Format** | :orange_circle: High | Each endpoint returns different error formats. |
| **ETag/Caching Headers** | :yellow_circle: Medium | No Cache-Control, ETag, or conditional requests. |
| **Cursor-based Pagination** | :yellow_circle: Medium | Only offset/limit; no cursor pagination for large datasets. |

### Recommendations

```kotlin
// Example: RFC 7807 Problem Details
@Serializable
data class ProblemDetail(
    val type: String = "about:blank",
    val title: String,
    val status: Int,
    val detail: String? = null,
    val instance: String? = null,
    val errors: List<ValidationError>? = null
)

// Example: Centralized Exception Handler
install(StatusPages) {
    exception<ValidationException> { call, cause ->
        call.respond(HttpStatusCode.BadRequest, ProblemDetail(
            type = "https://api.example.com/errors/validation-error",
            title = "Validation Failed",
            status = 400,
            detail = cause.message,
            instance = call.request.path()
        ))
    }
}
```

---

## 5. Database

### Current State

| Feature | Status | Location |
|---------|--------|----------|
| PostgreSQL support | :white_check_mark: Exists | `DatabaseConfiguration.kt` |
| HikariCP connection pooling | :white_check_mark: Exists | Max 10 connections (configurable via `DB_POOL_SIZE`) |
| Suspend transactions | :white_check_mark: Exists | `suspendTransaction` in `UsersSchema.kt` |
| SQL logging | :white_check_mark: Exists | `KotlinLoggingSqlLogger` |
| Health checks | :white_check_mark: Exists | Cohort DB connectivity check |

### Remaining Gaps

| Feature | Priority | Description |
|---------|----------|-------------|
| **Database Migrations** | :red_circle: Critical | No Flyway or Liquibase. Schema auto-created at runtime. |
| **Query Timeouts** | :orange_circle: High | No statement timeout at database level. |
| **Connection Pool Metrics** | :yellow_circle: Medium | Only health checks; no detailed Prometheus metrics. |
| **Read Replicas** | :yellow_circle: Medium | No configuration for read/write splitting. |
| **Schema Documentation** | :yellow_circle: Medium | No ER diagrams or schema documentation. |

### Recommendations

```kotlin
// Example: Flyway Integration
dependencies {
    implementation("org.flywaydb:flyway-core:10.x.x")
    implementation("org.flywaydb:flyway-database-postgresql:10.x.x")
}

// DatabaseConfiguration.kt
fun configureDatabase() {
    val flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()
    flyway.migrate()

    Database.connect(dataSource)
}

// Migration file: src/main/resources/db/migration/V1__Create_users_table.sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    age INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 6. Configuration

### Current State

| Feature | Status | Location |
|---------|--------|----------|
| Environment variables | :white_check_mark: Exists | All configs support env vars |
| YAML configuration | :white_check_mark: Exists | `application.yaml` |
| Modular config files | :white_check_mark: Exists | Separate files per concern |
| Koin DI modules | :white_check_mark: Exists | `Koin.kt` |
| **Environment Profiles** | :white_check_mark: **Implemented** | `application-{dev,staging,prod}.yaml` |

#### Implemented Environment Profiles

| File | Purpose |
|------|---------|
| `application.yaml` | Base configuration with all defaults |
| `application-dev.yaml` | Development: DEBUG logging, local services |
| `application-staging.yaml` | Staging: JSON logging, HSTS enabled |
| `application-prod.yaml` | Production: Higher pool sizes, strict security |

**Key Configuration Options**:
```yaml
# application.yaml structure
database:
    host: ${POSTGRES_HOST:localhost}
    port: ${POSTGRES_PORT:5432}
    poolSize: ${DB_POOL_SIZE:10}

security:
    hsts:
        enabled: ${ENABLE_HSTS:false}

logging:
    format: ${LOG_FORMAT:console}    # console | json
    level: ${LOG_LEVEL:INFO}
```

### Remaining Gaps

| Feature | Priority | Description |
|---------|----------|-------------|
| **Feature Flags** | :yellow_circle: Medium | No feature flag framework (FF4j, Unleash). |
| **Dynamic Configuration** | :yellow_circle: Medium | No runtime config updates without restart. |
| **Config Validation** | :orange_circle: High | No startup validation for required configs. |
| **Secret Vault Integration** | :red_circle: Critical | No HashiCorp Vault or AWS Secrets Manager. |

### Recommendations

```kotlin
// Config validation at startup
fun validateConfiguration(config: ApplicationConfig) {
    val required = listOf(
        "database.host",
        "database.port",
        "kafka.bootstrapServers"
    )
    val missing = required.filter {
        config.propertyOrNull(it) == null
    }
    if (missing.isNotEmpty()) {
        throw IllegalStateException("Missing required config: $missing")
    }
}
```

---

## 7. Testing

### Current State

| Feature | Status | Location |
|---------|--------|----------|
| Integration tests | :white_check_mark: Exists | `ApplicationTest.kt`, `PokemonApiIntegrationTest.kt` |
| TestContainers | :white_check_mark: Exists | `IntegrationTestBase.kt` |
| Smart service detection | :white_check_mark: Exists | Skips containers if services running |
| JUnit 5 + Kotlin Test | :white_check_mark: Exists | Test dependencies |
| CI/CD test execution | :white_check_mark: Exists | GitHub Actions workflow |
| **JaCoCo Code Coverage** | :white_check_mark: **Implemented** | `build.gradle.kts` |

#### Implemented JaCoCo Configuration

```kotlin
// build.gradle.kts
jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    // Excludes generated code (KSP)
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit { minimum = "0.50".toBigDecimal() }
        }
    }
}
```

**Usage**:
```bash
./gradlew test jacocoTestReport
# Report: build/reports/jacoco/test/html/index.html
```

### Remaining Gaps

| Feature | Priority | Description |
|---------|----------|-------------|
| **Unit Tests** | :orange_circle: High | No isolated unit tests with mocks. |
| **Service Layer Tests** | :orange_circle: High | No dedicated service tests. |
| **Contract Tests** | :orange_circle: High | No consumer-driven contract tests (Pact). |
| **Load/Performance Tests** | :yellow_circle: Medium | No load testing (K6, Gatling, JMeter). |
| **Security Tests** | :orange_circle: High | No OWASP vulnerability scanning. |
| **Mutation Testing** | :yellow_circle: Medium | No mutation testing for test quality. |

### Recommendations

```kotlin
// Example: Unit Test with MockK
class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val userService = UserService(userRepository)

    @Test
    fun `create user returns id`() = runTest {
        coEvery { userRepository.create(any()) } returns 1

        val result = userService.createUser(ExposedUser(0, "John", 30))

        assertEquals(1, result)
        coVerify { userRepository.create(any()) }
    }
}
```

---

## 8. Documentation

### Current State

| Feature | Status | Location |
|---------|--------|----------|
| README.md | :white_check_mark: Exists | Project root |
| CLAUDE.md | :white_check_mark: Exists | Project instructions |
| OpenAPI/Swagger | :white_check_mark: Exists | `/swagger`, `/api.json` |
| Inline code comments | :white_check_mark: Partial | Minimal |
| **Production Readiness Doc** | :white_check_mark: **Exists** | `docs/PRODUCTION_READINESS.md` |

### Remaining Gaps

| Feature | Priority | Description |
|---------|----------|-------------|
| **Architecture Decision Records** | :yellow_circle: Medium | No ADRs documenting design decisions. |
| **Runbooks** | :orange_circle: High | No operational runbooks for troubleshooting. |
| **API Changelog** | :yellow_circle: Medium | No CHANGELOG documenting API changes. |
| **Database Schema Docs** | :yellow_circle: Medium | No ER diagrams or schema documentation. |
| **Deployment Guide** | :orange_circle: High | Limited production deployment guidance. |
| **Troubleshooting Guide** | :yellow_circle: Medium | No common issues and solutions. |

### Recommendations

```
docs/
├── adr/
│   ├── 0001-use-ktor-framework.md
│   ├── 0002-use-koin-for-di.md
│   └── 0003-use-exposed-orm.md
├── runbooks/
│   ├── deployment.md
│   ├── scaling.md
│   ├── database-maintenance.md
│   └── incident-response.md
├── architecture/
│   ├── overview.md
│   └── database-schema.md
└── CHANGELOG.md
```

---

## Implementation Roadmap

### ~~Phase 0: Quick Wins~~ :white_check_mark: COMPLETED

| Task | Status | Location |
|------|--------|----------|
| ~~Add security headers middleware~~ | :white_check_mark: Done | `config/SecurityHeadersConfiguration.kt` |
| ~~Configure structured JSON logging~~ | :white_check_mark: Done | `logback.xml` |
| ~~Implement correlation ID tracking~~ | :white_check_mark: Done | `config/RequestLoggingConfiguration.kt` |
| ~~Create environment profiles~~ | :white_check_mark: Done | `application-{dev,staging,prod}.yaml` |
| ~~Implement API versioning~~ | :white_check_mark: Done | All controllers now use `/api/v1/` |
| ~~Configure JaCoCo coverage~~ | :white_check_mark: Done | `build.gradle.kts` |

### Phase 1: Security Foundation (Critical)

| Task | Effort | Dependencies |
|------|--------|--------------|
| Implement JWT authentication | Medium | Ktor Auth plugin |
| Implement input validation (Konform) | Medium | Konform dependency |
| Remove hardcoded credentials | Low | Secret management solution |
| Add HTTPS/TLS configuration | Medium | Certificates |

### Phase 2: Data Integrity

| Task | Effort | Dependencies |
|------|--------|--------------|
| Integrate Flyway migrations | Medium | Flyway dependency |
| Create RFC 7807 error responses | Medium | StatusPages plugin |
| Add request validation interceptor | Medium | Phase 1 validation |
| Configure database query timeouts | Low | HikariCP config |

### Phase 3: Observability

| Task | Effort | Dependencies |
|------|--------|--------------|
| Integrate Micrometer + Prometheus | Medium | Micrometer dependency |
| Add OpenTelemetry tracing | High | OpenTelemetry SDK |
| Add audit logging | Medium | Logging infrastructure |

### Phase 4: Resilience

| Task | Effort | Dependencies |
|------|--------|--------------|
| Add Resilience4j circuit breakers | Medium | Resilience4j dependency |
| Implement retry policies | Low | Resilience4j |
| Add rate limiting | Medium | Ktor RateLimit plugin |
| Configure per-endpoint timeouts | Low | None |
| Implement graceful degradation | Medium | Circuit breakers |

### Phase 5: Testing & Quality

| Task | Effort | Dependencies |
|------|--------|--------------|
| Add unit tests for services | Medium | MockK dependency |
| Implement contract tests | High | Pact framework |
| Add load testing | Medium | K6 or Gatling |
| Implement security scanning | Medium | OWASP tools |

### Phase 6: Documentation & Operations

| Task | Effort | Dependencies |
|------|--------|--------------|
| Create ADRs for key decisions | Low | None |
| Write deployment runbook | Medium | Production environment |
| Create troubleshooting guide | Medium | Operational experience |
| Document database schema | Low | None |
| Create API changelog | Low | None |

---

## Dependencies Added (Quick Wins)

```kotlin
// build.gradle.kts - Already added
dependencies {
    // Security Headers
    implementation(libs.ktor.server.default.headers)

    // Request Logging
    implementation(libs.ktor.server.call.logging)

    // Structured JSON Logging
    implementation(libs.logstash.logback.encoder)  // 8.0
    implementation(libs.janino)                     // 3.1.12 (for conditional logback)
}

// Gradle plugins - Already added
plugins {
    jacoco  // Code coverage
}
```

## Dependencies Still Needed

```kotlin
// build.gradle.kts additions for remaining production readiness

dependencies {
    // Security
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")

    // Validation
    implementation("io.konform:konform:0.4.0")

    // Resilience
    implementation("io.github.resilience4j:resilience4j-kotlin:2.2.0")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.2.0")
    implementation("io.github.resilience4j:resilience4j-retry:2.2.0")

    // Observability
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktor_version")
    implementation("io.micrometer:micrometer-registry-prometheus:1.12.0")
    implementation("io.opentelemetry:opentelemetry-api:1.32.0")
    implementation("io.opentelemetry:opentelemetry-sdk:1.32.0")

    // Database Migrations
    implementation("org.flywaydb:flyway-core:10.4.1")
    implementation("org.flywaydb:flyway-database-postgresql:10.4.1")

    // Rate Limiting
    implementation("io.ktor:ktor-server-rate-limit:$ktor_version")

    // Testing
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("au.com.dius.pact:pact-jvm-consumer-junit5:4.6.5")
}
```

---

## Conclusion

This service template now provides a stronger foundation for production deployment with the implementation of:
- **Security headers** protecting against common web vulnerabilities
- **Structured logging** for production log aggregation
- **Correlation IDs** for request tracing across services
- **Environment profiles** for consistent deployment across environments
- **API versioning** for backwards compatibility
- **Code coverage** for test quality metrics

The most critical remaining gaps are in **security** (authentication, authorization, input validation) and **data integrity** (database migrations). These should be prioritized in the next implementation phase.

---

## References

- [Ktor Documentation](https://ktor.io/docs/)
- [Koin Documentation](https://insert-koin.io/docs/)
- [Exposed Documentation](https://github.com/JetBrains/Exposed/wiki)
- [RFC 7807 - Problem Details](https://www.rfc-editor.org/rfc/rfc7807)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [12-Factor App](https://12factor.net/)
- [Logstash Logback Encoder](https://github.com/logfellow/logstash-logback-encoder)
