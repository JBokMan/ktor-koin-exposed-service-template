package com.example

import com.example.boundary.UserController
import com.example.config.CohortConfiguration
import com.example.config.KafkaConfiguration
import com.example.config.configureKoin
import com.example.config.configureSerialization
import com.example.config.configureSwagger
import config.DatabaseConfiguration
import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.config.yaml.YamlConfig
import io.ktor.server.engine.embeddedServer
import org.koin.ktor.ext.inject

fun main() {
    embeddedServer(CIO, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    val config = YamlConfig("application.yaml") ?: error("Failed to load configuration")

    configureKoin(config)
    configureSerialization()
    configureSwagger()

    val databaseConfiguration: DatabaseConfiguration by inject()
    databaseConfiguration.configureDatabase()

    val kafkaConfiguration: KafkaConfiguration by inject()
    kafkaConfiguration.configureKafka()

    val cohortConfiguration: CohortConfiguration by inject()
    cohortConfiguration.configureCohort(this)

    val userController by inject<UserController>()
    userController.registerUserRoutes(this)
}
