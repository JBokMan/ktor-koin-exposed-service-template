package com.example.config

import io.ktor.server.application.Application
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing

fun Application.configureSwagger() {
    routing { swaggerUI(path = "swagger", swaggerFile = "openapi/openapi.yaml") }
}
