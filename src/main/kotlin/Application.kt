package com.example

import com.example.boundary.userRoutes
import com.example.config.configureDatabase
import com.example.config.configureKoin
import com.example.config.configureSerialization
import com.example.config.configureSwagger
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureKoin()
    configureSwagger()
    configureDatabase()

    userRoutes()
}
