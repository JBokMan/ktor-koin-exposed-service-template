package config

import com.example.repository.UserService.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Single

@Single
class DatabaseConfiguration() {
    private val log = KotlinLogging.logger { this::class::simpleName }

    val dataSource =
        HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = "jdbc:postgresql://localhost:5432/mydatabase"
                driverClassName = "org.postgresql.Driver"
                username = "myuser"
                password = "mypassword"
                maximumPoolSize = 10
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                initializationFailTimeout = -1
                connectionTimeout = 30000
                validationTimeout = 5000
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

    object KotlinLoggingSqlLogger : org.jetbrains.exposed.sql.SqlLogger {
        private val kLogger = KotlinLogging.logger { this::class::simpleName }

        override fun log(context: StatementContext, transaction: Transaction) {
            kLogger.debug { "SQL: ${context.expandArgs(transaction)}" }
        }
    }
}
