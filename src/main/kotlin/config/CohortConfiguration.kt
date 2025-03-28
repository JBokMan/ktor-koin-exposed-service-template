package com.example.config

import com.sksamuel.cohort.Cohort
import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.db.DatabaseConnectionHealthCheck
import com.sksamuel.cohort.healthcheck.http.EndpointHealthCheck
import com.sksamuel.cohort.hikari.HikariConnectionsHealthCheck
import com.sksamuel.cohort.hikari.HikariDataSourceManager
import com.sksamuel.cohort.logback.LogbackManager
import com.sksamuel.cohort.memory.FreememHealthCheck
import com.sksamuel.cohort.memory.GarbageCollectionTimeCheck
import com.sksamuel.cohort.system.AvailableCoresHealthCheck
import com.sksamuel.cohort.system.DiskSpaceHealthCheck
import com.sksamuel.cohort.system.SystemCpuHealthCheck
import com.sksamuel.cohort.system.SystemLoadHealthCheck
import com.sksamuel.cohort.threads.LiveThreadsHealthCheck
import com.sksamuel.cohort.threads.ThreadDeadlockHealthCheck
import config.DatabaseConfiguration
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import java.nio.file.FileSystems
import java.nio.file.Files
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Single
import org.koin.java.KoinJavaComponent.inject

@Single
class CohortConfiguration() {
    val databaseConfiguration by inject<DatabaseConfiguration>(DatabaseConfiguration::class.java)
    val client: HttpClient by inject<HttpClient>(HttpClient::class.java)

    @OptIn(ExperimentalTime::class)
    fun configureCohort(application: Application) {

        val startUpChecks =
            HealthCheckRegistry(Dispatchers.Default) {
                register(
                    check = AvailableCoresHealthCheck.multiple,
                    initialDelay = 30.seconds,
                    checkInterval = 1.minutes,
                )
                register(
                    check = DatabaseConnectionHealthCheck(databaseConfiguration.dataSource),
                    initialDelay = 30.seconds,
                    checkInterval = 1.minutes,
                )
                register(
                    check = FreememHealthCheck.gb(1),
                    initialDelay = 30.seconds,
                    checkInterval = 1.minutes,
                )
                register(
                    check = HikariConnectionsHealthCheck(databaseConfiguration.dataSource, 1),
                    initialDelay = 30.seconds,
                    checkInterval = 5.seconds,
                )
                val fileStore = Files.getFileStore(FileSystems.getDefault().getPath("/"))
                register(
                    check = DiskSpaceHealthCheck(fileStore = fileStore),
                    initialDelay = 30.seconds,
                    checkInterval = 5.minutes,
                )
            }

        val readinessChecks =
            HealthCheckRegistry(Dispatchers.Default) {
                register(
                    check = GarbageCollectionTimeCheck(5),
                    initialDelay = 30.seconds,
                    checkInterval = 1.minutes,
                )
                register(
                    check = ThreadDeadlockHealthCheck(),
                    initialDelay = 30.seconds,
                    checkInterval = 1.minutes,
                )
                register(
                    check = SystemLoadHealthCheck(50.0),
                    initialDelay = 30.seconds,
                    checkInterval = 1.minutes,
                )
                register(
                    check = SystemCpuHealthCheck(0.8),
                    initialDelay = 30.seconds,
                    checkInterval = 1.minutes,
                )

                // Ensures that an HTTP service is reachable
                register(
                    check =
                        EndpointHealthCheck(
                            name = "get_user_endpoint",

                            // Function to perform the HTTP request
                            fn = { client: HttpClient ->
                                client.get("http://localhost:8080/users/1") // Using test user ID 1
                            },

                            // Function to evaluate the response
                            eval = { response -> response.status == HttpStatusCode.OK },
                        ),
                    initialDelay = 30.seconds,
                    checkInterval = 1.minutes,
                )
            }

        val livenessChecks =
            HealthCheckRegistry(Dispatchers.Default) {
                register(
                    check = LiveThreadsHealthCheck(500),
                    initialDelay = 30.seconds,
                    checkInterval = 1.minutes,
                )
            }

        val healthChecks =
            HealthCheckRegistry(Dispatchers.Default) {
                // These include checks that are useful for continuous health monitoring.
                val fileStore = Files.getFileStore(FileSystems.getDefault().getPath("/"))
                register(
                    check = SystemCpuHealthCheck(0.8),
                    initialDelay = 30.seconds,
                    checkInterval = 1.minutes,
                )
                register(
                    check = AvailableCoresHealthCheck.multiple,
                    initialDelay = 30.seconds,
                    checkInterval = 1.minutes,
                )
                register(
                    check = DiskSpaceHealthCheck(fileStore = fileStore),
                    initialDelay = 30.seconds,
                    checkInterval = 5.minutes,
                )
            }

        application.install(Cohort) {

            // enable an endpoint to display operating system name and version
            operatingSystem = true

            // enable runtime JVM information such as vm options and vendor name
            jvmInfo = true

            // configure the Logback log manager to show effective log levels and allow runtime
            // adjustment
            logManager = LogbackManager

            // show connection pool information
            dataSources = listOf(HikariDataSourceManager(databaseConfiguration.dataSource))

            // show current system properties
            sysprops = true

            // enable health checks for kubernetes
            // each of these is optional and can map to any healthcheck url you wish
            // for example if you just want a single health endpoint, you could use /health
            healthcheck("/liveness", livenessChecks)
            healthcheck("/readiness", readinessChecks)
            healthcheck("/startup", startUpChecks)
            healthcheck("/health", healthChecks)
        }
    }
}
