package com.example.config

import com.example.boundary.UserController
import com.example.repository.UserRepository
import com.example.repository.UserService
import config.DatabaseConfiguration
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() {
    install(Koin) {
        modules(
            module {
                single { DatabaseConfiguration() }
                single { UserRepository() }
                single { UserService() }
                single { UserController(get()) }
            }
        )
    }
}
