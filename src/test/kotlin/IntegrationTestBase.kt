package com.example

import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.io.File
import java.net.Socket
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.stopKoin
import org.testcontainers.containers.ComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class IntegrationTestBase {
    companion object {
        private const val POSTGRES_SERVICE_NAME = "postgres"
        private const val POSTGRES_SERVICE_PORT = 5432
        private const val KAFKA_SERVICE_NAME = "kafka"
        private const val KAFKA_SERVICE_PORT = 9092

        private fun isServiceRunning(port: Int): Boolean {
            return try {
                Socket("localhost", port).use { true }
            } catch (_: Exception) {
                false
            }
        }
    }

    private lateinit var composeContainer: ComposeContainer

    @BeforeAll
    fun startContainers() {
        val postgresRunning = isServiceRunning(POSTGRES_SERVICE_PORT)
        val kafkaRunning = isServiceRunning(KAFKA_SERVICE_PORT)

        // If both services are already running, we don't need to start containers
        if (postgresRunning && kafkaRunning) {
            println("Services are already running, skipping container startup")
            return
        }

        composeContainer =
            ComposeContainer(File("docker-compose.yml"))
                .withExposedService(
                    POSTGRES_SERVICE_NAME,
                    POSTGRES_SERVICE_PORT,
                    Wait.forListeningPort(),
                )
                .withExposedService(KAFKA_SERVICE_NAME, KAFKA_SERVICE_PORT, Wait.forListeningPort())
                .withLocalCompose(true)

        composeContainer.start()
    }

    @AfterAll
    fun stopContainers() {
        if (::composeContainer.isInitialized) {
            composeContainer.stop()
        }

        stopKoin()
    }

    fun withTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        application { module() }
        block()
    }
}
