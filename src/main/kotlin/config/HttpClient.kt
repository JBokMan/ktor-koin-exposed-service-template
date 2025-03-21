package com.example.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.OkHttpClient
import org.koin.dsl.module

val networkModule = module {
    single {
        // Define OkHttpClient
        OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    single {
        // Create Ktor client with OkHttp engine
        HttpClient(OkHttp) {
            engine {
                config {
                    // You can configure additional OkHttp-specific options here
                }
            }
        }
    }
}
