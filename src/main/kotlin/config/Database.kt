package com.example.config

import com.example.repository.UserService.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KLogger
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject

fun Application.configureDatabase() {

    val log by inject<KLogger>()

    Database.connect(HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://localhost:5432/mydatabase"
        driverClassName = "org.postgresql.Driver"
        username = "myuser"
        password = "mypassword"
        maximumPoolSize = 10
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
    }))

    transaction {
        SchemaUtils.create(Users)
    }

    log.info { "Database initialized successfully" }
}
