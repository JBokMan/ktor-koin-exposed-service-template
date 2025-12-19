package com.example

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.net.Socket
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.stopKoin
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class IntegrationTestBase {
    companion object {
        private const val POSTGRES_SERVICE_PORT = 5432
        private const val KAFKA_SERVICE_PORT = 9092

        private fun isServiceRunning(port: Int): Boolean {
            return try {
                Socket("localhost", port).use { true }
            } catch (_: Exception) {
                false
            }
        }
    }

    private var postgresContainer: PostgreSQLContainer<*>? = null
    private var kafkaContainer: GenericContainer<*>? = null

    @BeforeAll
    fun startContainers() {
        val postgresRunning = isServiceRunning(POSTGRES_SERVICE_PORT)
        val kafkaRunning = isServiceRunning(KAFKA_SERVICE_PORT)

        // If both services are already running, we don't need to start containers
        if (postgresRunning && kafkaRunning) {
            println("Services are already running, skipping container startup")
            return
        }

        if (!postgresRunning) {
            postgresContainer =
                PostgreSQLContainer(DockerImageName.parse("postgres:latest"))
                    .withDatabaseName("mydatabase")
                    .withUsername("myuser")
                    .withPassword("mypassword")
                    .withCreateContainerCmdModifier { cmd ->
                        cmd.withHostConfig(
                            HostConfig()
                                .withPortBindings(
                                    PortBinding(
                                        Ports.Binding.bindPort(POSTGRES_SERVICE_PORT),
                                        ExposedPort(POSTGRES_SERVICE_PORT),
                                    )
                                )
                        )
                    }
            postgresContainer?.start()
        }

        if (!kafkaRunning) {
            kafkaContainer =
                GenericContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
                    .withEnv("KAFKA_NODE_ID", "1")
                    .withEnv("KAFKA_PROCESS_ROLES", "controller,broker")
                    .withEnv("KAFKA_CONTROLLER_QUORUM_VOTERS", "1@localhost:9093")
                    .withEnv(
                        "KAFKA_LISTENERS",
                        "PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093",
                    )
                    .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092")
                    .withEnv(
                        "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP",
                        "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT",
                    )
                    .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "PLAINTEXT")
                    .withEnv("KAFKA_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
                    .withEnv("CLUSTER_ID", "012345678910abcdefgh")
                    .withEnv("KAFKA_NUM_PARTITIONS", "1")
                    .withEnv("KAFKA_DEFAULT_REPLICATION_FACTOR", "1")
                    .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
                    .withExposedPorts(KAFKA_SERVICE_PORT)
                    .withCreateContainerCmdModifier { cmd ->
                        cmd.withHostConfig(
                            HostConfig()
                                .withPortBindings(
                                    PortBinding(
                                        Ports.Binding.bindPort(KAFKA_SERVICE_PORT),
                                        ExposedPort(KAFKA_SERVICE_PORT),
                                    )
                                )
                        )
                    }
                    .waitingFor(Wait.forListeningPort())
            kafkaContainer?.start()
        }
    }

    @AfterAll
    fun stopContainers() {
        postgresContainer?.stop()
        kafkaContainer?.stop()
        stopKoin()
    }

    fun withTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        application { module() }
        block()
    }
}
