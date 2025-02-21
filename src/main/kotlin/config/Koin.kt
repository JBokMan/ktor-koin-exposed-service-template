package com.example.config

import com.example.repository.UserRepository
import com.example.repository.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() {
    install(Koin) {
        modules(
            module {
                single { KotlinLogging.logger {} }
                single { UserRepository() }
                single { UserService() }
            }
        )
    }
}
