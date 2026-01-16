package com.example

import boundary.pokemon.PokemonController
import com.example.boundary.KafkaController
import com.example.boundary.UserController
import com.example.config.CohortConfiguration
import com.example.config.KafkaConfiguration
import com.example.config.configureKoin
import com.example.config.configureRequestLogging
import com.example.config.configureSecurityHeaders
import com.example.config.configureSerialization
import com.example.config.configureSwagger
import com.example.service.KafkaService
import config.DatabaseConfiguration
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.config.yaml.YamlConfig
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(CIO, port = port, module = Application::module).start(wait = true)
}

fun Application.module() {
    val config = YamlConfig("application.yaml") ?: error("Failed to load configuration")

    configureKoin(config)
    configureSecurityHeaders()
    configureRequestLogging()
    configureSerialization()
    configureSwagger()

    val databaseConfiguration: DatabaseConfiguration by inject()
    databaseConfiguration.configureDatabase()

    val kafkaConfiguration: KafkaConfiguration by inject()
    kafkaConfiguration.configureKafka(this)

    val cohortConfiguration: CohortConfiguration by inject()
    launch { cohortConfiguration.configureCohort(this@module) }

    val userController by inject<UserController>()
    userController.registerUserRoutes(this)

    val kafkaController by inject<KafkaController>()
    kafkaController.registerKafkaRoutes(this)

    val pokemonController by inject<PokemonController>()
    pokemonController.registerRoutes(this)

    val kafkaService by inject<KafkaService>()

    install(
        createApplicationPlugin("ShutdownPlugin") {
            on(MonitoringEvent(ApplicationStopping)) {
                kafkaConfiguration.close()
                kafkaService.close()
                databaseConfiguration.close()
            }
        }
    )
}
