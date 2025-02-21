package com.example

import com.example.boundary.userRoutes
import com.example.config.configureDatabase
import com.example.config.configureKoin
import com.example.config.configureSerialization
import com.example.config.configureSwagger
import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer

fun main() {
    embeddedServer(CIO, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureKoin()
    configureSwagger()
    configureDatabase()

    userRoutes()
}
