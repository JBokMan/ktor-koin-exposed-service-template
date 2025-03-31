package com.example.config

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.yaml.YamlConfig
import org.koin.ksp.generated.defaultModule
import org.koin.ktor.plugin.Koin

fun Application.configureKoin(config: YamlConfig) {
    val ktorModule =
        org.koin.dsl.module {
            single<ApplicationConfig> { config }
        } // Creates A Bean of ApplicationConfig

    install(Koin) { modules(ktorModule, defaultModule, networkModule) }
}
