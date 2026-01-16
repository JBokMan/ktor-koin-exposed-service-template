package com.example.config

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import java.util.UUID
import org.slf4j.event.Level

fun Application.configureRequestLogging() {
    install(CallLogging) {
        level = Level.INFO

        // Filter which requests to log (exclude health checks to reduce noise)
        filter { call ->
            val path = call.request.path()
            !path.startsWith("/health") &&
                !path.startsWith("/liveness") &&
                !path.startsWith("/readiness") &&
                !path.startsWith("/startup")
        }

        // Add correlation ID to MDC for structured logging
        mdc("correlationId") { call ->
            call.request.headers["X-Correlation-ID"]
                ?: call.request.headers["X-Request-ID"]
                ?: UUID.randomUUID().toString()
        }

        // Add request path to MDC
        mdc("requestPath") { call -> call.request.path() }

        // Add HTTP method to MDC
        mdc("requestMethod") { call -> call.request.httpMethod.value }

        // Format log message
        format { call ->
            val status = call.response.status()
            val method = call.request.httpMethod.value
            val path = call.request.path()
            "$method $path -> $status"
        }
    }
}
