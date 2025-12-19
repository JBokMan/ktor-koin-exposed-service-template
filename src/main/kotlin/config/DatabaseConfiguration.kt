package config

import com.example.repository.UserService.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.StatementContext
import org.jetbrains.exposed.v1.core.statements.expandArgs
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.annotation.Single

@Single
class DatabaseConfiguration {
    private val log = KotlinLogging.logger {}

    val dataSource =
        HikariDataSource(
            HikariConfig().apply {
                val dbHost = System.getenv("POSTGRES_HOST") ?: "localhost"
                val dbPort = System.getenv("POSTGRES_PORT") ?: "5432"
                val dbName = System.getenv("POSTGRES_DB") ?: "mydatabase"
                val dbUser = System.getenv("POSTGRES_USER") ?: "myuser"
                val dbPassword = System.getenv("POSTGRES_PASSWORD") ?: "mypassword"

                jdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
                driverClassName = "org.postgresql.Driver"
                username = dbUser
                password = dbPassword
                maximumPoolSize = 10
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                connectionTimeout = 5000
                validationTimeout = 3000
                initializationFailTimeout = 60000 // Fail after 1 minute
            }
        )

    fun configureDatabase() {
        Database.connect(dataSource)

        transaction {
            addLogger(KotlinLoggingSqlLogger)
            SchemaUtils.create(Users)
        }

        log.info { "Database initialized successfully" }
    }

    fun close() {
        dataSource.close()
        log.info { "Database connection pool closed" }
    }

    object KotlinLoggingSqlLogger : org.jetbrains.exposed.v1.core.SqlLogger {
        private val kLogger = KotlinLogging.logger {}

        override fun log(context: StatementContext, transaction: Transaction) {
            kLogger.debug { "SQL: ${context.expandArgs(transaction)}" }
        }
    }
}
